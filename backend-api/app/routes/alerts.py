"""
Alert Routes
=============
API endpoints for disaster alerts and predictions.
"""

from fastapi import APIRouter, HTTPException, WebSocket, WebSocketDisconnect
from typing import Optional

from ..schemas.alert_schema import AlertResponse, PredictionRequest, PredictionResponse
from ..services.model_service import ModelService

router = APIRouter()
model_service = ModelService()


@router.get("/alerts", response_model=list[AlertResponse])
async def get_alerts(
    region: Optional[str] = None,
    risk_level: Optional[str] = None,
    limit: int = 50,
):
    """
    Get active disaster alerts.

    - **region**: Filter by region name
    - **risk_level**: Filter by risk level (safe, low, medium, high)
    - **limit**: Max number of alerts to return
    """
    # TODO: Fetch from database
    return [
        AlertResponse(
            id="alert-001",
            region=region or "sample-region",
            risk_level="medium",
            confidence=0.85,
            message="Elevated flood risk detected from satellite analysis",
            timestamp="2026-03-27T12:00:00Z",
            coordinates={"lat": 21.25, "lon": 81.63},
        )
    ]


@router.post("/alerts/predict", response_model=PredictionResponse)
async def trigger_prediction(request: PredictionRequest):
    """
    Trigger a new disaster prediction for a given region.

    Runs the Sentinel AI model on the latest satellite data.
    """
    try:
        result = await model_service.predict(
            region=request.region,
            coordinates=request.coordinates,
        )
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction failed: {str(e)}")


@router.get("/heatmap/{region}")
async def get_heatmap(region: str):
    """Get risk heatmap data for a specific region."""
    # TODO: Return actual heatmap data
    return {
        "region": region,
        "heatmap_url": f"/static/heatmaps/{region}_latest.png",
        "generated_at": "2026-03-27T12:00:00Z",
        "risk_summary": {
            "safe": 45.2,
            "low": 30.1,
            "medium": 18.5,
            "high": 6.2,
        },
    }


@router.websocket("/ws/alerts")
async def websocket_alerts(websocket: WebSocket):
    """Real-time alert stream via WebSocket."""
    await websocket.accept()
    try:
        while True:
            # Wait for client messages (e.g., subscription requests)
            data = await websocket.receive_text()
            # TODO: Stream real-time alerts
            await websocket.send_json({
                "type": "alert",
                "message": f"Subscribed to alerts: {data}",
            })
    except WebSocketDisconnect:
        print("Client disconnected from alert stream")
