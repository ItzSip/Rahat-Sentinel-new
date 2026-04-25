"""
Rahat Sentinel — FastAPI real-time alert backbone.

Endpoints:
  GET  /                       → Health check
  GET  /api/alerts/latest      → Last 5 cached alerts
  GET  /api/alerts/{alert_id}  → Single alert by ID
  POST /api/alerts/simulate    → Publish a mock alert to Redis (debug)
  WS   /ws/alerts              → Live alert stream

Architecture:
  Redis Pub/Sub (sentinel:alerts)  →  ConnectionManager  →  WebSocket clients
"""

from __future__ import annotations

import asyncio
import json
import logging
import os
import random
import uuid
from contextlib import asynccontextmanager
from datetime import datetime, timezone
from typing import Any

import redis.asyncio as aioredis
from dotenv import load_dotenv
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from . import alert_cache
from .ble_codec import pack_alert, unpack_alert, PAYLOAD_SIZE
from .models import (
    AlertHistoryResponse,
    AlertResponse,
    ESP32Device,
    ESP32ListResponse,
    SentinelAlert,
    WebSocketMessage,
)
from .redis_listener import start_listener

# In-memory store for ESP32 devices keyed by device_id
_esp32_store: dict[str, ESP32Device] = {}
_esp32_lock = asyncio.Lock()

load_dotenv()

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s  %(name)-22s  %(levelname)-5s  %(message)s",
)
logger = logging.getLogger("sentinel.api")

REDIS_URL = os.getenv("REDIS_URL", "redis://localhost:6379")
REDIS_CHANNEL = "sentinel:alerts"


# ---------------------------------------------------------------------------
# WebSocket connection manager
# ---------------------------------------------------------------------------

class ConnectionManager:
    """Tracks active WS connections and broadcasts alerts."""

    def __init__(self) -> None:
        self._connections: list[WebSocket] = []

    async def connect(self, ws: WebSocket) -> None:
        await ws.accept()
        self._connections.append(ws)
        logger.info("WS client connected — total: %d", len(self._connections))

        # Send cached alert history so the client starts with context
        recent = await alert_cache.get_recent()
        history_msg = WebSocketMessage(
            event="history",
            data=[a.model_dump() for a in recent],
        )
        await ws.send_text(history_msg.model_dump_json())

        # Send current ESP32 device list
        async with _esp32_lock:
            devices = list(_esp32_store.values())
        esp32_msg = WebSocketMessage(
            event="esp32_list",
            data=[d.model_dump() for d in devices],
        )
        await ws.send_text(esp32_msg.model_dump_json())

    def disconnect(self, ws: WebSocket) -> None:
        if ws in self._connections:
            self._connections.remove(ws)
        logger.info("WS client disconnected — total: %d", len(self._connections))

    async def broadcast(self, alert: SentinelAlert) -> None:
        """Send a new_alert message to every connected client."""
        msg = WebSocketMessage(
            event="new_alert",
            data=alert.model_dump(),
        )
        await self.broadcast_raw(msg.model_dump_json())

    async def broadcast_raw(self, payload: str) -> None:
        """Send a pre-serialised JSON string to every connected client."""
        stale: list[WebSocket] = []
        for ws in self._connections:
            try:
                await ws.send_text(payload)
            except Exception:
                stale.append(ws)
        for ws in stale:
            self.disconnect(ws)

    @property
    def client_count(self) -> int:
        return len(self._connections)


manager = ConnectionManager()


# ---------------------------------------------------------------------------
# Lifespan — start / stop the Redis listener
# ---------------------------------------------------------------------------

@asynccontextmanager
async def lifespan(app: FastAPI):
    """Start the Redis pub/sub listener as a background task."""
    logger.info("Starting Redis listener on %s", REDIS_URL)
    task = asyncio.create_task(
        start_listener(redis_url=REDIS_URL, on_alert=manager.broadcast)
    )
    yield
    task.cancel()
    try:
        await task
    except asyncio.CancelledError:
        pass
    logger.info("Redis listener stopped")


# ---------------------------------------------------------------------------
# FastAPI app
# ---------------------------------------------------------------------------

app = FastAPI(
    title="Rahat Sentinel — Alert Backbone",
    description=(
        "Real-time alert backbone for Rahat Sentinel. "
        "Bridges ML inference ↔ dashboard ↔ BLE mesh."
    ),
    version="0.1.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ---------------------------------------------------------------------------
# REST endpoints
# ---------------------------------------------------------------------------

@app.get("/", tags=["health"])
async def health_check() -> dict[str, Any]:
    """Health check & system status."""
    return {
        "status": "ok",
        "service": "rahat-sentinel-backbone",
        "ws_clients": manager.client_count,
        "cached_alerts": len(await alert_cache.get_recent()),
        "redis_url": REDIS_URL,
        "timestamp": datetime.now(timezone.utc).isoformat(),
    }


@app.get("/api/alerts/latest", response_model=AlertHistoryResponse, tags=["alerts"])
async def get_latest_alerts() -> AlertHistoryResponse:
    """Return the last 5 alerts from the in-memory cache."""
    recent = await alert_cache.get_recent()
    return AlertHistoryResponse(count=len(recent), alerts=recent)


@app.get("/api/alerts/{alert_id}", response_model=AlertResponse, tags=["alerts"])
async def get_alert_by_id(alert_id: str) -> AlertResponse | JSONResponse:
    """Lookup a single alert by ID from the cache."""
    recent = await alert_cache.get_recent()
    for alert in recent:
        if alert.id == alert_id:
            return AlertResponse(alert=alert)
    return JSONResponse(status_code=404, content={"status": "error", "detail": "Alert not found"})


@app.post("/api/alerts/simulate", tags=["debug"])
async def simulate_alert() -> dict[str, Any]:
    """
    Publish a randomly generated mock alert to Redis.
    Useful for testing the WebSocket pipeline without the ML model.
    """
    cells = [[round(random.uniform(0.0, 1.0), 3) for _ in range(20)] for _ in range(20)]
    risk = round(random.uniform(0.5, 1.0), 3)
    severity = int(risk * 255)

    alert = SentinelAlert(
        id=uuid.uuid4().hex[:12],
        risk_score=risk,
        cells=cells,
        district="Bhilai/Durg",
        region_id=1,
        severity=severity,
        gradcam_hash=uuid.uuid4().hex[:16],
        timestamp=datetime.now(timezone.utc).isoformat(),
    )

    # Publish to Redis
    try:
        client = aioredis.from_url(REDIS_URL, decode_responses=True)
        await client.publish(REDIS_CHANNEL, alert.model_dump_json())
        await client.aclose()
    except Exception as exc:
        logger.error("Failed to publish to Redis: %s", exc)
        return {"status": "error", "detail": str(exc)}

    return {"status": "published", "alert_id": alert.id, "risk_score": alert.risk_score}


# ---------------------------------------------------------------------------
# BLE codec utility endpoint (debug / demo)
# ---------------------------------------------------------------------------

@app.post("/api/ble/encode", tags=["ble"])
async def ble_encode(alert: SentinelAlert) -> dict[str, Any]:
    """Pack a SentinelAlert into BLE binary and return hex + size."""
    binary = pack_alert(alert)
    return {
        "hex": binary.hex(),
        "size_bytes": len(binary),
        "max_ble_bytes": 512,
        "decoded": unpack_alert(binary),
    }


# ---------------------------------------------------------------------------
# ESP32 device endpoints
# ---------------------------------------------------------------------------

@app.post("/api/esp32/update", tags=["esp32"])
async def esp32_update(device: ESP32Device) -> dict[str, Any]:
    """
    Receive a location + severity packet from an ESP32 field node.

    The device_id is used as the upsert key — repeated updates from the same
    node move the marker rather than creating duplicates.
    """
    device.severity = device.severity.upper()
    if device.severity not in ("CRITICAL", "HIGH", "MEDIUM", "LOW"):
        device.severity = "LOW"
    if not device.name:
        device.name = device.device_id

    async with _esp32_lock:
        _esp32_store[device.device_id] = device

    # Broadcast live update to all dashboard WebSocket clients
    msg = WebSocketMessage(event="esp32_update", data=device.model_dump())
    await manager.broadcast_raw(msg.model_dump_json())

    logger.info("ESP32 update: %s  sev=%s  %.5f,%.5f", device.device_id, device.severity, device.lat, device.lon)
    return {"status": "ok", "device_id": device.device_id, "severity": device.severity}


@app.get("/api/esp32/devices", response_model=ESP32ListResponse, tags=["esp32"])
async def esp32_list() -> ESP32ListResponse:
    """Return all known ESP32 field nodes."""
    async with _esp32_lock:
        devices = list(_esp32_store.values())
    return ESP32ListResponse(count=len(devices), devices=devices)


@app.delete("/api/esp32/devices/{device_id}", tags=["esp32"])
async def esp32_delete(device_id: str) -> dict[str, Any]:
    """Remove an ESP32 node from the live map."""
    async with _esp32_lock:
        removed = _esp32_store.pop(device_id, None)
    if removed is None:
        return JSONResponse(status_code=404, content={"status": "error", "detail": "Device not found"})
    return {"status": "ok", "removed": device_id}


# ---------------------------------------------------------------------------
# WebSocket endpoint
# ---------------------------------------------------------------------------

@app.websocket("/ws/alerts")
async def ws_alerts(ws: WebSocket) -> None:
    """
    Real-time alert stream.

    On connect: sends cached alert history (event="history").
    On Redis message: broadcasts new_alert events.
    Clients can send 'ping' to keep the connection alive.
    """
    await manager.connect(ws)
    try:
        while True:
            # Keep the connection alive; handle client pings
            data = await ws.receive_text()
            if data.strip().lower() == "ping":
                await ws.send_text(
                    WebSocketMessage(event="heartbeat", data={"pong": True}).model_dump_json()
                )
    except WebSocketDisconnect:
        manager.disconnect(ws)
    except Exception:
        manager.disconnect(ws)



