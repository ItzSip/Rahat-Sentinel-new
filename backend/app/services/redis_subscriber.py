import asyncio
import logging
import redis.asyncio as redis
from app.services.websocket_manager import manager
from app.services.redis_service import REDIS_URL, CHANNEL_NAME

logger = logging.getLogger(__name__)

async def redis_listener():
    """
    Background worker that continually listens to the sentinel:alerts channel
    and bridges any incoming telemetry to the WebSocket manager.
    Auto-verifies connection health and reconnects natively.
    """
    while True:
        try:
            r = redis.from_url(REDIS_URL, decode_responses=True)
            async with r.pubsub() as pubsub:
                await pubsub.subscribe(CHANNEL_NAME)
                logger.info(f"Subscribed to Redis channel: {CHANNEL_NAME} for WS Broadcast")
                
                async for message in pubsub.listen():
                    if message["type"] == "message":
                        payload = message["data"]
                        logger.info(f"Redis Subscriber received payload for WS broadcast. Forwarding...")
                        await manager.broadcast(payload)
                        
        except asyncio.CancelledError:
            # Lifecycle shutdown hook safely exits the loop
            logger.info("Redis listener task cancelled.")
            break
        except Exception as e:
            logger.error(f"Redis Subscriber error: {e}. Reconnecting in 5s...")
            await asyncio.sleep(5)
