import React, { useState } from 'react';
import {
    View,
    Text,
    TextInput,
    StyleSheet,
    TouchableOpacity,
    KeyboardAvoidingView,
    Platform,
} from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Colors } from '../theme/colors';
import { useUserStore } from '../store/userStore';

// Standalone screen — rendered by App.tsx before NavigationContainer mounts.
// No navigation hooks; completion is signalled via the onComplete callback.
interface AuthScreenProps {
    onComplete: () => void;
}

export default function AuthScreen({ onComplete }: AuthScreenProps) {
    const [name, setName] = useState('');
    const [phone, setPhone] = useState('');
    const setProfile = useUserStore(state => state.setProfile);

    const handleContinue = async () => {
        if (!name.trim()) return;

        try {
            await AsyncStorage.setItem('has_onboarded', 'true');
            await AsyncStorage.setItem('user_name', name.trim());
            await AsyncStorage.setItem('user_phone', phone.trim());

            // RootNavigator-compatible keys (so navigator always boots to Home)
            await AsyncStorage.setItem('@rahat_launched', 'true');
            await AsyncStorage.setItem('@rahat_name', name.trim());
            await AsyncStorage.setItem('@rahat_phone', phone.trim());

            setProfile({ name: name.trim(), phone: phone.trim() });
        } catch (e) {
            console.log('Storage error:', e);
        }

        // CRITICAL: always runs — onComplete must NOT depend on storage succeeding
        onComplete();
    };

    return (
        <KeyboardAvoidingView
            style={styles.container}
            behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        >
            <View style={styles.inner}>
                <View style={styles.logoArea}>
                    <Text style={styles.logo}>⛑</Text>
                    <Text style={styles.appName}>Rahat Sentinel</Text>
                    <Text style={styles.subtitle}>Offline-first disaster response</Text>
                </View>

                <View style={styles.form}>
                    <Text style={styles.label}>Your name *</Text>
                    <TextInput
                        style={styles.input}
                        placeholder="Enter your name"
                        placeholderTextColor={Colors.textSecondary}
                        value={name}
                        onChangeText={setName}
                        autoFocus
                        returnKeyType="next"
                    />

                    <Text style={styles.label}>Phone number (optional)</Text>
                    <TextInput
                        style={styles.input}
                        placeholder="+91 XXXXXXXXXX"
                        placeholderTextColor={Colors.textSecondary}
                        keyboardType="phone-pad"
                        value={phone}
                        onChangeText={setPhone}
                        returnKeyType="done"
                        onSubmitEditing={handleContinue}
                    />

                    <TouchableOpacity
                        style={[styles.btn, !name.trim() && styles.btnDisabled]}
                        onPress={handleContinue}
                        activeOpacity={0.85}
                        disabled={!name.trim()}
                    >
                        <Text style={styles.btnText}>Continue →</Text>
                    </TouchableOpacity>

                    <Text style={styles.note}>Stored locally on your device. No account needed.</Text>
                </View>
            </View>
        </KeyboardAvoidingView>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, backgroundColor: Colors.background },
    inner: { flex: 1, justifyContent: 'center', paddingHorizontal: 32 },
    logoArea: { alignItems: 'center', marginBottom: 48 },
    logo: { fontSize: 64, marginBottom: 12 },
    appName: { color: Colors.textPrimary, fontSize: 28, fontWeight: 'bold', marginBottom: 6 },
    subtitle: { color: Colors.textSecondary, fontSize: 16 },
    form: {},
    label: { color: Colors.textSecondary, fontSize: 14, marginBottom: 8, marginTop: 20 },
    input: {
        backgroundColor: Colors.glassBackground,
        borderColor: Colors.glassBorder,
        borderWidth: 1,
        borderRadius: 10,
        color: Colors.textPrimary,
        padding: 14,
        fontSize: 16,
    },
    btn: {
        backgroundColor: Colors.cyan,
        borderRadius: 10,
        paddingVertical: 16,
        alignItems: 'center',
        marginTop: 28,
    },
    btnDisabled: { opacity: 0.35 },
    btnText: { color: '#000', fontSize: 16, fontWeight: 'bold' },
    note: {
        color: Colors.textSecondary,
        fontSize: 13,
        textAlign: 'center',
        marginTop: 18,
    },
});
