import json
import hashlib
from shapely.geometry import shape
from geoalchemy2.shape import from_shape

def convert_geojson_to_geom(geojson_dict: dict):
    """
    Converts a GeoJSON dictionary into a GeoAlchemy2 WKBElement
    suitable for inserting into a PostGIS geometry column.
    """
    geom = shape(geojson_dict)
    return from_shape(geom, srid=4326)

def hash_geojson(geojson_dict: dict) -> str:
    """
    Generates a consistent 16-character SHA256 hash for a GeoJSON object 
    so it doesn't have to be sent over BLE continually.
    """
    # Sort keys for consistent hashing
    json_str = json.dumps(geojson_dict, sort_keys=True)
    return hashlib.sha256(json_str.encode('utf-8')).hexdigest()[:16]
