import { useEffect, useRef } from 'react';
import { NativeModules, NativeEventEmitter } from 'react-native';
import { useDeviceStore } from '../store/deviceStore';
import { useSeverityStore, computeSeverity } from '../store/severityStore';
import { SeverityLevel } from '../store/severityStore';

const { RahatNodeModule } = NativeModules;

// Matches the receiver ESP32's BT name "RAHAT_RX"
const ESP32_BT_NAME    = 'RAHAT_RX';
// How often to send location while app is open (ms)
const SEND_INTERVAL_MS = 8_000;
// Retry delay after BT disconnect (ms)
const RECONNECT_MS     = 12_000;

/**
 * Connects to the ESP32 RAHAT_NODE over Bluetooth Classic and periodically
 * sends the device's GPS location + severity over BOTH:
 *   • Bluetooth SPP socket
 *   • HTTP POST to http://192.168.4.1/location
 *
 * Call once from a top-level screen (e.g. HomeScreen) while disaster is active,
 * or always-on if you want continuous tracking.
 */
export function useRahatNode(severity: SeverityLevel) {
    if (!RahatNodeModule) return; // module not available (simulator / old build)

    const myLocation    = useDeviceStore(s => s.myLocation);
    const locationRef   = useRef(myLocation);
    const severityRef   = useRef(severity);
    const reconnectRef  = useRef<ReturnType<typeof setTimeout> | null>(null);

    // Keep refs fresh so the interval closure never captures stale values
    useEffect(() => { locationRef.current  = myLocation; }, [myLocation]);
    useEffect(() => { severityRef.current  = severity;   }, [severity]);

    // ── Lifecycle: connect on mount, disconnect on unmount ────────────────────
    useEffect(() => {
        let mounted = true;

        const tryConnect = () => {
            if (!mounted) return;
            RahatNodeModule.connect(ESP32_BT_NAME)
                .then((name: string) => console.log('[RahatNode] connected:', name))
                .catch((e: any)      => console.log('[RahatNode] connect failed:', e?.message));
        };

        const emitter = new NativeEventEmitter(RahatNodeModule);
        const subs = [
            emitter.addListener('onNodeConnected',    (name: string) => {
                console.log('[RahatNode] BT connected to', name);
            }),
            emitter.addListener('onNodeDisconnected', () => {
                console.log('[RahatNode] BT disconnected — retry in', RECONNECT_MS / 1000, 's');
                if (!mounted) return;
                reconnectRef.current = setTimeout(tryConnect, RECONNECT_MS);
            }),
        ];

        tryConnect();

        return () => {
            mounted = false;
            if (reconnectRef.current) clearTimeout(reconnectRef.current);
            subs.forEach(s => s.remove());
            RahatNodeModule.disconnect();
        };
    }, []);

    // ── Periodic send every SEND_INTERVAL_MS ─────────────────────────────────
    useEffect(() => {
        const id = setInterval(() => {
            const loc = locationRef.current;
            if (!loc) return;

            RahatNodeModule.sendLocation(
                loc.latitude,
                loc.longitude,
                severityRef.current,
            )
            .then((result: string) => console.log('[RahatNode] sent:', result))
            .catch(() => {/* BT/HTTP failure logged in native */});
        }, SEND_INTERVAL_MS);

        return () => clearInterval(id);
    }, []);
}
