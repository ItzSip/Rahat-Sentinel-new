export type EventType = 'SOS' | 'LOCATION' | 'PING';

export type RahatEvent = {
  id: string;
  type: EventType;
  payload: any;
  timestamp: number;
  ttl: number; // seconds
  priority: number; // SOS > LOCATION > PING
  origin: string;
  hops: number;
};
