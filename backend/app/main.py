import time
import asyncio
from contextlib import asynccontextmanager
from fastapi import FastAPI, Depends, APIRouter, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import engine, Base, get_db
from app.routes import alerts, health, websockets
from app.services.redis_service import redis_service
from app.services.redis_subscriber import redis_listener
from app.schemas import AlertCreate, CompressedPayload
from app.services import alert_service

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Connect external stores
    await redis_service.connect()
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
        
    # Start the bridging task between Redis & WebSockets
    subscriber_task = asyncio.create_task(redis_listener())
    
    yield
    
    # Safely teardown all async workers
    subscriber_task.cancel()
    await redis_service.disconnect()
    await engine.dispose()

app = FastAPI(
    title="Rahat Sentinel",
    description="Real-time geospatial disaster response alert system.",
    version="4.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

root_router = APIRouter(tags=["Root Alerts"])

@root_router.post("/predict", status_code=201)
async def predict_root(alert_in: AlertCreate, db: AsyncSession = Depends(get_db)):
    return await alerts.create_alert(alert_in, db)

@root_router.get("/demo/trigger")
async def trigger_demo(background_tasks: BackgroundTasks, db: AsyncSession = Depends(get_db)):
    demo_alert = AlertCreate(
        region_id=999,
        risk_score=0.95,
        type="heatwave",
        district="Vidarbha",
        geojson={
            "type": "Polygon",
            "coordinates": [[[78.0, 20.0], [79.5, 20.0], [79.5, 21.5], [78.0, 21.5], [78.0, 20.0]]]
        }
    )
    background_tasks.add_task(alert_service.create_alert, db, demo_alert)
    return {"status": "success", "message": "Demo alert triggered successfully via background task."}

@root_router.get("/debug/redis-test")
async def debug_redis_test():
    payload = CompressedPayload(
        v=1,
        alert_id=999,
        region_id=999,
        severity=0.95,
        type="heatwave",
        timestamp=int(time.time()),
        ttl=21600,
        source="test"
    ).model_dump()
    
    await redis_service.publish_alert(payload)
    return payload

app.include_router(root_router)
app.include_router(health.router)
app.include_router(alerts.router)
app.include_router(websockets.router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
