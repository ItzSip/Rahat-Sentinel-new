"""
BLE binary codec — packs / unpacks a SentinelAlert into a ≤512-byte payload.

Schema (47 bytes fixed):
  ┌──────────────┬───────┬────────────┐
  │ Field        │ Bytes │ Format     │
  ├──────────────┼───────┼────────────┤
  │ Region ID    │ 2     │ uint16 BE  │
  │ Severity     │ 1     │ uint8      │
  │ Timestamp    │ 4     │ uint32 BE  │
  │ GradCAM Hash │ 8     │ raw bytes  │
  │ District     │ 32    │ UTF-8 pad  │
  └──────────────┴───────┴────────────┘
  Total: 47 bytes  (well within BLE 512-byte limit)
"""

from __future__ import annotations

import struct
from datetime import datetime, timezone
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from .models import SentinelAlert

# Binary layout constants
HEADER_FMT = ">HBI"  # uint16 region_id, uint8 severity, uint32 timestamp
HEADER_SIZE = struct.calcsize(HEADER_FMT)  # 7
HASH_SIZE = 8
DISTRICT_SIZE = 32
PAYLOAD_SIZE = HEADER_SIZE + HASH_SIZE + DISTRICT_SIZE  # 47


def pack_alert(alert: "SentinelAlert") -> bytes:
    """
    Compress a SentinelAlert into a fixed-size binary payload for BLE transmission.

    Returns:
        bytes of length PAYLOAD_SIZE (47).
    """
    # Parse ISO timestamp → epoch seconds (uint32)
    try:
        dt = datetime.fromisoformat(alert.timestamp)
    except (ValueError, TypeError):
        dt = datetime.now(timezone.utc)
    epoch = int(dt.timestamp()) & 0xFFFFFFFF  # clamp to 32-bit

    # Pack header
    header = struct.pack(HEADER_FMT, alert.region_id, alert.severity, epoch)

    # GradCAM hash: take first 8 bytes of hex string decoded, or zero-pad
    hash_hex = (alert.gradcam_hash or "").ljust(16, "0")[:16]
    hash_bytes = bytes.fromhex(hash_hex)

    # District name: UTF-8, truncate/pad to 32 bytes
    district_bytes = alert.district.encode("utf-8")[:DISTRICT_SIZE]
    district_bytes = district_bytes.ljust(DISTRICT_SIZE, b"\x00")

    return header + hash_bytes + district_bytes


def unpack_alert(data: bytes) -> dict:
    """
    Decode a binary BLE payload back into a dict.

    Returns:
        dict with keys: region_id, severity, timestamp, gradcam_hash, district.
    """
    if len(data) < PAYLOAD_SIZE:
        raise ValueError(
            f"Payload too short: got {len(data)} bytes, need {PAYLOAD_SIZE}"
        )

    region_id, severity, epoch = struct.unpack_from(HEADER_FMT, data, 0)

    offset = HEADER_SIZE
    hash_bytes = data[offset : offset + HASH_SIZE]
    offset += HASH_SIZE

    district_bytes = data[offset : offset + DISTRICT_SIZE]
    district = district_bytes.rstrip(b"\x00").decode("utf-8", errors="replace")

    ts = datetime.fromtimestamp(epoch, tz=timezone.utc).isoformat()

    return {
        "region_id": region_id,
        "severity": severity,
        "timestamp": ts,
        "gradcam_hash": hash_bytes.hex(),
        "district": district,
    }
