import json
import logging
import redis.asyncio as redis
from typing import Any, Dict
from app.config import REDIS_URL

CHANNEL_NAME = "sentinel:alerts"

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class RedisService:
    def __init__(self):
        self.redis_client = None

    async def connect(self):
        self.redis_client = redis.from_url(REDIS_URL)

    async def disconnect(self):
        if self.redis_client:
            await self.redis_client.aclose()

    async def publish_alert(self, payload: Dict[str, Any]):
        """
        Publishes a JSON payload to the sentinel:alerts channel.
        Warns if payload > 512 bytes and prints an explicit 'REDIS PUBLISHED' confirmation.
        """
        if self.redis_client:
            try:
                # Flat compact formatting
                message = json.dumps(payload, separators=(',', ':'))
                
                # Check for BLE constraints limit (<512b)
                if len(message) > 512:
                    logger.warning(f"Payload size > 512 bytes! Size: {len(message)}")
                    
                await self.redis_client.publish(CHANNEL_NAME, message)
                
                # Explicit hackathon log hook
                alert_id = payload.get("alert_id", "unknown")
                logger.info(f"REDIS PUBLISHED: alert_id={alert_id}")
            except Exception as e:
                logger.error(f"Failed to publish to Redis: {e}")

redis_service = RedisService()
