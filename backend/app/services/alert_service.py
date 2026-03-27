import json
import time
import logging
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from sqlalchemy import cast, Float, func, and_

from geoalchemy2 import Geometry
from geoalchemy2.functions import ST_AsGeoJSON, ST_DWithin, ST_MakePoint, ST_SetSRID

from app.models import Alert
from app.schemas import AlertCreate, AlertResponse, CompressedPayload
from app.utils.geo_utils import convert_geojson_to_geom, hash_geojson
from app.services.redis_service import redis_service

logger = logging.getLogger(__name__)

async def create_alert(db: AsyncSession, alert_data: AlertCreate) -> Alert:
    # Ensure standard UNIX seconds (int)
    current_time = int(time.time())
    
    stmt = select(Alert).where(
        and_(
            Alert.region_id == alert_data.region_id,
            Alert.timestamp >= current_time - 7200
        )
    )
    result = await db.execute(stmt)
    existing_alert = result.scalar_one_or_none()

    geojson_dict = alert_data.geojson.model_dump()
    geom = convert_geojson_to_geom(geojson_dict)
    poly_hash = hash_geojson(geojson_dict)
    
    ttl = 21600

    if existing_alert:
        existing_alert.severity = alert_data.severity
        existing_alert.alert_type = alert_data.alert_type
        existing_alert.district = alert_data.district
        existing_alert.timestamp = current_time
        existing_alert.polygon_hash = poly_hash
        existing_alert.geom = geom
        existing_alert.ttl = ttl
        db_alert = existing_alert
    else:
        db_alert = Alert(
            region_id=alert_data.region_id,
            severity=alert_data.severity,
            alert_type=alert_data.alert_type,
            district=alert_data.district,
            timestamp=current_time,
            ttl=ttl, # Default 21600
            polygon_hash=poly_hash,
            geom=geom
        )
        db.add(db_alert)
        
    await db.commit()
    await db.refresh(db_alert)

    # Standardized flat logging hooks for demonstration
    logger.info(f"ALERT CREATED: region_id={db_alert.region_id} severity={db_alert.severity}")

    # Explicit, exact matching structure
    compressed_payload = CompressedPayload(
        v=1,
        alert_id=db_alert.alert_id,
        region_id=db_alert.region_id,
        severity=db_alert.severity,
        type=db_alert.alert_type,
        timestamp=db_alert.timestamp,
        ttl=db_alert.ttl,
        source="cloud"
    )
    
    await redis_service.publish_alert(compressed_payload.model_dump())

    return db_alert

async def get_alerts(db: AsyncSession, limit: int = 100, severity: float = None):
    stmt = select(Alert, ST_AsGeoJSON(Alert.geom).label("geojson_str")).order_by(Alert.created_at.desc())
    
    if severity is not None:
        stmt = stmt.where(Alert.severity >= severity)
    
    if limit is not None:
        stmt = stmt.limit(limit)
        
    result = await db.execute(stmt)
    rows = result.all()
    
    alerts = []
    for row in rows:
        alert_obj, geo_json_str = row
        r = AlertResponse.model_validate(alert_obj)
        r.geojson = json.loads(geo_json_str) if geo_json_str else None
        alerts.append(r)
        
    return alerts

async def get_alerts_nearby(db: AsyncSession, lat: float, lng: float, radius: float = 50000.0):
    target_point = ST_SetSRID(ST_MakePoint(lng, lat), 4326)
    
    from geoalchemy2.types import Geography
    stmt = (
        select(Alert, ST_AsGeoJSON(Alert.geom).label("geojson_str"))
        .where(ST_DWithin(
            cast(Alert.geom, Geography),
            cast(target_point, Geography),
            radius
        ))
        .order_by(Alert.created_at.desc())
        .limit(100)
    )

    result = await db.execute(stmt)
    rows = result.all()
    
    alerts = []
    for row in rows:
        alert_obj, geo_json_str = row
        r = AlertResponse.model_validate(alert_obj)
        r.geojson = json.loads(geo_json_str) if geo_json_str else None
        alerts.append(r)
        
    return alerts
