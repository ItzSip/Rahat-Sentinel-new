from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from app.services.websocket_manager import manager

router = APIRouter(tags=["WebSockets"])

@router.websocket("/ws/alerts")
async def websocket_endpoint(websocket: WebSocket):
    """
    Standard WebSocket endpoint. Clients connect here.
    All incoming alerts pushed to Redis will instantly flow to connected clients.
    """
    await manager.connect(websocket)
    try:
        while True:
            # We must hold the connection open. 
            # Clients (mobile) can send ping/pong heartbeats here to prevent timeout.
            data = await websocket.receive_text()
    except WebSocketDisconnect:
        manager.disconnect(websocket)
