from fastapi import APIRouter, Depends, Query, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from typing import List, Optional

from app.database import get_db
from app.schemas import AlertCreate, AlertResponse
from app.services import alert_service

router = APIRouter(prefix="/alerts", tags=["Alerts"])

@router.post("/predict", status_code=201)
async def create_alert(alert_in: AlertCreate, db: AsyncSession = Depends(get_db)):
    """
    Receives anomaly detection output from ML model, normalizes severity, deduplicates,
    stores geographically, and publishes the compressed version to Redis.
    """
    try:
        await alert_service.create_alert(db, alert_in)
        return {"status": "alert_created"}
    except Exception as e:
        # Gracefully handle any internal failures (like invalid geojson shapes in shapely logic)
        raise HTTPException(status_code=400, detail=str(e))

@router.get("", response_model=List[AlertResponse])
async def get_alerts(
    limit: int = Query(100, ge=1, le=1000), 
    severity: Optional[float] = Query(None, description="Minimum severity threshold"),
    db: AsyncSession = Depends(get_db)
):
    """ Fetch all alerts sorted by newest first """
    return await alert_service.get_alerts(db, limit=limit, severity=severity)

@router.get("/nearby", response_model=List[AlertResponse])
async def get_alerts_nearby(
    lat: float = Query(..., description="Latitude of target location"),
    lng: float = Query(..., description="Longitude of target location"),
    radius: float = Query(50000.0, description="Search radius in meters"),
    db: AsyncSession = Depends(get_db)
):
    """ Fetch alerts near a location """
    return await alert_service.get_alerts_nearby(db, lat=lat, lng=lng, radius=radius)
