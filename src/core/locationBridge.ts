/**
 * locationBridge.ts
 *
 * Bridges validated LOCATION events from eventEngine into deviceStore.
 * Applies a 100 m jump-filter to discard GPS glitches before they
 * reach the map.  Call initLocationBridge() once at app startup.
 */
import { setLocationListener } from './eventEngine';
import { LocationPayload } from './types';
import { useDeviceStore } from '../store/deviceStore';

const JUMP_THRESHOLD_M = 100; // metres — larger jump → probable glitch, discard

function haversineMeters(
    lat1: number, lng1: number,
    lat2: number, lng2: number,
): number {
    const R = 6_371_000;
    const toRad = (v: number) => (v * Math.PI) / 180;
    const dLat = toRad(lat2 - lat1);
    const dLng = toRad(lng2 - lng1);
    const a =
        Math.sin(dLat / 2) ** 2 +
        Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2;
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

export function initLocationBridge(): void {
    setLocationListener((event) => {
        const payload = event.payload as Partial<LocationPayload>;
        const { lat, lng, accuracy } = payload;

        if (lat == null || lng == null) return; // malformed payload — drop

        const { peers, addOrUpdatePeer } = useDeviceStore.getState();
        const existing = peers.find(p => p.id === event.origin);

        // Jump filter: if the peer already has a known position and the new fix
        // is > 100 m away, treat it as a GPS glitch and discard.
        if (
            existing?.latitude != null &&
            existing?.longitude != null
        ) {
            const dist = haversineMeters(
                existing.latitude, existing.longitude,
                lat, lng,
            );
            if (dist > JUMP_THRESHOLD_M) return;
        }

        addOrUpdatePeer({
            id: event.origin,
            lastSeen: Date.now(),
            latitude: lat,
            longitude: lng,
            accuracy,
            locationTimestamp: event.timestamp,
        });
    });
}
