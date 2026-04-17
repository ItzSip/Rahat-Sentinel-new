import React, { memo, useCallback } from 'react';
import { View, StyleSheet, FlatList, Text, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useAlertStore } from '../store/alertStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/ui/GlassCard';
import { PillBadge, SeverityType } from '../components/ui/PillBadge';
import { ActionButton } from '../components/ui/ActionButton';
import { RahatEvent } from '../core/types';
import { emitTestEvent } from '../core/eventEngine';

const AlertRow = memo(({ item }: { item: RahatEvent }) => {
    // Determine severity from type/priority
    let severity: SeverityType = 'WATCH';
    if (item.type === 'SOS') severity = 'CRITICAL';
    else if (item.type === 'LOCATION') severity = 'WARNING';

    const handleShare = useCallback(() => {
        emitTestEvent('SOS'); // Re-broadcast concept
    }, [item]);

    return (
        <GlassCard style={styles.card}>
            <View style={styles.rowHeader}>
                <Text style={styles.title}>{item.type === 'SOS' ? 'SOS Received' : 'Alert Update'}</Text>
                <PillBadge label={severity} type={severity} />
            </View>
            <Text style={styles.text}>Location: {item.id.substring(0,6)}...</Text>
            <Text style={styles.text}>Time left: {item.ttl}s</Text>
            
            <View style={styles.actions}>
                <ActionButton style={styles.actionBtn} label="Share" onPress={handleShare} />
                <ActionButton style={styles.actionBtn} label="Mute" onPress={() => {}} />
            </View>
        </GlassCard>
    );
});

export default function AlertFeedScreen() {
    const navigation = useNavigation();
    const alerts = useAlertStore(state => state.alerts);
    const displayAlerts = alerts.slice(0, 20); // enforce max 20

    const renderItem = useCallback(({ item }: { item: RahatEvent }) => <AlertRow item={item} />, []);
    const keyExtractor = useCallback((item: RahatEvent) => item.id, []);

    return (
        <View style={styles.container}>
            <View style={styles.header}>
                <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
                    <Text style={styles.backText}>← Back</Text>
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Active Alerts</Text>
            </View>
            {displayAlerts.length === 0 ? (
                <View style={styles.emptyState}>
                    <Text style={styles.emptyText}>No active alerts nearby.</Text>
                </View>
            ) : (
                <FlatList
                    data={displayAlerts}
                    renderItem={renderItem}
                    keyExtractor={keyExtractor}
                    contentContainerStyle={styles.listContent}
                    initialNumToRender={5}
                    maxToRenderPerBatch={5}
                    windowSize={3}
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
    actions: { flexDirection: 'row', marginTop: 15, gap: 10 },
    actionBtn: { flex: 1, paddingVertical: 8 },
    emptyState: { flex: 1, alignItems: 'center', justifyContent: 'center' },
    emptyText: { color: Colors.textSecondary, fontSize: 16 }
});
