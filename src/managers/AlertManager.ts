import { useAlertStore, SentinelAlert } from '../store/alertStore';
import { relayClimateAlert } from '../features/bluetoothMesh/bluetoothService';

// PART 2: Metrics Tracking
export const systemMetrics = {
    alerts_received_ws: 0,
    alerts_received_ble: 0,
    alerts_relayed: 0
};

// Periodic logs and GC (every 60s)
setInterval(() => {
    console.log("SYSTEM METRICS:", systemMetrics);
    
    // Purge expired alerts physically from memory and storage
    try {
        useAlertStore.getState().removeExpiredAlerts();
    } catch(e) {
        console.error('[MANAGER ERROR] GC failure', e);
    }
}, 60000);

export class AlertManager {
    static handleIncomingAlert(payload: any) {
        try {
            // PART 3: Source Visibility Enforcements
            const source = payload.source === 'ble' ? 'ble' : 'cloud';

            // Update respective Counters
            if (source === 'cloud') {
                systemMetrics.alerts_received_ws++;
            } else {
                systemMetrics.alerts_received_ble++;
            }

            // PART 1: TTL Enforcement at the entry gate
            const now = Math.floor(Date.now() / 1000);
            const ttl = payload.ttl || 21600;
            if (now - payload.timestamp > ttl) {
                console.log(`[ALERT MANAGER] Dropped expired alert ${payload.alert_id}`);
                return;
            }

            const alert: SentinelAlert = {
                alert_id: payload.alert_id,
                region_id: payload.region_id,
                severity: payload.severity,
                type: payload.type,
                timestamp: payload.timestamp,
                ttl: ttl,
                source: source
            };

            const existingAlerts = useAlertStore.getState().alerts;
            const exists = existingAlerts.some((a) => a.alert_id === alert.alert_id);

            // Forward non-duplicates
            if (!exists) {
                useAlertStore.getState().addAlert(alert);
                console.log(`[ALERT STORED] alert_id=${alert.alert_id}`);

                if (alert.source === 'cloud') {
                    relayClimateAlert(alert);
                    systemMetrics.alerts_relayed++;
                }
            }
        } catch (error) {
            // PART 6: Safe error handling inside global boundary
            console.error('[MANAGER ERROR] Parsing incoming alert failed safely', error);
        }
    }
}
