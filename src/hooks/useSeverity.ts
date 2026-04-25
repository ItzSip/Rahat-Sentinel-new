import { useEffect, useReducer } from 'react';
import { AppState } from 'react-native';
import {
    useSeverityStore,
    computeSeverity,
    msUntilNextBoundary,
    SeverityLevel,
} from '../store/severityStore';

/**
 * Returns the current severity level for this device.
 *
 * Re-renders are triggered at exactly three moments:
 *   1. At the GREEN → ORANGE boundary (10 min mark, 10 s in __DEV__)
 *   2. At the ORANGE → RED boundary  (15 min mark, 20 s in __DEV__)
 *   3. When the app returns to foreground (AppState 'active')
 *
 * The bump counter `n` is included in the effect deps so each fired boundary
 * immediately re-schedules the NEXT boundary timer — without it the chain
 * stops after the first transition.
 */
export function useSeverity(): SeverityLevel {
    const timerStart  = useSeverityStore(s => s.timerStart);
    const overrideRed = useSeverityStore(s => s.overrideRed);

    // n: lightweight trigger — flips an integer to force a re-render and
    //    re-run the scheduling effect so the next boundary gets scheduled.
    const [n, bump] = useReducer((x: number) => x + 1, 0);

    useEffect(() => {
        if (timerStart === 0 || overrideRed) return;

        const ms = msUntilNextBoundary(timerStart);
        if (ms === null) return; // already RED — no more transitions

        const timer = setTimeout(() => bump(), ms + 100); // +100 ms buffer
        return () => clearTimeout(timer);
        // n is intentionally included: after each bump the effect re-runs and
        // schedules the next boundary timer (GREEN→ORANGE, then ORANGE→RED).
    }, [timerStart, overrideRed, n]);

    // Re-evaluate when app comes back to foreground — covers locked-screen transitions
    useEffect(() => {
        const sub = AppState.addEventListener('change', (state) => {
            if (state === 'active') bump();
        });
        return () => sub.remove();
    }, []);

    return computeSeverity(timerStart, overrideRed);
}
