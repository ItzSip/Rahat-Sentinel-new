import { create } from 'zustand';
import AsyncStorage from '@react-native-async-storage/async-storage';

const TIMER_KEY = '@rahat_timer_start';

export type SeverityLevel = 'GREEN' | 'ORANGE' | 'RED';

// ── Pure helpers (no React, no side effects) ─────────────────────────────────

// In DEV builds use 10 s → ORANGE, 20 s → RED so you can verify transitions without waiting.
// In production release builds __DEV__ is false so boundaries are 10 min / 15 min.
const GREEN_MS = __DEV__ ? 10_000  : 10 * 60_000;
const RED_MS   = __DEV__ ? 20_000  : 15 * 60_000;

export function computeSeverity(timerStart: number, overrideRed: boolean): SeverityLevel {
    if (overrideRed) return 'RED';
    if (timerStart === 0) return 'GREEN';
    const elapsed = Date.now() - timerStart;
    if (elapsed < GREEN_MS) return 'GREEN';
    if (elapsed < RED_MS)   return 'ORANGE';
    return 'RED';
}

/**
 * Returns ms until the next severity boundary, or null if already RED.
 * Used to schedule a single re-render exactly at each transition point.
 */
export function msUntilNextBoundary(timerStart: number): number | null {
    if (timerStart === 0) return null;
    const elapsed = Date.now() - timerStart;
    if (elapsed < GREEN_MS) return GREEN_MS - elapsed;
    if (elapsed < RED_MS)   return RED_MS   - elapsed;
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
