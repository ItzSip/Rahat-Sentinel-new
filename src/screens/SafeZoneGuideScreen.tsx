import React, { memo } from 'react';
import { View, StyleSheet, FlatList, Text, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/ui/GlassCard';
import { PillBadge } from '../components/ui/PillBadge';

interface SafeZone {
    id: string;
    name: string;
    distance: string;
    capacity: string;
    type: string;
}

const MOCK_ZONES: SafeZone[] = [
    { id: '1', name: 'Higher Ground - North Ridge', distance: '1.2 km', capacity: 'High', type: 'Elevation Safe' },
    { id: '2', name: 'Central Relief Camp', distance: '3.5 km', capacity: 'Medium', type: 'Relief Camp' }
];

const ZoneCard = memo(({ item }: { item: SafeZone }) => (
    <GlassCard style={styles.card}>
        <View style={styles.rowHeader}>
            <Text style={styles.title}>{item.name}</Text>
        </View>
        <Text style={styles.text}>{item.distance} away</Text>
        <View style={styles.tagRow}>
            <PillBadge label="Verified" type="SAFE" />
            <PillBadge label={`Cap: ${item.capacity}`} type="WATCH" />
            <PillBadge label={item.type} type="BLE" />
        </View>
    </GlassCard>
));

export default function SafeZoneGuideScreen() {
    const navigation = useNavigation();
    return (
        <View style={styles.container}>
            <View style={styles.header}>
                <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
                    <Text style={styles.backText}>← Back</Text>
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Safe Zones</Text>
            </View>

            <FlatList
                data={MOCK_ZONES}
                keyExtractor={i => i.id}
                renderItem={({ item }) => <ZoneCard item={item} />}
                contentContainerStyle={styles.listContent}
                ListHeaderComponent={(
                    <View style={styles.checklistSection}>
                        <Text style={styles.sectionTitle}>What to do now</Text>
                        <GlassCard style={styles.checkCard}>
                            <Text style={styles.checkItem}>✓ Pack essentials (meds, documents)</Text>
                            <Text style={styles.checkItem}>✓ Monitor alerts offline</Text>
                            <Text style={styles.checkItem}>✓ Keep phone charged</Text>
                            <Text style={styles.checkItem}>✓ Inform family (if SMS available)</Text>
                        </GlassCard>
                    </View>
                )}
            />
        </View>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, backgroundColor: Colors.background, paddingTop: 52 },
    header: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16, paddingBottom: 10 },
    backBtn: { paddingRight: 16, paddingVertical: 4 },
    backText: { color: Colors.cyan, fontSize: 16 },
    headerTitle: { color: Colors.textPrimary, fontSize: 22, fontWeight: 'bold' },
    listContent: { padding: 20, paddingBottom: 50 },
    sectionTitle: { color: Colors.textPrimary, fontSize: 18, fontWeight: 'bold', marginBottom: 15 },
    checklistSection: { marginBottom: 30 },
    checkCard: { padding: 20 },
    checkItem: { color: Colors.textSecondary, fontSize: 16, marginBottom: 10 },
    card: { marginBottom: 15 },
    rowHeader: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 5 },
    title: { color: Colors.textPrimary, fontSize: 18, fontWeight: 'bold' },
    text: { color: Colors.textSecondary, fontSize: 14, marginBottom: 15 },
    tagRow: { flexDirection: 'row', gap: 8, flexWrap: 'wrap' }
});
