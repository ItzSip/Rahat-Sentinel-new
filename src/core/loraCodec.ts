import { RahatEvent, EventType } from './types';

// Must match MAX_HOPS in eventEngine.ts
const MAX_HOPS = 3;

// LoRa packet format (pipe-delimited, 7 fields):
//   <type>|<id>|<lat>|<lng>|<ttl>|<hops>|<origin>
// Example: S|q05a|22.7196|75.8577|30|1|d41e
// Target:  < 80 bytes per packet

const TYPE_CHAR: Record<EventType, string> = {
    SOS:      'S',
    LOCATION: 'L',
    PING:     'P',
    DISASTER: 'D',
};

const CHAR_TYPE: Record<string, EventType> = {
    S: 'SOS',
    L: 'LOCATION',
    P: 'PING',
    D: 'DISASTER',
};

const TYPE_PRIORITY: Record<EventType, number> = {
    SOS:      3,
    LOCATION: 2,
    PING:     1,
    DISASTER: 4,
};

/**
 * Encode a RahatEvent into a compact LoRa packet string.
 * Always < 80 bytes for valid events (worst case ~44 bytes).
 */
export function encodeLoRaEvent(event: RahatEvent): string {
    const typeChar  = TYPE_CHAR[event.type] ?? 'P';
    // Last 4 chars of the random suffix — stable per event, fits 4 bytes
    const shortId   = event.id.slice(-4);
    // Last 6 hex chars of origin UUID (strip dashes first)
    const shortOrigin = event.origin.replace(/-/g, '').slice(-6);

    // Extract GPS from payload for LOCATION; use 0 for other types
    let lat = 0;
    let lng = 0;
    if (event.type === 'LOCATION') {
        const p = event.payload as Record<string, unknown>;
        if (typeof p.lat === 'number') lat = p.lat;
        if (typeof p.lng === 'number') lng = p.lng;
    }

    const packet = [
        typeChar,
        shortId,
        lat.toFixed(4),
        lng.toFixed(4),
        event.ttl,
        event.hops,
        shortOrigin,
    ].join('|');

    console.log('[LORA ENCODE]', packet);
    return packet;
}

/**
 * Decode a LoRa packet string back into a RahatEvent.
 * Returns null for any malformed or out-of-range input.
 */
export function decodeLoRaEvent(packet: string): RahatEvent | null {
    const parts = packet.split('|');
    if (parts.length !== 7) return null;

    const [typeChar, shortId, latStr, lngStr, ttlStr, hopsStr, shortOrigin] = parts;

    const type = CHAR_TYPE[typeChar];
    if (!type) return null;

    const lat  = parseFloat(latStr);
    const lng  = parseFloat(lngStr);
    const ttl  = parseInt(ttlStr, 10);
    const hops = parseInt(hopsStr, 10);

    // Field validation
    if (!shortId || shortId.length < 1)         return null;
    if (!shortOrigin || shortOrigin.length < 1) return null;
    if (isNaN(lat)  || lat  < -90  || lat  > 90)   return null;
    if (isNaN(lng)  || lng  < -180 || lng  > 180)   return null;
    if (isNaN(ttl)  || ttl  <= 0)                   return null;
    if (isNaN(hops) || hops < 0 || hops >= MAX_HOPS) return null;

    // Reconstruct payload: GPS only meaningful for LOCATION with non-zero coords
    const payload: Record<string, unknown> =
        type === 'LOCATION' && (lat !== 0 || lng !== 0)
            ? { lat, lng }
            : {};

    // Prefix id with 'lora_' to namespace away from BLE event IDs
    const event: RahatEvent = {
        id:        `lora_${shortId}`,
        type,
        payload,
        timestamp: Date.now(),
        ttl,
        priority:  TYPE_PRIORITY[type],
        origin:    shortOrigin,
        hops,
    };

    console.log('[LORA DECODE]', event.id, event.type);
    return event;
}
