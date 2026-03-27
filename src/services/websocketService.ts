import { AlertManager } from '../managers/AlertManager';
import { ENV } from '../config/env';

export class WebSocketService {
    private ws: WebSocket | null = null;
    private reconnectInterval = 2000;
    private shouldReconnect = true;
    public isOffline = true;

    connect() {
        try {
            console.log(`[WS ATTEMPT] Connecting to ${ENV.BACKEND_WS_URL}`);
            this.ws = new WebSocket(ENV.BACKEND_WS_URL);

            this.ws.onopen = () => {
                this.isOffline = false;
                console.log('[WS CONNECTED] connected to sentinel:alerts pipeline');
                this.reconnectInterval = 2000;
            };

            this.ws.onmessage = (event) => {
                try {
                    const payload = JSON.parse(event.data);
                    // PART 3: Force tag 'cloud' globally for websocket origins
                    payload.source = "cloud"; 
                    AlertManager.handleIncomingAlert(payload);
                } catch (error) {
                    console.error('[WS PARSE ERROR] Invalid socket frame dropped safely', error);
                }
            };

            this.ws.onclose = () => {
                // PART 4: Offline Target Fallbacks
                if (!this.isOffline) {
                    this.isOffline = true;
                    console.log('[WS DISCONNECTED] Connection dropped');
                    console.log("Operating in offline mesh mode");
                }
                this.handleReconnect();
            };

            this.ws.onerror = (error) => {
                console.warn('[WS ERROR] Socket transmission disrupted', error);
            };
            
        } catch(e) {
            // PART 6: Safe fallback wrapping
            console.error('[WS ERROR] Setup crashed, caught safely', e);
            this.handleReconnect();
        }
    }

    private handleReconnect() {
        if (!this.shouldReconnect) return;
        
        console.log(`[WS RECONNECTING] Retrying link in ${this.reconnectInterval}ms...`);
        setTimeout(() => {
            this.connect();
            this.reconnectInterval = Math.min(this.reconnectInterval * 1.5, 5000); 
        }, this.reconnectInterval);
    }

    disconnect() {
        this.shouldReconnect = false;
        if (this.ws) {
            this.ws.close();
        }
    }
}

export const webSocketService = new WebSocketService();
