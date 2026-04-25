export type EventType = 'SOS' | 'LOCATION' | 'PING' | 'DISASTER';

export interface LocationPayload {
    lat: number;
    lng: number;
    accuracy?: number; // metres, if the device reported it
}

export type RahatEvent = {
  id: string;
  type: EventType;
  payload: LocationPayload | Record<string, unknown>;
  timestamp: number;
  ttl: number; // seconds
  priority: number; // SOS > LOCATION > PING
  origin: string;
  hops: number;
};
