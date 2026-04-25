"""
Pydantic v2 models for the Rahat Sentinel alert backbone.

Data contract:
  - 20x20 spatial risk grid (quantized risk matrix)
  - Default center: Bhilai (Lat: 21.19, Lon: 81.35)
  - District: Bhilai/Durg
"""

from __future__ import annotations

import uuid
from datetime import datetime, timezone
from typing import Optional

from pydantic import BaseModel, Field


# ---------------------------------------------------------------------------
# Core alert model
# ---------------------------------------------------------------------------

class GeoCenter(BaseModel):
    """Geographic center point of the alert region."""

    lat: float = Field(default=21.19, description="Latitude (default: Bhilai)")
    lon: float = Field(default=81.35, description="Longitude (default: Bhilai)")


class SentinelAlert(BaseModel):
    """
    Primary alert payload produced by the ML inference pipeline.

    The *cells* field is a 20x20 matrix of float risk scores (0.0 – 1.0).
    Each cell represents a quantized spatial region within the alert zone.
    """

    id: str = Field(default_factory=lambda: uuid.uuid4().hex[:12])
    risk_score: float = Field(
        ..., ge=0.0, le=1.0, description="Aggregate anomaly risk score"
    )
    cells: list[list[float]] = Field(
        ...,
        min_length=20,
        max_length=20,
        description="20x20 spatial risk matrix",
    )
    timestamp: str = Field(
        default_factory=lambda: datetime.now(timezone.utc).isoformat(),
        description="ISO-8601 UTC timestamp",
    )
    district: str = Field(default="Bhilai/Durg")
    region_id: int = Field(
        default=1, ge=0, le=65535, description="Region ID for BLE encoding (uint16)"
    )
    severity: int = Field(
        default=0, ge=0, le=255, description="Severity bucket 0-255 for BLE encoding"
    )
    gradcam_hash: str = Field(
        default="0000000000000000",
        max_length=16,
        description="8-byte hex hash of GradCAM polygon",
    )
    center: GeoCenter = Field(default_factory=GeoCenter)


# ---------------------------------------------------------------------------
# REST response wrappers
# ---------------------------------------------------------------------------

class AlertResponse(BaseModel):
    """Single alert REST response."""

    status: str = "ok"
    alert: SentinelAlert


class AlertHistoryResponse(BaseModel):
    """Last-N alerts REST response."""

    status: str = "ok"
    count: int
    alerts: list[SentinelAlert]


# ---------------------------------------------------------------------------
# WebSocket envelope
# ---------------------------------------------------------------------------

class WebSocketMessage(BaseModel):
    """Envelope sent over the WebSocket connection."""

    event: str = Field(
        ..., description="Event type: 'new_alert' | 'history' | 'heartbeat' | 'esp32_update' | 'esp32_list'"
    )
    data: Optional[dict | list] = None


# ---------------------------------------------------------------------------
# ESP32 device model
# ---------------------------------------------------------------------------

class ESP32Device(BaseModel):
    """Location + severity packet from an ESP32 field node."""

    device_id: str = Field(..., description="Unique device identifier, e.g. 'ESP32_01'")
    name: str = Field(default="", description="Optional human-readable label")
    lat: float = Field(..., ge=-90.0, le=90.0, description="Latitude")
    lon: float = Field(..., ge=-180.0, le=180.0, description="Longitude")
    severity: str = Field(
        default="LOW",
        description="Severity tier: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'",
    )
    timestamp: str = Field(
        default_factory=lambda: datetime.now(timezone.utc).isoformat()
    )


class ESP32ListResponse(BaseModel):
    status: str = "ok"
    count: int
    devices: list[ESP32Device]
