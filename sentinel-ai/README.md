# 🔥 Sentinel AI — Disaster Prediction Engine

> ML pipeline for satellite-based disaster prediction using ConvLSTM and Earth observation data.

## Architecture

```
sentinel-ai/
├── data/
│   ├── raw/              ← Raw satellite data (GEE exports, Sentinel-2, etc.)
│   └── processed/        ← Cleaned tensors, normalized arrays
├── notebooks/            ← Jupyter experiments & EDA
├── src/
│   ├── data_pipeline/    ← Data fetching & preprocessing
│   ├── models/           ← Model architectures & training
│   ├── inference/        ← Prediction & serving logic
│   └── utils/            ← Shared utilities
├── outputs/
│   ├── heatmaps/         ← Generated risk heatmaps
│   └── models/           ← Saved model checkpoints (.pt, .h5)
└── tests/                ← Unit & integration tests
```

## Quick Start

```bash
# Create virtual environment
python -m venv venv
source venv/bin/activate  # or venv\Scripts\activate on Windows

# Install dependencies
pip install -r requirements.txt

# Run data pipeline
python -m src.data_pipeline.gee_fetch

# Train model
python -m src.models.train

# Run inference
python -m src.inference.predict
```

## Data Flow

```
Satellite (GEE/Sentinel-2) → data_pipeline → models → inference → backend-api
```

## Environment Variables

Create a `.env` file:
```env
GEE_SERVICE_ACCOUNT=your-service-account@project.iam.gserviceaccount.com
GEE_KEY_PATH=./keys/gee-key.json
MODEL_OUTPUT_DIR=./outputs/models
```
