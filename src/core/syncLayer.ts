import { RahatEvent } from './types';
import { setOutboundListener } from './eventEngine';
import { encodeLoRaEvent } from './loraCodec';

export type TransportMode = 'BLE' | 'LORA';
let transportMode: TransportMode = 'BLE';

export function setTransportMode(mode: TransportMode) {
    transportMode = mode;
}

const BATCH_MAX = 10;
const HARD_LIMIT = 20;
const MAX_PAYLOAD_BYTES = 120;

// Local Transport Buffer
let eventBatch: RahatEvent[] = [];

// Helpers
const isExpired = (event: RahatEvent): boolean => {
    const now = Date.now();
    return (event.timestamp + event.ttl * 1000) < now;
};

// Accurate O(n) byte-size computation
const getByteSize = (str: string): number => {
    let size = 0;
    for (let i = 0; i < str.length; i++) {
        const code = str.charCodeAt(i);
        if (code <= 0x7f) {
            size += 1;
        } else if (code <= 0x7ff) {
            size += 2;
        } else if (code >= 0xd800 && code <= 0xdfff) {
            size += 4; 
            i++; 
        } else {
            size += 3;
        }
    }
    return size;
};

// Transport function (Phase 4 placeholder, NO BLE direct call here)
let customTransport: ((frame: string) => void) | null = null;
export function setTransportStrategy(strategy: (frame: string) => void) {
    customTransport = strategy;
}

function sendFrame(frame: string) {
    console.log("[SYNC SEND]", frame);
    if (customTransport) {
        customTransport(frame);
    } else {
        // No transport wired yet — frame stays local
        console.warn("[SYNC SEND] No transport strategy set — frame not transmitted");
    }
}

export const flushBatch = () => {
    // 1. Remove expired events
    eventBatch = eventBatch.filter(e => !isExpired(e));

    // 2. If empty -> return
    if (eventBatch.length === 0) return;

    // 3. Stringify & Check limits
    const frame = JSON.stringify(eventBatch);
    const size = getByteSize(frame);

    if (size > MAX_PAYLOAD_BYTES) {
        // Fallback: send individually
        const failedEvents: RahatEvent[] = [];
        
        for (const event of eventBatch) {
            const singleFrame = JSON.stringify([event]);
            try {
                sendFrame(singleFrame);
            } catch (e) {
                if (event.type !== 'SOS') failedEvents.push(event);
            }
        }
        
        // Requeue logic: respect HARD_LIMIT
        eventBatch = [];
        for (const failed of failedEvents) {
            if (eventBatch.length < HARD_LIMIT) {
                eventBatch.push(failed);
            }
        }
    } else {
        try {
            sendFrame(frame);
            eventBatch = []; // Emptied on success
        } catch (e) {
            // Requeue non-SOS if space allows
            const failedNonSos = eventBatch.filter(e => e.type !== 'SOS');
            eventBatch = [];
            for (const failed of failedNonSos) {
                if (eventBatch.length < HARD_LIMIT) {
                    eventBatch.push(failed);
                }
            }
        }
    }
};

export const handleEvent = (event: RahatEvent) => {
    // 1. Drop expired
    if (isExpired(event)) return;

    // LoRa path: one event per packet, no batching, no size accumulation
    if (transportMode === 'LORA') {
        const packet = encodeLoRaEvent(event);
        if (packet.length > 100) {
            console.log('[LORA DROP] Packet too large:', packet.length);
            return;
        }
        console.log('[LORA TX]', packet);
        sendFrame(packet);
        return;
    }

    // BLE path — all batching logic below is unchanged
    // 2. SOS override logic -> immediate send
    if (event.type === 'SOS') {
        flushBatch(); // Flush existing queue first
        try {
            sendFrame(JSON.stringify([event]));
        } catch (e) {
            // SOS doesn't requeue
        }
    } else if (event.type === 'LOCATION') {
        // 3. LOCATION -> flush immediately after push
        if (eventBatch.length >= HARD_LIMIT) eventBatch.shift();
        eventBatch.push(event);
        flushBatch();
    } else {
        // 4. Batching for others
        if (eventBatch.length >= HARD_LIMIT) {
            eventBatch.shift(); // Drop oldest
        }
        eventBatch.push(event);

        if (eventBatch.length >= BATCH_MAX) {
            flushBatch();
        }
    }
};

// Wire listener deterministically upon initialization
setOutboundListener(handleEvent);
