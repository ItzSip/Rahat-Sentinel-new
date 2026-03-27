"""
Shared Schemas
===============
Common data structures used across sentinel-ai and backend-api.
Keeps both modules in sync without tight coupling.
"""

from enum import Enum
from dataclasses import dataclass, field
from typing import Optional


class RiskLevel(str, Enum):
    """Disaster risk levels."""
    SAFE = "safe"
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"


@dataclass
class Coordinates:
    """Geographic coordinates."""
    lat: float
    lon: float

    def to_dict(self) -> dict:
        return {"lat": self.lat, "lon": self.lon}


@dataclass
class AlertData:
    """Shared alert data structure."""
    id: str
    region: str
    risk_level: RiskLevel
    confidence: float
    message: str
    timestamp: str
    coordinates: Coordinates
    heatmap_url: Optional[str] = None
    metadata: dict = field(default_factory=dict)


@dataclass
class PredictionResult:
    """Shared prediction result structure."""
    region: str
    risk_level: RiskLevel
    confidence: float
    risk_summary: dict  # {RiskLevel: percentage}
    model_version: str = "0.1.0"
    heatmap_path: Optional[str] = None
