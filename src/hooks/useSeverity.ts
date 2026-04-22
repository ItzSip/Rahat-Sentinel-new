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
 *   1. At the GREEN → ORANGE boundary (10 min mark)
 *   2. At the ORANGE → RED boundary (15 min mark)
 *   3. When the app returns to foreground (AppState 'active')
 *
 * No polling loops. No continuous timers.
 */
export function useSeverity(): SeverityLevel {
    const timerStart  = useSeverityStore(s => s.timerStart);
    const overrideRed = useSeverityStore(s => s.overrideRed);

    // Lightweight trigger — flips an integer to force re-render
    const [, bump] = useReducer((n: number) => n + 1, 0);

    // Schedule a single re-render at the next severity boundary
    useEffect(() => {
        if (timerStart === 0 || overrideRed) return;

        const ms = msUntilNextBoundary(timerStart);
        if (ms === null) return; // already RED — no more transitions

        const timer = setTimeout(() => bump(), ms + 100); // +100 ms buffer
        return () => clearTimeout(timer);
    }, [timerStart, overrideRed]);

    // Re-evaluate when app comes back to foreground
    // (covers the case where the phone was locked during a severity transition)
    useEffect(() => {
        const sub = AppState.addEventListener('change', (state) => {
            if (state === 'active') bump();
        });
        return () => sub.remove();
    }, []);

    return computeSeverity(timerStart, overrideRed);
}
