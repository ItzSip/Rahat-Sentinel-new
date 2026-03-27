"""
ConvLSTM Model Architecture
============================
Convolutional LSTM for spatiotemporal disaster prediction
from satellite imagery sequences.
"""

import torch
import torch.nn as nn


class ConvLSTMCell(nn.Module):
    """Single ConvLSTM cell with convolutional gates."""

    def __init__(self, input_channels: int, hidden_channels: int, kernel_size: int = 3):
        super().__init__()
        self.hidden_channels = hidden_channels
        padding = kernel_size // 2

        self.gates = nn.Conv2d(
            in_channels=input_channels + hidden_channels,
            out_channels=4 * hidden_channels,  # i, f, o, g
            kernel_size=kernel_size,
            padding=padding,
            bias=True,
        )

    def forward(self, x, h_prev, c_prev):
        """
        Args:
            x: (B, C_in, H, W)
            h_prev: (B, C_hidden, H, W)
            c_prev: (B, C_hidden, H, W)
        """
        combined = torch.cat([x, h_prev], dim=1)
        gates = self.gates(combined)

        i, f, o, g = gates.chunk(4, dim=1)
        i = torch.sigmoid(i)
        f = torch.sigmoid(f)
        o = torch.sigmoid(o)
        g = torch.tanh(g)

        c_next = f * c_prev + i * g
        h_next = o * torch.tanh(c_next)

        return h_next, c_next


class ConvLSTM(nn.Module):
    """
    Multi-layer ConvLSTM for spatiotemporal prediction.

    Input:  (B, T, C, H, W) — batch of image sequences
    Output: (B, num_classes, H, W) — per-pixel risk prediction
    """

    def __init__(
        self,
        input_channels: int = 6,
        hidden_channels: list[int] = None,
        num_classes: int = 4,  # e.g., [safe, low, medium, high] risk
        kernel_size: int = 3,
    ):
        super().__init__()

        if hidden_channels is None:
            hidden_channels = [64, 64, 32]

        self.num_layers = len(hidden_channels)
        self.hidden_channels = hidden_channels

        # Build ConvLSTM layers
        self.cells = nn.ModuleList()
        for i in range(self.num_layers):
            in_ch = input_channels if i == 0 else hidden_channels[i - 1]
            self.cells.append(ConvLSTMCell(in_ch, hidden_channels[i], kernel_size))

        # Classification head
        self.classifier = nn.Sequential(
            nn.Conv2d(hidden_channels[-1], 16, kernel_size=1),
            nn.ReLU(inplace=True),
            nn.Conv2d(16, num_classes, kernel_size=1),
        )

    def forward(self, x):
        """
        Args:
            x: (B, T, C, H, W) — temporal sequence of satellite images

        Returns:
            (B, num_classes, H, W) — risk classification per pixel
        """
        B, T, C, H, W = x.shape
        device = x.device

        # Initialize hidden states
        h = [torch.zeros(B, ch, H, W, device=device) for ch in self.hidden_channels]
        c = [torch.zeros(B, ch, H, W, device=device) for ch in self.hidden_channels]

        # Process each timestep
        for t in range(T):
            input_t = x[:, t]
            for layer_idx, cell in enumerate(self.cells):
                h[layer_idx], c[layer_idx] = cell(input_t, h[layer_idx], c[layer_idx])
                input_t = h[layer_idx]

        # Use final hidden state for classification
        output = self.classifier(h[-1])
        return output


def build_model(input_channels: int = 6, num_classes: int = 4) -> ConvLSTM:
    """Factory function to create a ConvLSTM model."""
    model = ConvLSTM(
        input_channels=input_channels,
        hidden_channels=[64, 64, 32],
        num_classes=num_classes,
    )
    total_params = sum(p.numel() for p in model.parameters())
    print(f"🧠 ConvLSTM model created — {total_params:,} parameters")
    return model


if __name__ == "__main__":
    model = build_model()
    dummy_input = torch.randn(2, 10, 6, 64, 64)  # (B=2, T=10, C=6, H=64, W=64)
    output = model(dummy_input)
    print(f"Input:  {dummy_input.shape}")
    print(f"Output: {output.shape}")  # Expected: (2, 4, 64, 64)
