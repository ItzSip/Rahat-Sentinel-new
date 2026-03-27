"""
Model Training Script
======================
Training loop, validation, checkpointing for ConvLSTM disaster model.
"""

import os
import torch
import torch.nn as nn
from pathlib import Path
from datetime import datetime

from .convlstm import build_model


# Paths
OUTPUT_DIR = Path(__file__).parent.parent.parent / "outputs" / "models"


def get_device():
    """Get the best available device."""
    if torch.cuda.is_available():
        return torch.device("cuda")
    elif hasattr(torch.backends, "mps") and torch.backends.mps.is_available():
        return torch.device("mps")
    return torch.device("cpu")


def train_one_epoch(model, dataloader, criterion, optimizer, device):
    """Train for one epoch."""
    model.train()
    total_loss = 0.0
    num_batches = 0

    for batch_idx, (inputs, targets) in enumerate(dataloader):
        inputs = inputs.to(device)
        targets = targets.to(device)

        optimizer.zero_grad()
        outputs = model(inputs)
        loss = criterion(outputs, targets)
        loss.backward()
        optimizer.step()

        total_loss += loss.item()
        num_batches += 1

        if batch_idx % 10 == 0:
            print(f"  Batch {batch_idx}: loss = {loss.item():.4f}")

    return total_loss / max(num_batches, 1)


def validate(model, dataloader, criterion, device):
    """Validate the model."""
    model.eval()
    total_loss = 0.0
    num_batches = 0

    with torch.no_grad():
        for inputs, targets in dataloader:
            inputs = inputs.to(device)
            targets = targets.to(device)

            outputs = model(inputs)
            loss = criterion(outputs, targets)

            total_loss += loss.item()
            num_batches += 1

    return total_loss / max(num_batches, 1)


def save_checkpoint(model, optimizer, epoch, loss, path):
    """Save model checkpoint."""
    os.makedirs(os.path.dirname(path), exist_ok=True)
    torch.save(
        {
            "epoch": epoch,
            "model_state_dict": model.state_dict(),
            "optimizer_state_dict": optimizer.state_dict(),
            "loss": loss,
            "timestamp": datetime.now().isoformat(),
        },
        path,
    )
    print(f"💾 Checkpoint saved: {path}")


def train(
    train_loader,
    val_loader=None,
    epochs: int = 50,
    lr: float = 1e-3,
    input_channels: int = 6,
    num_classes: int = 4,
):
    """
    Full training pipeline.

    Args:
        train_loader: Training DataLoader
        val_loader: Validation DataLoader (optional)
        epochs: Number of training epochs
        lr: Learning rate
        input_channels: Number of input bands
        num_classes: Number of risk categories
    """
    device = get_device()
    print(f"🖥️ Using device: {device}")

    model = build_model(input_channels, num_classes).to(device)
    criterion = nn.CrossEntropyLoss()
    optimizer = torch.optim.Adam(model.parameters(), lr=lr)
    scheduler = torch.optim.lr_scheduler.ReduceLROnPlateau(
        optimizer, mode="min", patience=5, factor=0.5
    )

    best_val_loss = float("inf")

    for epoch in range(1, epochs + 1):
        print(f"\n{'='*50}")
        print(f"Epoch {epoch}/{epochs}")
        print(f"{'='*50}")

        train_loss = train_one_epoch(model, train_loader, criterion, optimizer, device)
        print(f"📊 Train Loss: {train_loss:.4f}")

        if val_loader:
            val_loss = validate(model, val_loader, criterion, device)
            print(f"📊 Val Loss:   {val_loss:.4f}")
            scheduler.step(val_loss)

            if val_loss < best_val_loss:
                best_val_loss = val_loss
                save_checkpoint(
                    model, optimizer, epoch, val_loss,
                    str(OUTPUT_DIR / "best_model.pt"),
                )

    # Save final model
    save_checkpoint(
        model, optimizer, epochs, train_loss,
        str(OUTPUT_DIR / "latest.pt"),
    )

    print(f"\n✅ Training complete! Best val loss: {best_val_loss:.4f}")
    return model


if __name__ == "__main__":
    print("⚠️ Run with actual DataLoaders. This is the training entry point.")
    print("Usage: from sentinel_ai.src.models.train import train")
