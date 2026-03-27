/**
 * ENVIRONMENT CONFIGURATION
 * 
 * ⚠️ IMPORTANT FOR PHYSICAL ANDROID TESTING ⚠️
 * When running the app on a real phone via USB, 'localhost' will not work
 * because it points to the phone itself, not your PC running the FastAPI backend.
 * 
 * Replace '192.168.1.X' with your PC's actual local IPv4 address.
 * You can find this by running `ipconfig` in your terminal.
 */

export const ENV = {
    // BACKEND_WS_URL: 'ws://localhost:8000/ws/alerts', // <-- Use ONLY for iOS Simulator
    BACKEND_WS_URL: 'ws://10.0.12.134:8000/ws/alerts',  // <-- BOUND TO HOST PC
};
