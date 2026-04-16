import { create } from 'zustand';
import { RahatEvent } from '../core/types';

interface AlertState {
    alerts: RahatEvent[];
    addAlert: (alert: RahatEvent) => void;
    removeAlert: (id: string) => void;
    clearExpiredAlerts: (currentTime: number) => void;
}

export const useAlertStore = create<AlertState>((set) => ({
    alerts: [],
    addAlert: (alert) =>
        set((state) => ({
            // Prevent duplicates
            alerts: state.alerts.findIndex(a => a.id === alert.id) === -1 
                ? [...state.alerts, alert] 
                : state.alerts,
        })),
    removeAlert: (id) =>
        set((state) => ({
            alerts: state.alerts.filter((alert) => alert.id !== id),
        })),
    clearExpiredAlerts: (currentTime) =>
        set((state) => ({
            alerts: state.alerts.filter((alert) => alert.timestamp + (alert.ttl * 1000) > currentTime),
        })),
}));
