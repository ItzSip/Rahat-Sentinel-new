# 🏗️ Rahat — System Architecture

## High-Level Data Flow

```
┌─────────────┐     ┌──────────────┐     ┌──────────────┐     ┌─────────────┐
│  Satellite   │────▶│ sentinel-ai  │────▶│ backend-api  │────▶│ mobile-app  │
│  (GEE/S2)   │     │  ML Pipeline │     │   FastAPI     │     │   (Rahat)   │
└─────────────┘     └──────────────┘     └──────────────┘     └─────────────┘
                                                                      │
                                                                      ▼
                                                               ┌─────────────┐
                                                               │  BLE Mesh   │
                                                               │  Network    │
                                                               └─────────────┘
```

## Module Responsibilities

| Module | Responsibility | Isolation |
|--------|---------------|-----------|
| `mobile-app/` | BLE, UI, SOS | 🔒 Stable — minimal changes |
| `sentinel-ai/` | ML + satellite data | 🔒 Independent — no app deps |
| `backend-api/` | REST/WS bridge | 🔗 Bridge — connects AI to app |
| `shared/` | Common schemas | 📦 Lightweight — data contracts |

## Communication Pattern

```
AI → API → Mobile (NEVER direct coupling)
```

- `sentinel-ai` produces predictions + heatmaps
- `backend-api` serves them via REST + WebSocket
- `mobile-app` consumes via HTTP/WS client
- BLE mesh distributes alerts to nearby devices

## Merging Strategy

- Each module has its own `requirements.txt` / `package.json`
- Changes in `sentinel-ai/` NEVER touch `mobile-app/`
- `backend-api/` is the ONLY interface point
- Zero merge conflicts guaranteed ✅
