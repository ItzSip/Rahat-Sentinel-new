import { NativeModules, NativeEventEmitter } from 'react-native';
import { ingestFrame } from '../../core/ingestionLayer';
import { useDeviceStore } from '../../store/deviceStore';

const { RahatMesh } = NativeModules;
const meshEventEmitter = RahatMesh ? new NativeEventEmitter(RahatMesh) : null;

let isScanning = false;
let peerSubscription: any = null;

// PHASE 5: BLE SCANNING
export function startScanning() {
    if (!RahatMesh) {
        console.warn('[BLE SERVICE] RahatMesh native module not found!');
        return;
    }
    if (isScanning) return;

    console.log("[BLE SERVICE] Scanning started natively...");
    isScanning = true;
    useDeviceStore.getState().setScanning(true);
    
    // Call Native Bridge to start EmergencyBleService and observing peers
    RahatMesh.startScanning();

    // Subscribe to nearby peers exactly once
    if (meshEventEmitter && !peerSubscription) {
        peerSubscription = meshEventEmitter.addListener('onPeersUpdated', (peers: any[]) => {
            // Natively we send an array of maps
            // e.g. { id, name, severity, signalLevel, signalTrend, lastSeen }
            const mappedPeers = peers.map(p => ({
                id: p.id,
                lastSeen: p.lastSeen,
                name: p.name,
                severity: p.severity,
                signalLevel: p.signalLevel,
                signalTrend: p.signalTrend
                // Natively no lat/lng yet
            }));
            useDeviceStore.getState().setPeers(mappedPeers);
        });
    }
}

export function stopScanning() {
    if (!RahatMesh) return;
    
    RahatMesh.stopScanning();
    isScanning = false;
    useDeviceStore.getState().setScanning(false);
    console.log("[BLE SERVICE] Scanning native completely halted.");

    if (peerSubscription) {
        peerSubscription.remove();
        peerSubscription = null;
    }
}

// PHASE 5: ON RECEIVE (Triggered natively when BLE mesh receives a chunk)
export function onReceive(data: string) {
    // Send string payload directly into ingestion layer
    ingestFrame(data);
}

// PHASE 5: BLE SEND (No retries, lightweight, strictly pass-through to native)
export function bleSend(frame: string) {
    if (!RahatMesh) return;
    console.log(`[BLE TX NATIVE] Sending frame: ${frame}`);
    RahatMesh.bleSend(frame);
}
