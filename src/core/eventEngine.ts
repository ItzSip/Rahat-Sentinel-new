import { RahatEvent } from './types';

const MAX_SEEN_IDS = 100;
const MAX_EVENTS = 50;
const MAX_HOPS = 3;

// State (In-Memory, strictly bounded)
const seenEventIds = new Set<string>();
let storedEvents: RahatEvent[] = [];
let outboundListener: ((event: RahatEvent) => void) | null = null;
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
  if (event.type === 'SOS') return true;
  if (event.type === 'LOCATION') return true;
  if (event.type === 'PING') return event.hops === 0; // PING origin only
  return false;
};

// Core Functions
export const setOutboundListener = (fn: (event: RahatEvent) => void) => {
  outboundListener = fn;
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
    console.log("[EVENT]", processedEvent.type, processedEvent.id);
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

  // 7. Dispatch if allowed
  if (shouldBroadcast(processedEvent) && outboundListener) {
    outboundListener(processedEvent);
  }
};

// --- PHASE 6: DEMO CONTROLS (REQUIRED) ---
const randomId = () => `evt_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`;

export function emitTestEvent(type: 'SOS' | 'LOCATION') {
    const event: RahatEvent = {
        id: randomId(),
        type,
        payload: type === 'LOCATION' ? {
            lat: 22.7196,
            lng: 75.8577
        } : {},
        timestamp: Date.now(),
        ttl: 30, // 30 seconds
        priority: type === 'SOS' ? 3 : 2,
        origin: "local-device",
        hops: 0,
    };

    processIncomingEvent(event);
}

// Readonly access for debug/test
export const getStoredEvents = () => storedEvents;
export const getSeenIdsCount = () => seenEventIds.size;
