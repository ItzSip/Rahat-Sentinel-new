import { create } from 'zustand';
import AsyncStorage from '@react-native-async-storage/async-storage';

const STORAGE_KEY = '@rahat_disaster_active';

interface DisasterState {
    isDisasterActive: boolean;
    activateDisaster:   () => Promise<void>;
    deactivateDisaster: () => Promise<void>;
    hydrate:            () => Promise<void>;
}

export const useDisasterStore = create<DisasterState>((set) => ({
    isDisasterActive: false,

    activateDisaster: async () => {
        await AsyncStorage.setItem(STORAGE_KEY, '1');
        set({ isDisasterActive: true });
    },

    deactivateDisaster: async () => {
        await AsyncStorage.removeItem(STORAGE_KEY);
        set({ isDisasterActive: false });
    },

    // Called once on app mount — restores state after kill/restart
    hydrate: async () => {
        const val = await AsyncStorage.getItem(STORAGE_KEY);
        if (val === '1') set({ isDisasterActive: true });
    },
}));
