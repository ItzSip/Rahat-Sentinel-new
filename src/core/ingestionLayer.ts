import { processIncomingEvent } from './eventEngine';
import { decodeLoRaEvent } from './loraCodec';

export function ingestFrame(frame: string) {
    const trimmed = frame.trim();

    if (trimmed.startsWith('{') || trimmed.startsWith('[')) {
        // JSON path — BLE batched or single-event frame
        console.log('[BLE RX]', trimmed);
        try {
            const parsed = JSON.parse(trimmed);
            // Accept both single-event objects and batched arrays — never drop silently
            const events = Array.isArray(parsed) ? parsed : [parsed];
            for (const event of events) {
                processIncomingEvent(event);
            }
        } catch {
            // Malformed JSON — discard (intentional silent fail)
        }
    } else {
        // LoRa path — compact pipe-delimited packet
        if (trimmed.length > 100) {
            console.log('[LORA DROP] Packet too large');
            return;
        }
        const event = decodeLoRaEvent(trimmed);
        if (event) {
            processIncomingEvent(event);
        }
    }
}
