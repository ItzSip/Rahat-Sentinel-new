"""
Pydantic Schemas for Alerts
=============================
Request/response models for the alert API.
"""

from pydantic import BaseModel, Field
from typing import Optional


class AlertResponse(BaseModel):
    """Response model for a disaster alert."""

    id: str
    region: str
    risk_level: str = Field(..., pattern="^(safe|low|medium|high)$")
    confidence: float = Field(..., ge=0, le=1)
    message: str
    timestamp: str
    coordinates: dict = Field(..., description="{'lat': float, 'lon': float}")


class PredictionRequest(BaseModel):
    """Request model for triggering a prediction."""

    region: str = Field(..., description="Region name or identifier")
    coordinates: dict = Field(
        ..., description="{'lat': float, 'lon': float} center of AOI"
    )
    radius_km: float = Field(default=50.0, description="Radius of area to analyze")
    use_latest_data: bool = Field(
        default=True, description="Whether to fetch latest satellite data"
    )


class PredictionResponse(BaseModel):
    """Response model for prediction results."""

    region: str
    risk_level: str
    confidence: float
    risk_summary: dict = Field(
        ..., description="Percentage breakdown by risk level"
    )
    heatmap_url: Optional[str] = None
    model_version: str = "0.1.0"
    timestamp: str
