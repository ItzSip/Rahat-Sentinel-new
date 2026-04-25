import React from 'react';
import {
    View, StyleSheet, Text, Switch, TouchableOpacity, ScrollView,
} from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useSettingsStore } from '../store/settingsStore';
import { useDisasterStore } from '../store/disasterStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/ui/GlassCard';
import { useStrings } from '../i18n/strings';
import { useNarrator } from '../hooks/useNarrator';

export default function SettingsScreen() {
    const navigation = useNavigation();
    const { narratorEnabled, darkMode, language, setNarrator, setDarkMode, setLanguage } = useSettingsStore();
    const { isDisasterActive, activateDisaster, deactivateDisaster } = useDisasterStore();
    const s = useStrings();
    const { speakAlways } = useNarrator();

    const handleNarratorToggle = (val: boolean) => {
        setNarrator(val);
        // Always speak the confirmation regardless of prior state
        const msg = val ? s.narratorOn : s.narratorOff;
        speakAlways(msg);
    };

    const handleLanguageToggle = () => {
        const next = language === 'English' ? 'Hindi' : 'English';
        setLanguage(next);
        // Speak in the NEW language so user hears the change
        const { ShakeModule } = require('react-native').NativeModules;
        if (ShakeModule?.speak) {
            const newLang = next === 'Hindi'
                ? 'भाषा हिंदी में बदली गई'
                : 'Language changed to English';
            ShakeModule.speak(newLang, next);
        }
    };

    const handleDisasterToggle = async (val: boolean) => {
        if (val) await activateDisaster();
        else     await deactivateDisaster();
    };

    const renderRow = (title: string, value: boolean, onToggle: (v: boolean) => void, activeColor: string = Colors.primary) => (
        <View style={styles.row}>
            <Text style={styles.rowTitle}>{title}</Text>
            <Switch
                value={value}
                onValueChange={onToggle}
                trackColor={{ false: Colors.glassBorder, true: activeColor }}
                thumbColor="#fff"
            />
        </View>
    );

    return (
        <ScrollView style={styles.container} contentContainerStyle={{ paddingBottom: 40 }}>
            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
                    <Text style={styles.backText}>{s.back}</Text>
                </TouchableOpacity>
                <Text style={styles.headerTitle}>{s.settings}</Text>
            </View>

            {/* General settings */}
            <GlassCard style={styles.card}>
                <Text style={styles.sectionTitle}>{s.accessibility}</Text>
                {renderRow(s.screenNarrator, narratorEnabled, handleNarratorToggle, Colors.primary)}

                <View style={styles.divider} />

                <Text style={styles.sectionTitle}>{s.general}</Text>
                {renderRow(s.darkMode, darkMode, setDarkMode, Colors.primary)}

                <View style={[styles.row, { marginTop: 20 }]}>
                    <Text style={styles.rowTitle}>{s.language}</Text>
                    <Text style={styles.langText} onPress={handleLanguageToggle}>
                        {language} ⇄
                    </Text>
                </View>
            </GlassCard>

            {/* ── Disaster Simulation ─────────────────────────────────────── */}
            <GlassCard style={[styles.card, styles.simCard]}>
                <Text style={styles.sectionTitle}>{s.disasterSimulation}</Text>
                <Text style={styles.simHint}>{s.disasterHint}</Text>

                <View style={[styles.row, styles.disasterRow]}>
                    <View style={{ flex: 1 }}>
                        <Text style={styles.rowTitle}>{s.simulateDisaster}</Text>
                        <Text style={styles.disasterSubtitle}>
                            {isDisasterActive ? s.disasterActive : s.disasterInactive}
                        </Text>
                    </View>
                    <Switch
                        value={isDisasterActive}
                        onValueChange={handleDisasterToggle}
                        trackColor={{ false: Colors.glassBorder, true: Colors.red }}
                        thumbColor={isDisasterActive ? Colors.red : '#fff'}
                    />
                </View>

                {isDisasterActive && (
                    <View style={styles.activeBanner}>
                        <View style={styles.activePulse} />
                        <Text style={styles.activeBannerText}>{s.disasterBanner}</Text>
                    </View>
                )}
            </GlassCard>
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, backgroundColor: Colors.background, paddingTop: 52, paddingHorizontal: 20 },
    header: { flexDirection: 'row', alignItems: 'center', marginBottom: 20 },
    backBtn: { paddingRight: 16, paddingVertical: 4 },
    backText: { color: Colors.primary, fontSize: 16 },
    headerTitle: { color: Colors.textPrimary, fontSize: 22, fontWeight: 'bold' },

    card: { padding: 20, marginBottom: 16 },
    simCard: { borderColor: 'rgba(255,59,59,0.3)', borderWidth: 1 },

    sectionTitle: {
        color: Colors.primary, fontSize: 14, fontWeight: 'bold',
        textTransform: 'uppercase', marginBottom: 15, letterSpacing: 0.8,
    },
    row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 },
    rowTitle: { color: Colors.textPrimary, fontSize: 16 },
    divider: { height: 1, backgroundColor: Colors.glassBorder, marginVertical: 10, marginBottom: 20 },
    langText: { color: Colors.primary, fontSize: 16, fontWeight: 'bold', padding: 5 },

    simHint: { color: Colors.textSecondary, fontSize: 13, marginBottom: 16, lineHeight: 19 },
    disasterRow: { alignItems: 'flex-start', paddingBottom: 4 },
    disasterSubtitle: { color: Colors.textSecondary, fontSize: 12, marginTop: 3 },

    activeBanner: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 8,
        backgroundColor: 'rgba(255,59,59,0.12)',
        borderWidth: 1,
        borderColor: 'rgba(255,59,59,0.35)',
        borderRadius: 8,
        paddingHorizontal: 14,
        paddingVertical: 10,
        marginTop: 4,
    },
    activePulse: { width: 8, height: 8, borderRadius: 4, backgroundColor: Colors.red },
    activeBannerText: { color: Colors.red, fontSize: 12, fontWeight: '700', letterSpacing: 1 },
});
