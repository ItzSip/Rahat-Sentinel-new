import asyncio
import websockets

async def listen():
    """
    Simulates a Mobile App connecting to the Sentinel Backend via WebSockets.
    Instantly receives and logs JSON alerts published through Redis.
    Includes robust fail-safe auto-reconnection logic.
    """
    uri = "ws://localhost:8000/ws/alerts"
    print(f"Attempting to connect to {uri}...")
    
    while True:
        try:
            async with websockets.connect(uri) as websocket:
                print(f"Successfully Connected to {uri}")
                print("Waiting for alerts... (Trigger GET /demo/trigger or /debug/redis-test)")
                while True:
                    # Non-blocking async listen
                    message = await websocket.recv()
                    print(f"\n[RECEIVED ALERT] {message}")
                    
        except websockets.exceptions.ConnectionClosed:
            print("WebSocket Server disconnected. Reconnecting in 2 seconds...")
            await asyncio.sleep(2)
        except Exception as e:
            print(f"Connection dropped ({e}). Retrying in 2 seconds...")
            await asyncio.sleep(2)

if __name__ == "__main__":
    try:
        asyncio.run(listen())
    except KeyboardInterrupt:
        print("Test client stopped.")
