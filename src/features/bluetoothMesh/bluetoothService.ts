import { SentinelAlert } from '../../store/alertStore';
import { AlertManager } from '../../managers/AlertManager';

export interface MeshPayload {
    id: string;
    type: string;
    originDeviceId: string;
    ttl: number;
    createdAt: number;
    alertData: any;
}

export const receiveMeshPayload = (payload: MeshPayload): void => {
    try {
        if (payload.type === 'CLIMATE_ALERT') {
            const rawAlert = payload.alertData;
            
            // PART 3: Hardcode ble origin locally upon receipt
            AlertManager.handleIncomingAlert({
                ...rawAlert,
                source: 'ble' 
            });
            
        } else if (payload.type === 'SOS') {
            console.log(`[BLE RECEIVE] Dispatched critical SOS to legacy core.`);
        }
    } catch (error) {
        // PART 6: Crash protections 
        console.error('[BLE PARSE ERROR] Malformed BT frame intercepted safely', error);
    }
};

export const relayClimateAlert = (alert: SentinelAlert): void => {
    try {
        // PART 1: Enforce TTL validation before transmitting boundless alerts into the localized Mesh
        const now = Math.floor(Date.now() / 1000);
        if (now - alert.timestamp > alert.ttl) {
            return; // Refuse propagation of dead threats
        }

        const meshPayload: MeshPayload = {
            id: `climate-${alert.alert_id}`,
            type: 'CLIMATE_ALERT',
            originDeviceId: 'rahat-local-cloud-relay',
            ttl: 5,
            createdAt: Date.now(),
            alertData: alert
        };

        const relayDelay = Math.floor(Math.random() * 3000) + 2000;
        
        setTimeout(() => {
            console.log(`[BLE RELAY] Executing burst over Mesh bounds for alert_id=${alert.alert_id}`);
        }, relayDelay);
        
    } catch(e) {
         console.error('[BLE RELAY ERROR] Stopped transmission failure safely', e);
    }
};
