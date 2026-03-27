# 🌐 Backend API — Bridge Layer

> FastAPI service bridging Sentinel AI predictions with the Rahat mobile app.

## Architecture

```
backend-api/
├── app/
│   ├── main.py           ← FastAPI entry point
│   ├── routes/           ← API endpoint definitions
│   ├── services/         ← Business logic & model integration
│   ├── schemas/          ← Pydantic request/response models
│   └── middleware/       ← Auth, CORS, rate limiting
└── tests/                ← API tests
```

## Quick Start

```bash
# Create virtual environment
python -m venv venv
source venv/bin/activate  # or venv\Scripts\activate on Windows

# Install dependencies
pip install -r requirements.txt

# Run development server
uvicorn app.main:app --reload --port 8000
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| GET | `/api/v1/alerts` | Get active disaster alerts |
| POST | `/api/v1/alerts/predict` | Trigger prediction for a region |
| GET | `/api/v1/heatmap/{region}` | Get risk heatmap data |
| WS | `/ws/alerts` | Real-time alert stream |

## Data Flow

```
sentinel-ai → backend-api → mobile-app → BLE mesh
```

## Environment Variables

Create a `.env` file:
```env
SENTINEL_MODEL_PATH=../sentinel-ai/outputs/models/latest.pt
DATABASE_URL=sqlite:///./alerts.db
SECRET_KEY=your-secret-key
CORS_ORIGINS=["http://localhost:3000"]
```
