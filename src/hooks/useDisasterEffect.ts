import { useEffect, useRef } from 'react';
import { AppState, NativeModules, NativeEventEmitter, EmitterSubscription, Vibration } from 'react-native';
import { useDisasterStore } from '../store/disasterStore';
import { useSeverityStore, computeSeverity } from '../store/severityStore';
import { useDeviceStore }  from '../store/deviceStore';
import { startShakeDetection, stopShakeDetection } from '../core/shakeDetector';
import { startScanning, stopScanning, updateSeverity } from '../features/bluetoothMesh/bluetoothService';
import { setDisasterListener, emitDisasterSync } from '../core/eventEngine';

const { ShakeModule } = NativeModules;

// Rapid SOS vibration pattern: three short pulses
const SOS_VIBRATION = [0, 350, 150, 350, 150, 350];

/**
 * Master disaster orchestrator — call once in HomeScreen.
 *
 * Responsibilities:
 *   • Hydrate both stores from AsyncStorage on mount
 *   • Gate BLE scanning on isDisasterActive
 *   • Start / stop background shake detection service
 *   • Post native "Are you safe?" notification on FRESH activation
 *   • Subscribe to native events for immediate foreground handling
 *   • Drain SharedPreferences on every foreground resume (background path)
 *   • Auto-sync disaster activation to BLE peers (DISASTER frame)
 *   • RED severity: vibrate every 2 min + send location to Rahat node every 5 min
 */
export function useDisasterEffect() {
    const isDisasterActive  = useDisasterStore(s => s.isDisasterActive);
    const activateDisaster  = useDisasterStore(s => s.activateDisaster);
    const hydrateDisaster   = useDisasterStore(s => s.hydrate);

    const timerStart        = useSeverityStore(s => s.timerStart);
    const overrideRed       = useSeverityStore(s => s.overrideRed);
    const startTimer        = useSeverityStore(s => s.startTimer);
    const resetTimer        = useSeverityStore(s => s.resetTimer);
    const setOverrideRed    = useSeverityStore(s => s.setOverrideRed);
    const stopTimer         = useSeverityStore(s => s.stopTimer);
    const hydrateSeverity   = useSeverityStore(s => s.hydrate);

    const setScanning       = useDeviceStore(s => s.setScanning);

    // Prevents notification + timer start from firing again on re-renders
    const activationHandled = useRef(false);

    // ── 1. Hydrate stores once on mount ──────────────────────────────────────
    useEffect(() => {
        hydrateDisaster();
        hydrateSeverity();
    }, []);

    // ── 2. Listen for native action events (foreground path) ─────────────────
    //    onShakeDetected   → shake while app is visible
    //    onDisasterAction  → YES/NO notification tap while app is alive (bg/fg)
    useEffect(() => {
        if (!ShakeModule) return;
        const emitter = new NativeEventEmitter(ShakeModule);

        const subs: EmitterSubscription[] = [
            emitter.addListener('onShakeDetected',  () => resetTimer()),
            emitter.addListener('onDisasterAction', (action: string) => {
                if (action === 'YES' || action === 'SHAKE') resetTimer();
                if (action === 'NO')                        setOverrideRed();
            }),
        ];

        return () => subs.forEach(s => s.remove());
    }, []);

    // ── 3. Drain SharedPreferences on every foreground resume (background path)
    //    Handles: shake / YES / NO that occurred while JS was not running
    useEffect(() => {
        const drainPending = () => {
            if (!ShakeModule || !isDisasterActive) return;
            ShakeModule.consumePendingAction((action: string | null) => {
                if (!action) return;
                if (action === 'YES' || action === 'SHAKE') resetTimer();
                if (action === 'NO')                        setOverrideRed();
            });
        };

        const sub = AppState.addEventListener('change', (state) => {
            if (state === 'active') drainPending();
        });

        drainPending();

        return () => sub.remove();
    }, [isDisasterActive]);

    // ── 4. Always-on BLE — start scanning on mount regardless of disaster state ─
    useEffect(() => {
        startScanning();
        setScanning(true);
        return () => {
            stopScanning();
            setScanning(false);
        };
    }, []);

    // ── 5. Register BLE disaster-sync listener ────────────────────────────────
    //    When a remote DISASTER frame arrives via BLE mesh, auto-activate locally.
    useEffect(() => {
        setDisasterListener(() => {
            useDisasterStore.getState().activateDisaster();
        });
        return () => setDisasterListener(null);
    }, []);

    // ── 6. React to disaster activation / deactivation ───────────────────────
    useEffect(() => {
        if (isDisasterActive) {
            startShakeDetection(() => resetTimer());

            if (!activationHandled.current) {
                activationHandled.current = true;

                if (timerStart === 0) {
                    startTimer();
                    ShakeModule?.postDisasterNotification?.();
                }

                // Broadcast activation to all BLE peers so they auto-activate too
                emitDisasterSync();
            }

        } else {
            activationHandled.current = false;
            stopTimer();
            stopShakeDetection();
            ShakeModule?.dismissDisasterNotification?.();
        }

        return () => stopShakeDetection();
    }, [isDisasterActive]);

    // ── 7. Push severity level into BLE advertisement so peers show correct color ─
    //    0=OK, 1=GREEN, 2=ORANGE, 3=RED — updates the status byte on every change
    useEffect(() => {
        if (!isDisasterActive) {
            updateSeverity(0);
            return;
        }
        const sev = computeSeverity(timerStart, overrideRed);
        const level = sev === 'RED' ? 3 : sev === 'ORANGE' ? 2 : 1;
        updateSeverity(level as 0 | 1 | 2 | 3);
    }, [isDisasterActive, timerStart, overrideRed]);

    // ── 8. RED severity effects ───────────────────────────────────────────────
    //    While severity is RED and disaster is active:
    //      • Vibrate with SOS pattern immediately + every 2 minutes
    //      • Send current location to Rahat WiFi node every 5 minutes
    useEffect(() => {
        const severity = computeSeverity(timerStart, overrideRed);
        if (!isDisasterActive || severity !== 'RED') return;

        // Immediate burst on entering RED
        Vibration.vibrate(SOS_VIBRATION);

        const vibTimer = setInterval(() => {
            Vibration.vibrate(SOS_VIBRATION);
        }, 2 * 60_000);

        const locTimer = setInterval(() => {
            const loc = useDeviceStore.getState().myLocation;
            if (!loc) return;
            const msg = `SOS:LAT:${loc.latitude.toFixed(4)},LNG:${loc.longitude.toFixed(4)}`;
            fetch(`http://192.168.4.1/send?data=${encodeURIComponent(msg)}`).catch(() => {});
        }, 5 * 60_000);

        return () => {
            clearInterval(vibTimer);
            clearInterval(locTimer);
            Vibration.cancel();
        };
    }, [isDisasterActive, timerStart, overrideRed]);
}
