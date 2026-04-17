import React from 'react';
import { View, StyleSheet, Text, Switch, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useSettingsStore } from '../store/settingsStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/ui/GlassCard';

export default function SettingsScreen() {
    const navigation = useNavigation();
    const {
        narratorEnabled, darkMode, language,
        setNarrator, setDarkMode, setLanguage
    } = useSettingsStore();

    const renderSettingRow = (title: string, value: boolean, onToggle: (val: boolean) => void) => (
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
        <View style={styles.container}>
            <View style={styles.header}>
                <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
                    <Text style={styles.backText}>← Back</Text>
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Settings</Text>
            </View>
            
            <GlassCard style={styles.card}>
                <Text style={styles.sectionTitle}>Accessibility</Text>
                {renderSettingRow('Screen Narrator', narratorEnabled, setNarrator)}
                
                <View style={styles.divider} />
                
                <Text style={styles.sectionTitle}>General</Text>
                {renderSettingRow('Dark Mode Theme', darkMode, setDarkMode)}
                
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
        </View>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, backgroundColor: Colors.background, paddingTop: 52, paddingHorizontal: 20 },
    header: { flexDirection: 'row', alignItems: 'center', marginBottom: 20 },
    backBtn: { paddingRight: 16, paddingVertical: 4 },
    backText: { color: Colors.cyan, fontSize: 16 },
    headerTitle: { color: Colors.textPrimary, fontSize: 22, fontWeight: 'bold' },
    card: { padding: 20 },
    sectionTitle: { color: Colors.cyan, fontSize: 14, fontWeight: 'bold', textTransform: 'uppercase', marginBottom: 15 },
    row: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 },
    rowTitle: { color: Colors.textPrimary, fontSize: 16 },
    divider: { height: 1, backgroundColor: Colors.glassBorder, marginVertical: 10, marginBottom: 20 },
    langText: { color: Colors.cyan, fontSize: 16, fontWeight: 'bold', padding: 5 }
});
