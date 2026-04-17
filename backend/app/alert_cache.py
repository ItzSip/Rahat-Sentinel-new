"""
Thread-safe in-memory ring buffer for the last N alerts.

New WebSocket connections receive this cache so they are never
greeted with a blank screen.
"""

from __future__ import annotations

import asyncio
from collections import deque
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from .models import SentinelAlert

_MAX_SIZE = 5
_lock = asyncio.Lock()
_buffer: deque["SentinelAlert"] = deque(maxlen=_MAX_SIZE)


async def push_alert(alert: "SentinelAlert") -> None:
    """Append an alert. Oldest is evicted when the buffer is full."""
    async with _lock:
        _buffer.append(alert)


async def get_recent() -> list["SentinelAlert"]:
    """Return the most recent alerts (up to _MAX_SIZE), newest last."""
    async with _lock:
        return list(_buffer)


async def clear() -> None:
    """Flush the cache (useful for tests)."""
    async with _lock:
        _buffer.clear()
