"""
Rahat Sentinel — WebSocket Alert Receiver & BLE Encoder.

Connects to the Sentinel FastAPI WebSocket, receives live alerts,
encodes each into a 47-byte BLE payload, and logs everything
with colour-coded, timestamped output for demo clarity.

Usage:
    cd backend/
    python -m receiver          # connects to ws://localhost:8000/ws/alerts
    python -m receiver --test   # skips WS, simulates alerts every 2 s
"""

from __future__ import annotations

import argparse
import asyncio
import json
import logging
import random
import struct
import sys
import uuid
from datetime import datetime, timezone
from typing import Any

try:
    import websockets
    from websockets.exceptions import (
        ConnectionClosed,
        InvalidURI,
        WebSocketException,
    )
except ImportError:
    print("✖  'websockets' package not found. Install with: pip install websockets")
    sys.exit(1)

from app.ble_codec import pack_alert, unpack_alert, PAYLOAD_SIZE
from app.models import SentinelAlert

# ---------------------------------------------------------------------------
# ANSI colour helpers
# ---------------------------------------------------------------------------

class C:
    """ANSI escape codes for coloured terminal output."""
    RESET   = "\033[0m"
    BOLD    = "\033[1m"
    DIM     = "\033[2m"
    RED     = "\033[91m"
    YELLOW  = "\033[93m"
    GREEN   = "\033[92m"
    CYAN    = "\033[96m"
    MAGENTA = "\033[95m"
    WHITE   = "\033[97m"
    BG_RED  = "\033[41m"
    BG_YEL  = "\033[43m"
    BG_GRN  = "\033[42m"


def _severity_colour(severity: int) -> str:
    """Return an ANSI colour based on severity (0-255)."""
    if severity >= 180:
        return C.RED
    if severity >= 100:
        return C.YELLOW
    return C.GREEN


def _ts() -> str:
    """Compact local timestamp for log lines."""
    return datetime.now().strftime("%H:%M:%S")


# ---------------------------------------------------------------------------
# Core alert processing
# ---------------------------------------------------------------------------

def process_alert(alert_data: dict[str, Any]) -> None:
    """
    Process a single alert dict:
      1. Build a SentinelAlert model
      2. Log formatted alert details
      3. Encode to BLE binary
      4. Log the hex payload
    """
    try:
        alert = SentinelAlert(**alert_data)
    except Exception as exc:
        print(f"  {C.DIM}[{_ts()}]{C.RESET}  {C.YELLOW}⚠  Skipping malformed alert: {exc}{C.RESET}")
        return

    sev_colour = _severity_colour(alert.severity)

    # ── Formatted alert log ──────────────────────────────────────────
    print()
    print(f"  {C.DIM}[{_ts()}]{C.RESET}  {sev_colour}{C.BOLD}🚨 ALERT RECEIVED{C.RESET}")
    print(f"  {C.DIM}├─{C.RESET} Region:    {C.CYAN}{alert.district}{C.RESET}  (id {alert.region_id})")
    print(f"  {C.DIM}├─{C.RESET} Severity:  {sev_colour}{alert.severity}/255{C.RESET}  "
          f"(risk {alert.risk_score:.3f})")
    print(f"  {C.DIM}├─{C.RESET} Time:      {alert.timestamp}")
    print(f"  {C.DIM}├─{C.RESET} Center:    {alert.center.lat:.4f}°N, {alert.center.lon:.4f}°E")
    print(f"  {C.DIM}└─{C.RESET} GradCAM:   {alert.gradcam_hash}")

    # ── BLE encoding ─────────────────────────────────────────────────
    try:
        payload = pack_alert(alert)
        hex_str = payload.hex()
        print(f"  {C.DIM}[{_ts()}]{C.RESET}  {C.MAGENTA}📡 BLE PAYLOAD READY{C.RESET}  "
              f"({len(payload)}B)  {C.DIM}{hex_str}{C.RESET}")
    except Exception as exc:
        print(f"  {C.DIM}[{_ts()}]{C.RESET}  {C.RED}✖  BLE encode failed: {exc}{C.RESET}")


# ---------------------------------------------------------------------------
# WebSocket listener
# ---------------------------------------------------------------------------

WS_URL = "ws://localhost:8000/ws/alerts"
RETRY_DELAY = 3  # seconds


async def _handle_message(raw: str) -> None:
    """Parse a WebSocketMessage envelope and process alerts inside."""
    try:
        msg = json.loads(raw)
    except json.JSONDecodeError as exc:
        print(f"  {C.DIM}[{_ts()}]{C.RESET}  {C.YELLOW}⚠  Bad JSON, skipping: {exc}{C.RESET}")
        return

    event = msg.get("event", "")
    data = msg.get("data")

    if event == "new_alert" and isinstance(data, dict):
        process_alert(data)

    elif event == "history" and isinstance(data, list):
        print(f"  {C.DIM}[{_ts()}]{C.RESET}  {C.CYAN}📜 Received {len(data)} cached alert(s){C.RESET}")
        for alert_data in data:
            process_alert(alert_data)

    elif event == "heartbeat":
        print(f"  {C.DIM}[{_ts()}]{C.RESET}  {C.DIM}💓 heartbeat{C.RESET}")

    else:
        print(f"  {C.DIM}[{_ts()}]{C.RESET}  {C.DIM}ℹ  Unknown event: {event}{C.RESET}")


async def ws_listener() -> None:
    """Connect to the WS server and listen forever with auto-reconnect."""
    print(f"\n  {C.BOLD}{'═' * 52}{C.RESET}")
    print(f"  {C.BOLD}{C.CYAN}  Rahat Sentinel — Alert Receiver & BLE Encoder{C.RESET}")
    print(f"  {C.BOLD}{'═' * 52}{C.RESET}")
    print(f"  {C.DIM}Target:{C.RESET}  {WS_URL}")
    print()

    first_attempt = True

    while True:
        try:
            print(f"  {C.DIM}[{_ts()}]{C.RESET}  Connecting to WebSocket …")
            async with websockets.connect(WS_URL) as ws:
                first_attempt = False
                print(f"  {C.DIM}[{_ts()}]{C.RESET}  {C.GREEN}✔  Connected!{C.RESET}")

                # Send periodic pings to keep connection alive
                async def _ping_loop():
                    while True:
                        await asyncio.sleep(25)
                        try:
                            await ws.send("ping")
                        except Exception:
                            break

                ping_task = asyncio.create_task(_ping_loop())

                try:
                    async for raw_msg in ws:
                        await _handle_message(raw_msg)
                finally:
                    ping_task.cancel()

        except (ConnectionClosed, ConnectionRefusedError, OSError) as exc:
            label = type(exc).__name__
            print(f"  {C.DIM}[{_ts()}]{C.RESET}  {C.YELLOW}⚠  {label} — ", end="")
            if first_attempt:
                print(f"server unavailable, switching to test mode{C.RESET}")
                await _fallback_test_loop()
                return
            print(f"retrying in {RETRY_DELAY}s …{C.RESET}")
            await asyncio.sleep(RETRY_DELAY)

        except WebSocketException as exc:
            print(f"  {C.DIM}[{_ts()}]{C.RESET}  {C.RED}✖  WebSocket error: {exc}{C.RESET}")
            await asyncio.sleep(RETRY_DELAY)

        except KeyboardInterrupt:
            print(f"\n  {C.DIM}[{_ts()}]{C.RESET}  {C.CYAN}Shutting down …{C.RESET}")
            return


# ---------------------------------------------------------------------------
# Fallback test mode — simulates alerts when WS is unavailable
# ---------------------------------------------------------------------------

DEMO_DISTRICTS = [
    "NIT Delhi", "North Delhi", "Civil Lines", "Rohini",
    "Model Town", "Pitampura", "Burari", "Shalimar Bagh",
]

# NIT Delhi center: 28.7453°N, 77.1157°E
_NIT_LAT, _NIT_LON = 28.7453, 77.1157


def _random_alert_data() -> dict[str, Any]:
    """Generate a fake SentinelAlert-shaped dict for testing."""
    risk = round(random.uniform(0.3, 1.0), 3)
    return {
        "id": uuid.uuid4().hex[:12],
        "risk_score": risk,
        "cells": [[round(random.uniform(0, 1), 2) for _ in range(20)] for _ in range(20)],
        "district": random.choice(DEMO_DISTRICTS),
        "region_id": random.randint(1, 50),
        "severity": int(risk * 255),
        "gradcam_hash": uuid.uuid4().hex[:16],
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "center": {
            "lat": round(_NIT_LAT + random.uniform(-0.05, 0.05), 4),
            "lon": round(_NIT_LON + random.uniform(-0.05, 0.05), 4),
        },
    }


async def _fallback_test_loop() -> None:
    """Emit simulated alerts every 2 seconds."""
    print()
    print(f"  {C.BOLD}{C.YELLOW}{'─' * 52}{C.RESET}")
    print(f"  {C.BOLD}{C.YELLOW}  ⚡  TEST MODE — simulating alerts every 2 s{C.RESET}")
    print(f"  {C.BOLD}{C.YELLOW}{'─' * 52}{C.RESET}")
    print()

    try:
        while True:
            data = _random_alert_data()
            process_alert(data)
            await asyncio.sleep(2)
    except KeyboardInterrupt:
        print(f"\n  {C.DIM}[{_ts()}]{C.RESET}  {C.CYAN}Test mode stopped.{C.RESET}")


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main() -> None:
    parser = argparse.ArgumentParser(
        description="Rahat Sentinel — Alert Receiver & BLE Encoder",
    )
    parser.add_argument(
        "--test", action="store_true",
        help="Skip WebSocket and run in simulated test mode",
    )
    args = parser.parse_args()

    if args.test:
        asyncio.run(_fallback_test_loop())
    else:
        asyncio.run(ws_listener())


if __name__ == "__main__":
    main()
