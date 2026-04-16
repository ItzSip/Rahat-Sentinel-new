import { create } from 'zustand';

export interface EarlyWarning {
    id: string;
    title: string;
    location: string;
    timeWindow: string;
    severity: 'WATCH' | 'WARNING' | 'CRITICAL';
    metrics: {
        rainfallAnomaly: number;
        terrainInstability: number;
        historicalMatch: number;
        modelConfidence: number;
    };
}

interface WarningState {
    warnings: EarlyWarning[];
    setWarnings: (warnings: EarlyWarning[]) => void;
}

// Static mock data for Early Warning
const MOCK_WARNINGS: EarlyWarning[] = [
    {
        id: 'warn_1',
        title: 'Flood risk detected',
        location: 'North Ridge, Sector 4',
        timeWindow: '~18h away',
        severity: 'WATCH',
        metrics: {
            rainfallAnomaly: 85,
            terrainInstability: 42,
            historicalMatch: 78,
            modelConfidence: 91,
        }
    }
];

export const useWarningStore = create<WarningState>((set) => ({
    warnings: MOCK_WARNINGS,
    setWarnings: (warnings) => set({ warnings }),
}));
