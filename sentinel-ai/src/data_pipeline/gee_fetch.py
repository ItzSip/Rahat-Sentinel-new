"""
Google Earth Engine Data Fetching Module
========================================
Fetches satellite imagery (Sentinel-2, Landsat, etc.) via GEE API
for disaster prediction preprocessing.
"""

import os
from dotenv import load_dotenv

load_dotenv()


def initialize_gee():
    """Initialize Google Earth Engine with service account credentials."""
    try:
        import ee

        key_path = os.getenv("GEE_KEY_PATH", "./keys/gee-key.json")
        service_account = os.getenv("GEE_SERVICE_ACCOUNT", "")

        if service_account and os.path.exists(key_path):
            credentials = ee.ServiceAccountCredentials(service_account, key_path)
            ee.Initialize(credentials)
        else:
            ee.Authenticate()
            ee.Initialize()

        print("✅ GEE initialized successfully")
        return True
    except Exception as e:
        print(f"❌ GEE initialization failed: {e}")
        return False


def fetch_sentinel2(region: dict, start_date: str, end_date: str):
    """
    Fetch Sentinel-2 imagery for a given region and date range.

    Args:
        region: GeoJSON-like dict defining the area of interest
        start_date: Start date string (YYYY-MM-DD)
        end_date: End date string (YYYY-MM-DD)

    Returns:
        ee.ImageCollection filtered and processed
    """
    import ee

    collection = (
        ee.ImageCollection("COPERNICUS/S2_SR_HARMONIZED")
        .filterBounds(ee.Geometry(region))
        .filterDate(start_date, end_date)
        .filter(ee.Filter.lt("CLOUDY_PIXEL_PERCENTAGE", 20))
        .select(["B2", "B3", "B4", "B8", "B11", "B12"])  # RGB + NIR + SWIR
    )

    print(f"📡 Found {collection.size().getInfo()} images")
    return collection


def export_to_drive(image, description: str, region: dict, scale: int = 10):
    """Export a processed image to Google Drive."""
    import ee

    task = ee.batch.Export.image.toDrive(
        image=image,
        description=description,
        region=ee.Geometry(region),
        scale=scale,
        maxPixels=1e13,
    )
    task.start()
    print(f"📤 Export task started: {description}")
    return task


if __name__ == "__main__":
    initialize_gee()
    print("🛰️ GEE fetch module ready")
