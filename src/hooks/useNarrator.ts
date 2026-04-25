import { useCallback } from 'react';
import { NativeModules } from 'react-native';
import { useSettingsStore } from '../store/settingsStore';

const { ShakeModule } = NativeModules;

export function useNarrator() {
    const narratorEnabled = useSettingsStore(s => s.narratorEnabled);
    const language        = useSettingsStore(s => s.language);

    const speak = useCallback((text: string) => {
        if (!narratorEnabled || !ShakeModule?.speak) return;
        ShakeModule.speak(text, language);
    }, [narratorEnabled, language]);

    const speakAlways = useCallback((text: string) => {
        if (!ShakeModule?.speak) return;
        ShakeModule.speak(text, language);
    }, [language]);

    return { speak, speakAlways, narratorEnabled };
}
