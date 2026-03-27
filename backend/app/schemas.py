from typing import Any, Dict, Optional
from pydantic import BaseModel, Field, field_validator
from datetime import datetime

class GeoJSONPolygon(BaseModel):
    type: str = Field(..., pattern="^Polygon$")
    coordinates: list[list[list[float]]]

class AlertCreate(BaseModel):
    region_id: int
    severity: float = Field(..., alias="risk_score")
    alert_type: str = Field(..., alias="type")
    district: str
    geojson: GeoJSONPolygon

    @field_validator('severity', mode='before')
    @classmethod
    def normalize_severity(cls, v: float) -> float:
        if v > 1.0:
            return v / 10.0
        return v

    class Config:
        populate_by_name = True
        
class CompressedPayload(BaseModel):
    v: int = 1
    alert_id: int
    region_id: int
    severity: float
    type: str
    timestamp: int
    ttl: int = 21600
    source: str

class AlertResponse(BaseModel):
    alert_id: int
    region_id: int
    severity: float
    alert_type: str
    district: str
    polygon_hash: str
    timestamp: int
    created_at: datetime
    updated_at: datetime
    geojson: Optional[Dict[str, Any]] = None

    class Config:
        from_attributes = True

class HealthCheck(BaseModel):
    status: str
