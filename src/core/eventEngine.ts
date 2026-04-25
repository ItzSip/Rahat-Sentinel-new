import { RahatEvent } from './types';
import { getDeviceIdSync } from './deviceIdentity';

const MAX_SEEN_IDS = 100;
const MAX_EVENTS = 50;
const MAX_HOPS = 3;

// Minimum gap between outgoing LOCATION events (battery guard)
const LOCATION_EMIT_INTERVAL_MS = 8_000;
let lastLocationEmitMs = 0;

// State (In-Memory, strictly bounded)
const seenEventIds = new Set<string>();
let storedEvents: RahatEvent[] = [];
let outboundListener: ((event: RahatEvent) => void) | null = null;
// Separate hook for UI/store layer — fired after a LOCATION passes all checks
let locationListener: ((event: RahatEvent) => void) | null = null;
// Fired when a DISASTER frame arrives from a remote peer — triggers local activation
let disasterListener: (() => void) | null = null;
let relayCount = 0;

export const getRelayCount = () => relayCount;

// Helpers
function pruneExpired() {
  const now = Date.now();
  storedEvents = storedEvents.filter(
    (e) => e.timestamp + e.ttl * 1000 > now
  );
}

const isExpired = (event: RahatEvent): boolean => {
  const now = Date.now();
  return (event.timestamp + event.ttl * 1000) < now;
};

const shouldBroadcast = (event: RahatEvent): boolean => {
  if (event.type === 'SOS')      return true;
  if (event.type === 'LOCATION') return true;
  if (event.type === 'DISASTER') return true; // relay disaster activation mesh-wide
  if (event.type === 'PING')     return event.hops === 0; // PING origin only
  return false;
};

// Core Functions
export const setOutboundListener = (fn: (event: RahatEvent) => void) => {
  outboundListener = fn;
};

/** Called by locationBridge — fires after a LOCATION event passes all validation. */
export const setLocationListener = (fn: (event: RahatEvent) => void) => {
  locationListener = fn;
};

/** Registered by useDisasterEffect — fires when a remote DISASTER frame is received. */
export const setDisasterListener = (fn: (() => void) | null) => {
  disasterListener = fn;
};

export const processIncomingEvent = (event: RahatEvent): void => {
  // 1. Drop expired BEFORE processing
  if (isExpired(event)) return;

  // 2. Deduplication check
  if (seenEventIds.has(event.id)) return;

  // 3. Hop control limit
  if (event.hops >= MAX_HOPS) return;

  // 4. Increment hops
  const processedEvent = { ...event, hops: event.hops + 1 };

  if (processedEvent.hops > 0) {
    relayCount++;
  }

  // @ts-ignore
  if (typeof __DEV__ !== 'undefined' && __DEV__) {
    console.log("[EVENT RECEIVED]", processedEvent.id);
  }

  // Track ID (LRU eviction when full)
  seenEventIds.add(processedEvent.id);
  if (seenEventIds.size > MAX_SEEN_IDS) {
    const oldestId = seenEventIds.values().next().value;
    if (oldestId) {
      seenEventIds.delete(oldestId);
    }
  }

  // 5. Priority-aware storage
  pruneExpired();
  storedEvents.push(processedEvent);
  
  if (storedEvents.length > 1) {
    const last = storedEvents.length - 1;
    if (storedEvents[last].priority > storedEvents[last - 1].priority) {
      storedEvents.sort((a, b) => b.priority - a.priority);
    }
  }
  
  // 6. Hard memory cap: Evict oldest/lowest-priority when full (oldest is at end since we push and sort descending)
  if (storedEvents.length > MAX_EVENTS) {
    storedEvents.pop(); 
  }

  // 7. Notify location bridge (UI store update, separate from transport)
  if (processedEvent.type === 'LOCATION' && locationListener) {
    locationListener(processedEvent);
  }

  // 7b. Notify disaster bridge — remote activation auto-sync
  if (processedEvent.type === 'DISASTER' && disasterListener) {
    disasterListener();
  }

  // 8. Dispatch to transport if allowed
  if (shouldBroadcast(processedEvent) && outboundListener) {
    outboundListener(processedEvent);
  }
};

// --- PHASE 6: DEMO CONTROLS (REQUIRED) ---
const randomId = () => `evt_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`;

export function emitTestEvent(type: 'SOS' | 'LOCATION') {
    // Throttle LOCATION to at most one per 8 seconds (battery guard)
    if (type === 'LOCATION') {
        const now = Date.now();
        if (now - lastLocationEmitMs < LOCATION_EMIT_INTERVAL_MS) return;
        lastLocationEmitMs = now;
    }

    const event: RahatEvent = {
        id: randomId(),
        type,
        payload: type === 'LOCATION' ? {
            lat: 22.7196,
            lng: 75.8577,
            accuracy: 15, // metres (simulated)
        } : {},
        timestamp: Date.now(),
        ttl: type === 'LOCATION' ? 120 : 30, // LOCATION stays in mesh 2 min
        priority: type === 'SOS' ? 3 : 2,
        origin: getDeviceIdSync(), // unique per install — set by deviceIdentity bootstrap
        hops: 0,
    };

    processIncomingEvent(event);
}

/**
 * Broadcast a DISASTER activation frame to all BLE peers.
 * Called once on fresh local activation — bypasses local processing so it
 * doesn't re-trigger the disasterListener on the originating device.
 */
export function emitDisasterSync() {
    const event: RahatEvent = {
        id: `evt_dis_${Date.now()}_${Math.random().toString(36).substring(2, 5)}`,
        type: 'DISASTER',
        payload: {},
        timestamp: Date.now(),
        ttl: 120,      // 2-minute window for propagation
        priority: 4,   // above SOS so it's never evicted
        origin: getDeviceIdSync(),
        hops: 0,
    };
    // Pre-mark so if this frame somehow loops back we don't re-activate locally
    seenEventIds.add(event.id);
    if (outboundListener) {
        outboundListener(event);
    }
}

// Readonly access for debug/test
export const getStoredEvents = () => storedEvents;
export const getSeenIdsCount = () => seenEventIds.size;
