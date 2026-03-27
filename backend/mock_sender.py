#!/usr/bin/env python3
"""
Mock alert sender — simulates the ML inference pipeline by publishing
realistic SentinelAlert payloads to the Redis ``sentinel:alerts`` channel.

Usage:
    python mock_sender.py                      # single alert
    python mock_sender.py --count 5            # 5 alerts, 2s apart
    python mock_sender.py --count 0 --interval 3  # infinite, every 3s
"""

from __future__ import annotations

import argparse
import json
import os
import random
import sys
import time
import uuid
from datetime import datetime, timezone

# Add parent to path so we can import app.models when running standalone
sys.path.insert(0, os.path.dirname(__file__))

import redis  # type: ignore

REDIS_URL = os.getenv("REDIS_URL", "redis://localhost:6379")
CHANNEL = "sentinel:alerts"

# Bhilai / Durg region defaults
DEFAULT_CENTER = {"lat": 21.19, "lon": 81.35}

DISTRICTS = [
    "Bhilai/Durg",
    "Durg",
    "Bhilai",
    "Rajnandgaon",
    "Balod",
    "Bemetara",
]


def generate_mock_alert() -> dict:
    """Build a realistic SentinelAlert payload dict."""
    risk = round(random.uniform(0.45, 0.99), 3)
    severity = int(risk * 255)

    # 20x20 risk grid — hotter cluster in a random quadrant
    cells = [[0.0] * 20 for _ in range(20)]
    hot_r = random.randint(0, 14)
    hot_c = random.randint(0, 14)
    for r in range(20):
        for c in range(20):
            # Base noise
            base = round(random.uniform(0.0, 0.3), 3)
            # Hotspot cluster (6x6 block)
            if hot_r <= r < hot_r + 6 and hot_c <= c < hot_c + 6:
                base = round(random.uniform(0.6, 1.0), 3)
            cells[r][c] = base

    return {
        "id": uuid.uuid4().hex[:12],
        "risk_score": risk,
        "cells": cells,
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "district": random.choice(DISTRICTS),
        "region_id": random.randint(1, 10),
        "severity": severity,
        "gradcam_hash": uuid.uuid4().hex[:16],
        "center": {
            "lat": DEFAULT_CENTER["lat"] + random.uniform(-0.3, 0.3),
            "lon": DEFAULT_CENTER["lon"] + random.uniform(-0.3, 0.3),
        },
    }


def main() -> None:
    parser = argparse.ArgumentParser(description="Mock Sentinel alert sender")
    parser.add_argument(
        "--count",
        type=int,
        default=1,
        help="Number of alerts to send (0 = infinite). Default: 1",
    )
    parser.add_argument(
        "--interval",
        type=float,
        default=2.0,
        help="Seconds between alerts. Default: 2.0",
    )
    parser.add_argument(
        "--redis-url",
        type=str,
        default=REDIS_URL,
        help=f"Redis URL. Default: {REDIS_URL}",
    )
    args = parser.parse_args()

    client = redis.from_url(args.redis_url, decode_responses=True)

    # Verify Redis is reachable
    try:
        client.ping()
    except redis.ConnectionError as exc:
        print(f"[ERROR] Cannot connect to Redis at {args.redis_url}: {exc}")
        sys.exit(1)

    print(f"[mock_sender] Connected to Redis at {args.redis_url}")
    print(f"[mock_sender] Publishing to channel: {CHANNEL}")
    print(f"[mock_sender] Count: {'∞' if args.count == 0 else args.count}  Interval: {args.interval}s")
    print("-" * 60)

    sent = 0
    try:
        while True:
            alert = generate_mock_alert()
            payload = json.dumps(alert)
            listeners = client.publish(CHANNEL, payload)

            sent += 1
            print(
                f"[{sent}] Published alert id={alert['id']}  "
                f"risk={alert['risk_score']:.3f}  "
                f"district={alert['district']}  "
                f"listeners={listeners}"
            )

            if args.count > 0 and sent >= args.count:
                break

            time.sleep(args.interval)

    except KeyboardInterrupt:
        print("\n[mock_sender] Interrupted.")

    print(f"[mock_sender] Done — sent {sent} alert(s).")


if __name__ == "__main__":
    main()
