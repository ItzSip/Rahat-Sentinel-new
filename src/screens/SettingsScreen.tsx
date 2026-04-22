import React, { useState } from 'react';
import {
    View, StyleSheet, Text, Switch, TouchableOpacity, TextInput, ScrollView,
} from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useSettingsStore } from '../store/settingsStore';
import { useDisasterStore } from '../store/disasterStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/ui/GlassCard';

const ADMIN_PASSWORD = 'Rahat_admin1102';

export default function SettingsScreen() {
    const navigation = useNavigation();
    const { narratorEnabled, darkMode, language, setNarrator, setDarkMode, setLanguage } = useSettingsStore();
    const { isDisasterActive, activateDisaster, deactivateDisaster } = useDisasterStore();

    const [adminUnlocked, setAdminUnlocked] = useState(false);
    const [passwordInput, setPasswordInput] = useState('');
    const [passwordError, setPasswordError] = useState(false);

    const handleUnlock = () => {
        if (passwordInput === ADMIN_PASSWORD) {
            setAdminUnlocked(true);
            setPasswordError(false);
            setPasswordInput('');
        } else {
            setPasswordError(true);
            setPasswordInput('');
        }
    };

    const handleDisasterToggle = async (val: boolean) => {
        if (val) await activateDisaster();
        else     await deactivateDisaster();
    };

    const renderRow = (title: string, value: boolean, onToggle: (v: boolean) => void) => (
        <View style={styles.row}>
            <Text style={styles.rowTitle}>{title}</Text>
            <Switch
                value={value}
                onValueChange={onToggle}
                trackColor={{ false: Colors.glassBorder, true: Colors.cyan }}
                thumbColor="#fff"
            />
        </View>
    );

    return (
        <ScrollView style={styles.container} contentContainerStyle={{ paddingBottom: 40 }}>
            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
                    <Text style={styles.backText}>← Back</Text>
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Settings</Text>
            </View>

            {/* General settings */}
            <GlassCard style={styles.card}>
                <Text style={styles.sectionTitle}>Accessibility</Text>
                {renderRow('Screen Narrator', narratorEnabled, setNarrator)}

                <View style={styles.divider} />

                <Text style={styles.sectionTitle}>General</Text>
                {renderRow('Dark Mode Theme', darkMode, setDarkMode)}

                <View style={[styles.row, { marginTop: 20 }]}>
                    <Text style={styles.rowTitle}>Language</Text>
                    <Text
                        style={styles.langText}
                        onPress={() => setLanguage(language === 'English' ? 'Hindi' : 'English')}
                    >
                        {language} ⇄
                    </Text>
                </View>
            </GlassCard>

            {/* ── Admin Settings ─────────────────────────────────────────── */}
            <GlassCard style={[styles.card, styles.adminCard]}>
                <Text style={styles.sectionTitle}>Admin Settings</Text>

                {!adminUnlocked ? (
                    /* Password gate */
                    <View>
                        <Text style={styles.adminHint}>
                            Enter admin password to access simulation controls.
                        </Text>
                        <TextInput
                            style={[styles.passwordInput, passwordError && styles.passwordInputError]}
                            value={passwordInput}
                            onChangeText={(t) => { setPasswordInput(t); setPasswordError(false); }}
                            placeholder="Password"
                            placeholderTextColor="rgba(255,255,255,0.25)"
                            secureTextEntry
                            autoCapitalize="none"
                            autoCorrect={false}
                            onSubmitEditing={handleUnlock}
                            returnKeyType="done"
                        />
                        {passwordError && (
                            <Text style={styles.errorText}>Incorrect password</Text>
                        )}
                        <TouchableOpacity style={styles.unlockBtn} onPress={handleUnlock} activeOpacity={0.8}>
                            <Text style={styles.unlockBtnText}>Unlock</Text>
                        </TouchableOpacity>
                    </View>
                ) : (
                    /* Admin panel — shown after correct password */
                    <View>
                        <Text style={styles.adminHint}>
                            Simulation controls. Changes affect all connected devices.
                        </Text>

                        {/* Disaster toggle */}
                        <View style={[styles.row, styles.disasterRow]}>
                            <View style={{ flex: 1 }}>
                                <Text style={styles.rowTitle}>Simulate Disaster</Text>
                                <Text style={styles.disasterSubtitle}>
                                    {isDisasterActive
                                        ? 'BLE scanning active — severity timer running'
                                        : 'BLE scanning gated — system idle'}
                                </Text>
                            </View>
                            <Switch
                                value={isDisasterActive}
                                onValueChange={handleDisasterToggle}
                                trackColor={{ false: Colors.glassBorder, true: Colors.red }}
                                thumbColor={isDisasterActive ? Colors.red : '#fff'}
                            />
                        </View>

                        {/* Status badge */}
                        {isDisasterActive && (
                            <View style={styles.activeBanner}>
                                <View style={styles.activePulse} />
                                <Text style={styles.activeBannerText}>
                                    DISASTER SIMULATION ACTIVE
                                </Text>
                            </View>
                        )}

                        <TouchableOpacity
                            style={styles.lockBtn}
                            onPress={() => setAdminUnlocked(false)}
                            activeOpacity={0.75}
                        >
                            <Text style={styles.lockBtnText}>Lock Admin Panel</Text>
                        </TouchableOpacity>
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
    backText: { color: Colors.cyan, fontSize: 16 },
    headerTitle: { color: Colors.textPrimary, fontSize: 22, fontWeight: 'bold' },

    card: { padding: 20, marginBottom: 16 },
    adminCard: { borderColor: 'rgba(255,59,59,0.3)', borderWidth: 1 },

    sectionTitle: {
        color: Colors.cyan, fontSize: 14, fontWeight: 'bold',
        textTransform: 'uppercase', marginBottom: 15, letterSpacing: 0.8,
    },
    row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 },
    rowTitle: { color: Colors.textPrimary, fontSize: 16 },
    divider: { height: 1, backgroundColor: Colors.glassBorder, marginVertical: 10, marginBottom: 20 },
    langText: { color: Colors.cyan, fontSize: 16, fontWeight: 'bold', padding: 5 },

    // Admin
    adminHint: { color: Colors.textSecondary, fontSize: 13, marginBottom: 16, lineHeight: 19 },

    passwordInput: {
        backgroundColor: 'rgba(255,255,255,0.06)',
        borderWidth: 1,
        borderColor: Colors.glassBorder,
        borderRadius: 10,
        padding: 14,
        color: Colors.textPrimary,
        fontSize: 15,
        marginBottom: 8,
    },
    passwordInputError: { borderColor: Colors.red },
    errorText: { color: Colors.red, fontSize: 13, marginBottom: 12 },

    unlockBtn: {
        backgroundColor: Colors.cyan,
        borderRadius: 10,
        paddingVertical: 13,
        alignItems: 'center',
        marginTop: 4,
    },
    unlockBtnText: { color: '#000', fontWeight: '700', fontSize: 15 },

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
        marginBottom: 16,
    },
    activePulse: {
        width: 8, height: 8, borderRadius: 4,
        backgroundColor: Colors.red,
    },
    activeBannerText: { color: Colors.red, fontSize: 12, fontWeight: '700', letterSpacing: 1 },

    lockBtn: {
        borderWidth: 1,
        borderColor: Colors.glassBorder,
        borderRadius: 10,
        paddingVertical: 12,
        alignItems: 'center',
        marginTop: 8,
    },
    lockBtnText: { color: Colors.textSecondary, fontSize: 14 },
});
