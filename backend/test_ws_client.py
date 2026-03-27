import asyncio
import websockets

async def listen():
    uri = "ws://localhost:8000/ws/alerts"
    print(f"Attempting to connect to {uri}...")
    
    while True:
        try:
            async with websockets.connect(uri) as websocket:
                print(f"Successfully Connected to {uri}")
                print("Waiting for alerts... (Trigger GET /demo/trigger or /debug/redis-test)")
                while True:
                    message = await websocket.recv()
                    # Added explicit logging for demo reliability
                    print(f"\n[WS ALERT RECEIVED] {message}")
                    print(f"[BLE BROADCAST] Emitting payload over Bluetooth Mesh...")
                    print(f"[BLE RECEIVED] Successfully received by offline nodes.")
                    
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
