import { create } from 'zustand';

export interface PeerDevice {
    id: string;
    lastSeen: number;
    latitude?: number;
    longitude?: number;
    accuracy?: number;          // metres reported by originating device
    locationTimestamp?: number; // epoch ms of the last accepted location fix
    // Fields populated by BLE native bridge
    name?: string;
    severity?: string;          // 'HIGH' | 'NORMAL'
    signalLevel?: string;       // 'VERY_STRONG' | 'STRONG' | 'MODERATE' | 'WEAK'
    signalTrend?: string;       // 'APPROACHING' | 'RECEDING' | 'STABLE'
}

interface DeviceState {
    peers: PeerDevice[];
    isScanning: boolean;
    /** Own device's last accepted GPS fix — shared with disaster hooks for RED alerts */
    myLocation: { latitude: number; longitude: number } | null;
    setScanning:    (isScanning: boolean) => void;
    setPeers:       (peers: PeerDevice[]) => void;
    addOrUpdatePeer:(peer: PeerDevice) => void;
    setMyLocation:  (loc: { latitude: number; longitude: number } | null) => void;
}

export const useDeviceStore = create<DeviceState>((set) => ({
    peers: [],
    isScanning: false,
    myLocation: null,
    setScanning:     (isScanning) => set({ isScanning }),
    setPeers:        (peers) => set({ peers }),
    setMyLocation:   (myLocation) => set({ myLocation }),
    addOrUpdatePeer: (peer) =>
        set((state) => {
            const existingIndex = state.peers.findIndex(p => p.id === peer.id);
            if (existingIndex >= 0) {
                const newPeers = [...state.peers];
                newPeers[existingIndex] = { ...newPeers[existingIndex], ...peer };
                return { peers: newPeers };
            }
            return { peers: [...state.peers, peer] };
        }),
}));
