/**
 * deviceIdentity.ts
 *
 * Generates a persistent UUID for this app install and caches it in memory
 * after the first AsyncStorage read.  No native module required.
 *
 * Usage:
 *   - Call getDeviceId() once in the app bootstrap (awaited).
 *   - Thereafter use getDeviceIdSync() anywhere synchronously.
 */
import AsyncStorage from '@react-native-async-storage/async-storage';

const STORAGE_KEY = '@rahat_device_id';
let cachedId: string | null = null;

function generateUUID(): string {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
        const r = (Math.random() * 16) | 0;
        const v = c === 'x' ? r : (r & 0x3) | 0x8;
        return v.toString(16);
    });
}

/**
 * Async initialiser — call ONCE during app bootstrap.
 * Reads the persisted ID from storage, or creates and saves a new one.
 * After this resolves, getDeviceIdSync() is safe to call anywhere.
 */
export async function getDeviceId(): Promise<string> {
    if (cachedId != null) return cachedId;
    try {
        const stored = await AsyncStorage.getItem(STORAGE_KEY);
        if (stored != null) {
            cachedId = stored;
            return cachedId;
        }
        const fresh = generateUUID();
        await AsyncStorage.setItem(STORAGE_KEY, fresh);
        cachedId = fresh;
        return cachedId;
    } catch {
        // AsyncStorage unavailable — use ephemeral ID for this session
        if (cachedId == null) cachedId = generateUUID();
        return cachedId;
    }
}

/**
 * Synchronous accessor — only valid after getDeviceId() has resolved.
 * Returns 'unknown-device' if called before initialisation.
 */
export function getDeviceIdSync(): string {
    return cachedId ?? 'unknown-device';
}
