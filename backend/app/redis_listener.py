"""
Async Redis Pub/Sub listener for the ``sentinel:alerts`` channel.

Runs as a background task inside FastAPI's lifespan.  On each incoming
message it deserialises the JSON payload into a SentinelAlert, pushes it
to the in-memory cache, and broadcasts it to every connected WebSocket
client via the ConnectionManager reference injected at startup.
"""

from __future__ import annotations

import asyncio
import json
import logging
from typing import TYPE_CHECKING, Callable, Coroutine

import redis.asyncio as aioredis

from .models import SentinelAlert
from . import alert_cache

if TYPE_CHECKING:
    pass

logger = logging.getLogger("sentinel.redis")

CHANNEL = "sentinel:alerts"


async def start_listener(
    redis_url: str,
    on_alert: Callable[[SentinelAlert], Coroutine],
) -> None:
    """
    Subscribe to Redis and loop forever, calling *on_alert* for every
    valid message.

    Parameters
    ----------
    redis_url:
        Redis connection string, e.g. ``redis://localhost:6379``.
    on_alert:
        Async callback invoked with a validated SentinelAlert.
        Typically this is ``ConnectionManager.broadcast``.
    """
    while True:
        try:
            client = aioredis.from_url(redis_url, decode_responses=True)
            pubsub = client.pubsub()
            await pubsub.subscribe(CHANNEL)
            logger.info("Subscribed to Redis channel: %s", CHANNEL)

            async for message in pubsub.listen():
                if message["type"] != "message":
                    continue

                raw = message["data"]
                try:
                    payload = json.loads(raw)
                    alert = SentinelAlert(**payload)
                except (json.JSONDecodeError, Exception) as exc:
                    logger.warning("Invalid alert payload: %s — %s", exc, raw[:200])
                    continue

                # Push to in-memory cache
                await alert_cache.push_alert(alert)

                # Fan out to all WebSocket clients
                await on_alert(alert)

                logger.info(
                    "Alert dispatched — id=%s risk=%.2f district=%s",
                    alert.id,
                    alert.risk_score,
                    alert.district,
                )

        except aioredis.ConnectionError as exc:
            logger.error("Redis connection lost: %s — reconnecting in 3s", exc)
            await asyncio.sleep(3)
        except asyncio.CancelledError:
            logger.info("Redis listener shutting down")
            break
        except Exception as exc:
            logger.error("Unexpected error in Redis listener: %s", exc)
            await asyncio.sleep(3)
