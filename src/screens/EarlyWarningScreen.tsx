import React, { memo, useState } from 'react';
import { View, StyleSheet, FlatList, Text, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useWarningStore, EarlyWarning } from '../store/warningStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/ui/GlassCard';
import { PillBadge } from '../components/ui/PillBadge';
import { ActionButton } from '../components/ui/ActionButton';

const WarningRow = memo(({ item, isExpanded, onToggle }: { item: EarlyWarning, isExpanded: boolean, onToggle: () => void }) => {
    return (
        <TouchableOpacity activeOpacity={0.8} onPress={onToggle}>
            <GlassCard style={styles.card}>
                <View style={styles.rowHeader}>
                    <Text style={styles.title}>{item.title}</Text>
                    <PillBadge label={item.severity} type={item.severity} />
                </View>
                <Text style={styles.text}>Location: {item.location}</Text>
                <Text style={styles.text}>Time Window: {item.timeWindow}</Text>
                
                {isExpanded && (
                    <View style={styles.details}>
                        <Text style={styles.sectionHeader}>Why this alert?</Text>
                        <Text style={styles.metric}>• Rainfall Anomaly: {item.metrics.rainfallAnomaly}%</Text>
                        <Text style={styles.metric}>• Terrain Instability: {item.metrics.terrainInstability}%</Text>
                        <Text style={styles.metric}>• Historical Match: {item.metrics.historicalMatch}%</Text>
                        <Text style={styles.metric}>• Model Confidence: {item.metrics.modelConfidence}%</Text>

                        <Text style={styles.sectionHeader}>Impact Timeline</Text>
                        <Text style={styles.metric}>Now {'->'} +24h {'->'} +48h {'->'} +72h</Text>

                        <View style={styles.actions}>
                            <ActionButton style={styles.actionBtn} label="Map" onPress={() => {}} />
                            <ActionButton style={styles.actionBtn} label="Safe Zone" onPress={() => {}} />
                            <ActionButton style={styles.actionBtn} label="Save" onPress={() => {}} />
                        </View>
                    </View>
                )}
            </GlassCard>
        </TouchableOpacity>
    );
});

export default function EarlyWarningScreen() {
    const navigation = useNavigation();
    const warnings = useWarningStore(state => state.warnings);
    const [expandedIds, setExpandedIds] = useState<Set<string>>(new Set());

    const toggleExpand = (id: string) => {
        setExpandedIds(prev => {
            const next = new Set(prev);
            if (next.has(id)) next.delete(id);
            else next.add(id);
            return next;
        });
    };

    return (
        <View style={styles.container}>
            <View style={styles.header}>
                <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
                    <Text style={styles.backText}>← Back</Text>
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Early Warnings</Text>
            </View>
            {warnings.length === 0 ? (
                <View style={styles.emptyState}>
                    <Text style={styles.emptyText}>No predictive risks currently detected.</Text>
                </View>
            ) : (
                <FlatList
                    data={warnings}
                    keyExtractor={item => item.id}
                    renderItem={({ item }) => (
                        <WarningRow 
                            item={item} 
                            isExpanded={expandedIds.has(item.id)}
                            onToggle={() => toggleExpand(item.id)} 
                        />
                    )}
                    contentContainerStyle={styles.listContent}
                />
            )}
        </View>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, backgroundColor: Colors.background, paddingTop: 52 },
    header: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16, paddingBottom: 10 },
    backBtn: { paddingRight: 16, paddingVertical: 4 },
    backText: { color: Colors.cyan, fontSize: 16 },
    headerTitle: { color: Colors.textPrimary, fontSize: 22, fontWeight: 'bold' },
    listContent: { padding: 20 },
    card: { marginBottom: 15 },
    rowHeader: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 10 },
    title: { color: Colors.textPrimary, fontSize: 18, fontWeight: 'bold' },
    text: { color: Colors.textSecondary, fontSize: 14, marginBottom: 4 },
    details: { marginTop: 15, paddingTop: 15, borderTopWidth: 1, borderColor: Colors.glassBorder },
    sectionHeader: { color: Colors.textPrimary, fontSize: 16, fontWeight: 'bold', marginBottom: 5, marginTop: 10 },
    metric: { color: Colors.textSecondary, fontSize: 14, marginBottom: 2 },
    actions: { flexDirection: 'row', marginTop: 15, gap: 8 },
    actionBtn: { flex: 1, paddingVertical: 8 },
    emptyState: { flex: 1, alignItems: 'center', justifyContent: 'center' },
    emptyText: { color: Colors.textSecondary, fontSize: 16 }
});
