import { useEffect, useRef } from 'react';
import { AppState, NativeModules, NativeEventEmitter, EmitterSubscription } from 'react-native';
import { useDisasterStore } from '../store/disasterStore';
import { useSeverityStore } from '../store/severityStore';
import { useDeviceStore }  from '../store/deviceStore';
import { startShakeDetection, stopShakeDetection } from '../core/shakeDetector';
import { startScanning, stopScanning } from '../features/bluetoothMesh/bluetoothService';

const { ShakeModule } = NativeModules;

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
 */
export function useDisasterEffect() {
    const isDisasterActive  = useDisasterStore(s => s.isDisasterActive);
    const hydrateDisaster   = useDisasterStore(s => s.hydrate);

    const timerStart        = useSeverityStore(s => s.timerStart);
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

        // Also drain immediately in case there's a pending action right now
        drainPending();

        return () => sub.remove();
    }, [isDisasterActive]);

    // ── 4. Always-on BLE — start scanning on mount regardless of disaster state ─
    //    This pre-warms peer discovery so devices are already known the moment
    //    disaster is activated. Peer locations flow via onPeersUpdated → store.
    useEffect(() => {
        startScanning();
        setScanning(true);
        return () => {
            stopScanning();
            setScanning(false);
        };
    }, []);

    // ── 5. React to disaster activation / deactivation ───────────────────────
    //    BLE is always running — this effect only gates shake + notifications.
    useEffect(() => {
        if (isDisasterActive) {
            startShakeDetection(() => resetTimer());

            if (!activationHandled.current) {
                activationHandled.current = true;

                if (timerStart === 0) {
                    startTimer();
                    ShakeModule?.postDisasterNotification?.();
                }
            }

        } else {
            activationHandled.current = false;
            stopTimer();
            stopShakeDetection();
            ShakeModule?.dismissDisasterNotification?.();
        }

        return () => stopShakeDetection();
    }, [isDisasterActive]);
}
