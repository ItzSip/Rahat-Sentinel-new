# Backend API Reference

## Base URL
```
http://localhost:8000
```

## Endpoints

### Health Check
```
GET /health
Response: { "status": "healthy", "service": "rahat-sentinel-api", "version": "0.1.0" }
```

### Get Alerts
```
GET /api/v1/alerts?region=bastar&risk_level=high&limit=50

Response: [
  {
    "id": "alert-001",
    "region": "bastar",
    "risk_level": "high",
    "confidence": 0.92,
    "message": "Flood risk detected",
    "timestamp": "2026-03-27T12:00:00Z",
    "coordinates": { "lat": 21.25, "lon": 81.63 }
  }
]
```

### Trigger Prediction
```
POST /api/v1/alerts/predict
Body: {
  "region": "bastar",
  "coordinates": { "lat": 21.25, "lon": 81.63 },
  "radius_km": 50,
  "use_latest_data": true
}

Response: {
  "region": "bastar",
  "risk_level": "medium",
  "confidence": 0.78,
  "risk_summary": { "safe": 35, "low": 30, "medium": 25, "high": 10 },
  "heatmap_url": "/static/heatmaps/bastar_latest.png",
  "model_version": "0.1.0",
  "timestamp": "2026-03-27T12:00:00Z"
}
```

### Get Heatmap
```
GET /api/v1/heatmap/{region}

Response: {
  "region": "bastar",
  "heatmap_url": "/static/heatmaps/bastar_latest.png",
  "generated_at": "2026-03-27T12:00:00Z",
  "risk_summary": { "safe": 45.2, "low": 30.1, "medium": 18.5, "high": 6.2 }
}
```

### WebSocket — Real-time Alerts
```
WS /ws/alerts

Send: "subscribe:bastar"
Receive: { "type": "alert", "data": { ... } }
```
