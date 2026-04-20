import { create } from 'zustand';

export interface PeerDevice {
    id: string;
    lastSeen: number;
    latitude?: number;
    longitude?: number;
    accuracy?: number;          // metres reported by originating device
    locationTimestamp?: number; // epoch ms of the last accepted location fix
}

interface DeviceState {
    peers: PeerDevice[];
    isScanning: boolean;
    setScanning: (isScanning: boolean) => void;
    setPeers: (peers: PeerDevice[]) => void;
    addOrUpdatePeer: (peer: PeerDevice) => void;
}

export const useDeviceStore = create<DeviceState>((set) => ({
    peers: [],
    isScanning: false,
    setScanning: (isScanning) => set({ isScanning }),
    setPeers: (peers) => set({ peers }),
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
