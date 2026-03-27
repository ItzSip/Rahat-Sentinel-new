"""
Unit tests for the BLE binary codec (pack ↔ unpack round-trip).

Run:  python -m pytest app/test_ble_codec.py -v
"""

from datetime import datetime, timezone

from app.ble_codec import PAYLOAD_SIZE, pack_alert, unpack_alert
from app.models import SentinelAlert


def _make_alert(**overrides) -> SentinelAlert:
    """Helper: create a minimal valid SentinelAlert."""
    defaults = {
        "risk_score": 0.85,
        "cells": [[0.0] * 20 for _ in range(20)],
        "district": "Bhilai/Durg",
        "region_id": 42,
        "severity": 217,
        "gradcam_hash": "abcdef0123456789",
        "timestamp": datetime(2025, 5, 15, 12, 0, 0, tzinfo=timezone.utc).isoformat(),
    }
    defaults.update(overrides)
    return SentinelAlert(**defaults)


class TestBLECodec:
    """Round-trip and edge-case tests."""

    def test_payload_size(self) -> None:
        alert = _make_alert()
        binary = pack_alert(alert)
        assert len(binary) == PAYLOAD_SIZE
        assert len(binary) <= 512, "Must fit within BLE advertisement limit"

    def test_round_trip_fields(self) -> None:
        alert = _make_alert(
            region_id=7,
            severity=200,
            district="Durg",
            gradcam_hash="1122334455667788",
        )
        binary = pack_alert(alert)
        decoded = unpack_alert(binary)

        assert decoded["region_id"] == 7
        assert decoded["severity"] == 200
        assert decoded["district"] == "Durg"
        assert decoded["gradcam_hash"] == "1122334455667788"

    def test_district_truncation(self) -> None:
        long_name = "A" * 100  # exceeds 32-byte limit
        alert = _make_alert(district=long_name)
        binary = pack_alert(alert)
        decoded = unpack_alert(binary)
        assert len(decoded["district"]) <= 32

    def test_timestamp_roundtrip(self) -> None:
        ts = datetime(2024, 8, 1, 6, 30, 0, tzinfo=timezone.utc)
        alert = _make_alert(timestamp=ts.isoformat())
        binary = pack_alert(alert)
        decoded = unpack_alert(binary)
        decoded_dt = datetime.fromisoformat(decoded["timestamp"])
        assert decoded_dt == ts

    def test_short_payload_raises(self) -> None:
        import pytest

        with pytest.raises(ValueError, match="too short"):
            unpack_alert(b"\x00" * 10)

    def test_zero_region_and_severity(self) -> None:
        alert = _make_alert(region_id=0, severity=0)
        decoded = unpack_alert(pack_alert(alert))
        assert decoded["region_id"] == 0
        assert decoded["severity"] == 0

    def test_max_region_and_severity(self) -> None:
        alert = _make_alert(region_id=65535, severity=255)
        decoded = unpack_alert(pack_alert(alert))
        assert decoded["region_id"] == 65535
        assert decoded["severity"] == 255
