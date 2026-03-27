from datetime import datetime
from sqlalchemy import Column, Integer, String, Float, DateTime, Index
from geoalchemy2 import Geometry
from sqlalchemy.orm import declarative_base

Base = declarative_base()

class Alert(Base):
    __tablename__ = "alerts"

    alert_id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    region_id = Column(Integer, index=True, nullable=False)
    severity = Column(Float, nullable=False)
    alert_type = Column(String, index=True, nullable=False)
    district = Column(String, index=True, nullable=False)
    
    # New fields for demo/production readiness
    timestamp = Column(Integer, nullable=False) # UNIX timestamp
    ttl = Column(Integer, default=21600, nullable=False)
    polygon_hash = Column(String(16), index=True, nullable=False)
    
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)
    
    # PostGIS geometry
    geom = Column(Geometry(geometry_type='POLYGON', srid=4326, spatial_index=True), nullable=False)

    __table_args__ = (
        Index("idx_alert_time", created_at.desc()),
    )
