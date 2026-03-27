"""
Model Service
==============
Business logic for loading and running the Sentinel AI model.
"""

from datetime import datetime, timezone
from pathlib import Path


class ModelService:
    """Service for managing ML model predictions."""

    def __init__(self, model_path: str = None):
        self.model = None
        self.model_path = model_path
        self.model_loaded = False

    def load_model(self):
        """Load the trained ConvLSTM model."""
        try:
            # Import from sentinel-ai
            import sys
            sentinel_path = str(Path(__file__).parent.parent.parent.parent / "sentinel-ai")
            if sentinel_path not in sys.path:
                sys.path.insert(0, sentinel_path)

            from src.inference.predict import load_model

            self.model = load_model(self.model_path)
            self.model_loaded = True
            print("✅ Model loaded into service")
        except Exception as e:
            print(f"⚠️ Model loading failed: {e}")
            print("   Running in mock mode")

    async def predict(self, region: str, coordinates: dict) -> dict:
        """
        Run prediction for a given region.

        Args:
            region: Region identifier
            coordinates: Center coordinates {'lat': float, 'lon': float}

        Returns:
            Prediction result dict
        """
        if not self.model_loaded:
            # Return mock prediction when model isn't loaded
            return {
                "region": region,
                "risk_level": "medium",
                "confidence": 0.78,
                "risk_summary": {
                    "safe": 35.0,
                    "low": 30.0,
                    "medium": 25.0,
                    "high": 10.0,
                },
                "heatmap_url": None,
                "model_version": "mock-0.1.0",
                "timestamp": datetime.now(timezone.utc).isoformat(),
            }

        # TODO: Implement actual prediction pipeline:
        # 1. Fetch latest satellite data for coordinates
        # 2. Preprocess into tensor
        # 3. Run model inference
        # 4. Generate heatmap
        # 5. Return results

        raise NotImplementedError("Full prediction pipeline not yet implemented")
