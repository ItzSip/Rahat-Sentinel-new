"""
Inference / Prediction Module
==============================
Load trained ConvLSTM model and run predictions on new satellite data.
"""

import torch
import numpy as np
from pathlib import Path

from ..models.convlstm import build_model


MODEL_DIR = Path(__file__).parent.parent.parent / "outputs" / "models"
HEATMAP_DIR = Path(__file__).parent.parent.parent / "outputs" / "heatmaps"

# Risk level labels
RISK_LABELS = {0: "safe", 1: "low", 2: "medium", 3: "high"}


def load_model(checkpoint_path: str = None, device: str = "cpu") -> torch.nn.Module:
    """
    Load a trained ConvLSTM model from checkpoint.

    Args:
        checkpoint_path: Path to .pt checkpoint file
        device: Device to load model on

    Returns:
        Loaded model in eval mode
    """
    if checkpoint_path is None:
        checkpoint_path = str(MODEL_DIR / "latest.pt")

    model = build_model()
    checkpoint = torch.load(checkpoint_path, map_location=device, weights_only=True)
    model.load_state_dict(checkpoint["model_state_dict"])
    model.eval()

    print(f"✅ Model loaded from: {checkpoint_path}")
    print(f"   Epoch: {checkpoint.get('epoch', 'N/A')}")
    print(f"   Loss:  {checkpoint.get('loss', 'N/A')}")

    return model


def predict(model: torch.nn.Module, input_tensor: torch.Tensor) -> dict:
    """
    Run prediction on input satellite sequence.

    Args:
        model: Trained ConvLSTM model
        input_tensor: (1, T, C, H, W) tensor

    Returns:
        dict with predictions, risk_map, and confidence
    """
    device = next(model.parameters()).device
    input_tensor = input_tensor.to(device)

    with torch.no_grad():
        output = model(input_tensor)  # (1, num_classes, H, W)
        probabilities = torch.softmax(output, dim=1)
        risk_map = torch.argmax(probabilities, dim=1).squeeze(0)  # (H, W)
        confidence = probabilities.max(dim=1).values.squeeze(0)   # (H, W)

    return {
        "risk_map": risk_map.cpu().numpy(),
        "confidence": confidence.cpu().numpy(),
        "probabilities": probabilities.cpu().numpy(),
        "risk_summary": _summarize_risk(risk_map.cpu().numpy()),
    }


def _summarize_risk(risk_map: np.ndarray) -> dict:
    """Generate a summary of risk distribution."""
    total_pixels = risk_map.size
    summary = {}
    for level, label in RISK_LABELS.items():
        count = np.sum(risk_map == level)
        summary[label] = {
            "pixels": int(count),
            "percentage": round(float(count / total_pixels * 100), 2),
        }
    return summary


def generate_heatmap(risk_map: np.ndarray, output_path: str = None) -> str:
    """
    Generate a heatmap visualization from the risk map.

    Args:
        risk_map: (H, W) array of risk levels
        output_path: Where to save the heatmap image

    Returns:
        Path to saved heatmap
    """
    try:
        import matplotlib.pyplot as plt
        from matplotlib.colors import ListedColormap

        colors = ["#2ecc71", "#f1c40f", "#e67e22", "#e74c3c"]  # safe → high
        cmap = ListedColormap(colors)

        fig, ax = plt.subplots(1, 1, figsize=(10, 10))
        im = ax.imshow(risk_map, cmap=cmap, vmin=0, vmax=3)
        plt.colorbar(im, ax=ax, ticks=[0, 1, 2, 3], label="Risk Level")
        ax.set_title("Disaster Risk Heatmap")
        ax.axis("off")

        if output_path is None:
            HEATMAP_DIR.mkdir(parents=True, exist_ok=True)
            output_path = str(HEATMAP_DIR / "latest_heatmap.png")

        plt.savefig(output_path, dpi=150, bbox_inches="tight")
        plt.close()
        print(f"🗺️ Heatmap saved: {output_path}")
        return output_path

    except ImportError:
        print("⚠️ matplotlib not installed, skipping heatmap generation")
        return None


if __name__ == "__main__":
    print("🔮 Prediction module ready")
    print("Usage: from sentinel_ai.src.inference.predict import predict, load_model")
