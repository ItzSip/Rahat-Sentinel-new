import { create } from 'zustand';
import AsyncStorage from '@react-native-async-storage/async-storage';

const TIMER_KEY = '@rahat_timer_start';

export type SeverityLevel = 'GREEN' | 'ORANGE' | 'RED';

// ── Pure helpers (no React, no side effects) ─────────────────────────────────

export function computeSeverity(timerStart: number, overrideRed: boolean): SeverityLevel {
    if (overrideRed) return 'RED';
    if (timerStart === 0) return 'GREEN';
    const elapsedMin = (Date.now() - timerStart) / 60_000;
    if (elapsedMin < 10) return 'GREEN';
    if (elapsedMin < 15) return 'ORANGE';
    return 'RED';
}

/**
 * Returns ms until the next severity boundary, or null if already RED.
 * Used to schedule a single re-render exactly at each transition point.
 */
export function msUntilNextBoundary(timerStart: number): number | null {
    if (timerStart === 0) return null;
    const elapsed = Date.now() - timerStart;
    const min10   = 10 * 60_000;
    const min15   = 15 * 60_000;
    if (elapsed < min10) return min10 - elapsed;
    if (elapsed < min15) return min15 - elapsed;
    return null; // Already RED
}

// ── Store ─────────────────────────────────────────────────────────────────────

interface SeverityState {
    timerStart:  number;   // 0 = no active timer
    overrideRed: boolean;  // user pressed NO → instant RED until next reset

    startTimer:    () => Promise<void>;
    resetTimer:    () => Promise<void>;
    setOverrideRed:() => void;
    stopTimer:     () => Promise<void>;
    hydrate:       () => Promise<void>;
}

export const useSeverityStore = create<SeverityState>((set) => ({
    timerStart:  0,
    overrideRed: false,

    // Called on fresh disaster activation only (not on hydration)
    startTimer: async () => {
        const now = Date.now();
        await AsyncStorage.setItem(TIMER_KEY, String(now));
        set({ timerStart: now, overrideRed: false });
    },

    // Called on shake or YES response — restarts the clock from now
    resetTimer: async () => {
        const now = Date.now();
        await AsyncStorage.setItem(TIMER_KEY, String(now));
        set({ timerStart: now, overrideRed: false });
    },

    // Called when user answers NO
    setOverrideRed: () => set({ overrideRed: true }),

    // Called when disaster is deactivated
    stopTimer: async () => {
        await AsyncStorage.removeItem(TIMER_KEY);
        set({ timerStart: 0, overrideRed: false });
    },

    // Restores persisted timer after app kill — preserves elapsed time
    hydrate: async () => {
        const val = await AsyncStorage.getItem(TIMER_KEY);
        if (val) set({ timerStart: parseInt(val, 10) });
    },
}));
