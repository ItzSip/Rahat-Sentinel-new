"""
Data Preprocessing Module
==========================
Cleans, normalizes, and converts satellite data into tensors
for model training and inference.
"""

import numpy as np
from pathlib import Path


RAW_DIR = Path(__file__).parent.parent.parent / "data" / "raw"
PROCESSED_DIR = Path(__file__).parent.parent.parent / "data" / "processed"


def normalize_band(band: np.ndarray, min_val: float = 0, max_val: float = 10000) -> np.ndarray:
    """Normalize a satellite band to [0, 1] range."""
    return np.clip((band - min_val) / (max_val - min_val), 0, 1)


def compute_ndvi(nir: np.ndarray, red: np.ndarray) -> np.ndarray:
    """
    Compute Normalized Difference Vegetation Index.
    NDVI = (NIR - RED) / (NIR + RED)
    """
    denominator = nir + red
    denominator[denominator == 0] = 1e-10  # Avoid division by zero
    return (nir - red) / denominator


def compute_ndwi(green: np.ndarray, nir: np.ndarray) -> np.ndarray:
    """
    Compute Normalized Difference Water Index.
    NDWI = (GREEN - NIR) / (GREEN + NIR)
    """
    denominator = green + nir
    denominator[denominator == 0] = 1e-10
    return (green - nir) / denominator


def create_temporal_stack(images: list[np.ndarray], sequence_length: int = 10) -> np.ndarray:
    """
    Create temporal stacks for ConvLSTM input.

    Args:
        images: List of (C, H, W) arrays
        sequence_length: Number of timesteps per sample

    Returns:
        Array of shape (N, T, C, H, W)
    """
    if len(images) < sequence_length:
        raise ValueError(
            f"Need at least {sequence_length} images, got {len(images)}"
        )

    stacks = []
    for i in range(len(images) - sequence_length + 1):
        stack = np.stack(images[i : i + sequence_length], axis=0)
        stacks.append(stack)

    return np.array(stacks)


def preprocess_pipeline(raw_path: str, output_path: str = None):
    """
    Full preprocessing pipeline: load → normalize → compute indices → stack.

    Args:
        raw_path: Path to raw satellite data
        output_path: Path to save processed tensors
    """
    print(f"🔄 Preprocessing: {raw_path}")

    # TODO: Implement full pipeline based on data format
    # 1. Load raw data (GeoTIFF, NetCDF, etc.)
    # 2. Normalize bands
    # 3. Compute spectral indices (NDVI, NDWI)
    # 4. Create temporal stacks
    # 5. Save as .npy or .pt

    print("✅ Preprocessing complete")


if __name__ == "__main__":
    preprocess_pipeline(str(RAW_DIR))
