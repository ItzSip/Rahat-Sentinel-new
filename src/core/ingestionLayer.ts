import { processIncomingEvent } from './eventEngine';

export function ingestFrame(frame: string) {
    try {
        const events = JSON.parse(frame);
        if (!Array.isArray(events)) return;
        
        for (const event of events) {
            processIncomingEvent(event);
        }
    } catch {
        // silent fail (demo safe)
    }
}
