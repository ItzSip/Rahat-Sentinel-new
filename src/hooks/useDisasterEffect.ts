import { useEffect, useRef } from 'react';
import { AppState, NativeModules, NativeEventEmitter, EmitterSubscription, Vibration } from 'react-native';
import { useDisasterStore } from '../store/disasterStore';
import { useSeverityStore, SeverityLevel } from '../store/severityStore';
import { useDeviceStore }  from '../store/deviceStore';
import { useSettingsStore } from '../store/settingsStore';
import { startShakeDetection, stopShakeDetection } from '../core/shakeDetector';
import { startScanning, stopScanning, updateSeverity } from '../features/bluetoothMesh/bluetoothService';
import { setDisasterListener, emitDisasterSync } from '../core/eventEngine';
import S from '../i18n/strings';

const { ShakeModule } = NativeModules;

// Rapid SOS vibration pattern: three short pulses
const SOS_VIBRATION = [0, 350, 150, 350, 150, 350];

function announceDisaster(active: boolean) {
    if (!ShakeModule?.speak) return;
    const lang = useSettingsStore.getState().language;
    const str  = S[lang];
    if (useSettingsStore.getState().narratorEnabled) {
        ShakeModule.speak(active ? str.disasterActivated : str.disasterDeactivated, lang);
    }
}

/**
 * Master disaster orchestrator — called once in HomeScreen.
 *
 * @param severity - current severity level from useSeverity(), passed in so we
 *   share a single timer instance and Effects 7/9 re-run at every boundary.
 *
 * Responsibilities:
 *   • Hydrate both stores from AsyncStorage on mount
 *   • Start / stop background shake detection service
 *   • Post native "Are you safe?" notification on FRESH activation
 *   • Subscribe to native events for immediate foreground handling
 *   • Drain SharedPreferences on every foreground resume (background path)
 *   • Auto-sync disaster activation to BLE peers (DISASTER frame)
 *   • Push severity level into BLE advertisement on every boundary transition
 *   • RED severity: vibrate every 2 min + send location to Rahat node every 5 min
 *   • Guard against immediate re-activation after manual deactivation (90s cooldown)
 */
export function useDisasterEffect(severity: SeverityLevel) {
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
    const peers             = useDeviceStore(s => s.peers);

    // Prevents notification + timer start from firing again on re-renders
    const activationHandled = useRef(false);
    // Timestamp of the last manual/explicit deactivation.
    // Effect 8 (peer auto-sync) is suppressed for 90 s after deactivation so
    // a neighbouring device staying in disaster mode doesn't immediately
    // re-activate this device and reset its severity timer.
    const deactivatedAt     = useRef(0);

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

                announceDisaster(true);
                // Broadcast activation to all BLE peers so they auto-activate too
                emitDisasterSync();
            }

        } else {
            activationHandled.current = false;
            deactivatedAt.current     = Date.now(); // start cooldown clock
            announceDisaster(false);
            stopTimer();
            stopShakeDetection();
            ShakeModule?.dismissDisasterNotification?.();
        }

        return () => stopShakeDetection();
    }, [isDisasterActive]);

    // ── 7. Push severity level into BLE advertisement on every transition ─────
    //    Depends on `severity` (not timerStart/overrideRed) so this effect
    //    re-runs every time the boundary timer fires in useSeverity.
    //    0=OK, 1=GREEN, 2=ORANGE, 3=RED
    useEffect(() => {
        if (!isDisasterActive) {
            updateSeverity(0);
            return;
        }
        const level = severity === 'RED' ? 3 : severity === 'ORANGE' ? 2 : 1;
        updateSeverity(level as 0 | 1 | 2 | 3);
    }, [isDisasterActive, severity]);

    // ── 8. Advertisement-based disaster auto-sync ─────────────────────────────
    //    BLE advertisement already encodes severity (1=GREEN,2=ORANGE,3=RED).
    //    When any scanned peer shows an active severity, they are in disaster mode.
    //    90 s cooldown after deactivation prevents immediate re-activation when a
    //    neighbour is still in disaster mode (stops the severity-reset loop).
    const DEACTIVATION_COOLDOWN_MS = 90_000;
    useEffect(() => {
        if (isDisasterActive) return;
        if (Date.now() - deactivatedAt.current < DEACTIVATION_COOLDOWN_MS) return;
        const activeDisasterPeer = peers.find(
            p => p.severity === 'GREEN' || p.severity === 'ORANGE' || p.severity === 'RED'
        );
        if (activeDisasterPeer) {
            useDisasterStore.getState().activateDisaster();
        }
    }, [peers, isDisasterActive]);

    // ── 9. RED severity effects ───────────────────────────────────────────────
    //    Depends on `severity` so this effect activates/deactivates exactly when
    //    the boundary timer fires — without waiting for a manual reset.
    //    While severity is RED and disaster is active:
    //      • Vibrate with SOS pattern immediately + every 2 minutes
    //      • Send current location to Rahat WiFi node every 5 minutes
    useEffect(() => {
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
    }, [isDisasterActive, severity]);
}
