import { create } from 'zustand';

interface SettingsState {
    narratorEnabled: boolean;
    volumeLevel: number;
    vibrationLevel: number;
    darkMode: boolean;
    language: 'English' | 'Hindi';
    
    setNarrator: (enabled: boolean) => void;
    setVolume: (level: number) => void;
    setVibration: (level: number) => void;
    setDarkMode: (enabled: boolean) => void;
    setLanguage: (lang: 'English' | 'Hindi') => void;
}

export const useSettingsStore = create<SettingsState>((set) => ({
    narratorEnabled: false,
    volumeLevel: 80,
    vibrationLevel: 50,
    darkMode: true, // Requested default dark gradient theme
    language: 'English',

    setNarrator: (enabled) => set({ narratorEnabled: enabled }),
    setVolume: (level) => set({ volumeLevel: level }),
    setVibration: (level) => set({ vibrationLevel: level }),
    setDarkMode: (enabled) => set({ darkMode: enabled }),
    setLanguage: (lang) => set({ language: lang }),
}));
