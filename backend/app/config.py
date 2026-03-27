import os
from dotenv import load_dotenv

load_dotenv()

# System Config
DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "postgresql+asyncpg://postgres:postgres@localhost:5432/rahat_sentinel"
)

REDIS_URL = os.getenv(
    "REDIS_URL", 
    "redis://localhost:6379/0"
)

# Operational
DEMO_MODE = os.getenv("DEMO_MODE", "True").lower() in ["true", "1", "t"]
