import { NativeModules, NativeEventEmitter } from 'react-native';
import { ingestFrame } from '../../core/ingestionLayer';
import { useDeviceStore } from '../../store/deviceStore';

const { RahatMesh } = NativeModules;
const meshEventEmitter = RahatMesh ? new NativeEventEmitter(RahatMesh) : null;

let isScanning = false;
let peerSubscription: any = null;
let dataSubscription: any = null; // incoming BLE event frames

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

    // Subscribe to native events exactly once
    if (meshEventEmitter && !peerSubscription) {
        // Peer discovery — updates the device store for the Nearby screen / map
        peerSubscription = meshEventEmitter.addListener('onPeersUpdated', (peers: any[]) => {
            const mappedPeers = peers.map(p => ({
                id: p.id,
                lastSeen: p.lastSeen,
                name: p.name,
                severity: p.severity,
                signalLevel: p.signalLevel,
                signalTrend: p.signalTrend,
                latitude: typeof p.latitude === 'number' ? p.latitude : undefined,
                longitude: typeof p.longitude === 'number' ? p.longitude : undefined,
            }));
            useDeviceStore.getState().setPeers(mappedPeers);
        });

        // Incoming BLE event frames — routes into the event engine pipeline.
        // NOTE: native side must emit 'onDataReceived' with a JSON string payload
        // (single RahatEvent object OR array) for this to fire.
        dataSubscription = meshEventEmitter.addListener('onDataReceived', (data: string) => {
            onReceive(data);
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
    if (dataSubscription) {
        dataSubscription.remove();
        dataSubscription = null;
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
    console.log("[BLE TX]", frame);
    RahatMesh.bleSend(frame);
}

/**
 * Set device role BEFORE calling startScanning().
 * 'SENDER'   → hosts GATT server + advertises only (no scan, no GATT client)
 * 'RECEIVER' → scans + connects as GATT client only (no advertise, no GATT server)
 * 'FULL'     → both sides active (default)
 */
export function setDeviceRole(role: 'SENDER' | 'RECEIVER' | 'FULL') {
    if (!RahatMesh) return;
    RahatMesh.setDeviceRole(role);
}
