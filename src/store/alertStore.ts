import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import AsyncStorage from '@react-native-async-storage/async-storage';

export interface SentinelAlert {
    alert_id: number;
    region_id: number;
    severity: number;
    type: string;
    timestamp: number;
    ttl: number;
    source: string;
}

interface AlertState {
    alerts: SentinelAlert[];
    addAlert: (alert: SentinelAlert) => void;
    clearAlerts: () => void;
    removeExpiredAlerts: () => void;
}

export const useAlertStore = create<AlertState>()(
    persist(
        (set) => ({
            alerts: [],
            addAlert: (alert) =>
                set((state) => {
                    try {
                        // PART 1: Do not store expired alerts
                        const now = Math.floor(Date.now() / 1000);
                        if (now - alert.timestamp > alert.ttl) return state;

                        // PART 5: Prevent duplicate alert_id
                        if (state.alerts.some((a) => a.alert_id === alert.alert_id)) {
                            return state; 
                        }

                        // PART 5: Maintain Maximum 50 Sorted by Timestamp (Newest first)
                        const updated = [...state.alerts, alert]
                            .sort((a, b) => b.timestamp - a.timestamp)
                            .slice(0, 50);

                        return { alerts: updated };
                    } catch (e) {
                        console.error('[STORE ERROR] Failed writing alert', e);
                        return state;
                    }
                }),
            clearAlerts: () => set({ alerts: [] }),
            removeExpiredAlerts: () => 
                set((state) => {
                    const now = Math.floor(Date.now() / 1000);
                    return { 
                        alerts: state.alerts.filter(a => now - a.timestamp <= a.ttl) 
                    };
                }),
        }),
        {
            name: 'alerts',
            storage: createJSONStorage(() => AsyncStorage),
        }
    )
);
