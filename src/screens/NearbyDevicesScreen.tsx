import React, { memo, useCallback } from 'react';
import {
    View, StyleSheet, Text, FlatList,
    TouchableOpacity, ActivityIndicator,
} from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useDeviceStore, PeerDevice } from '../store/deviceStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/ui/GlassCard';

// Map signal level strings to colors
const signalColor = (level?: string) => {
    switch (level) {
        case 'VERY_STRONG': return Colors.green;
        case 'STRONG':      return '#6EE7B7';
        case 'MODERATE':    return Colors.orange;
        case 'WEAK':        return Colors.red;
        default:            return Colors.inactive;
    }
};

const trendIcon = (trend?: string) => {
    switch (trend) {
        case 'APPROACHING': return '↑';
        case 'RECEDING':    return '↓';
        default:            return '→';
    }
};

const PeerRow = memo(({ peer }: { peer: PeerDevice & { name?: string; severity?: string; signalLevel?: string; signalTrend?: string } }) => {
    const level = peer.signalLevel;
    const trend = peer.signalTrend;
    const age   = Math.round((Date.now() - peer.lastSeen) / 1000);

    return (
        <GlassCard style={styles.card}>
            <View style={styles.rowTop}>
                <View style={[styles.dot, { backgroundColor: signalColor(level) }]} />
                <Text style={styles.name} numberOfLines={1}>
                    {(peer as any).name || `Device ${peer.id.slice(-4)}`}
                </Text>
                <Text style={[styles.trend, { color: signalColor(level) }]}>
                    {trendIcon(trend)}
                </Text>
            </View>
            <View style={styles.rowMeta}>
                <Text style={styles.meta}>
                    Signal: <Text style={{ color: signalColor(level) }}>{level || '—'}</Text>
                </Text>
                <Text style={styles.meta}>
                    Severity: <Text style={{ color: peer.severity === 'HIGH' ? Colors.red : Colors.green }}>
                        {(peer as any).severity || 'NORMAL'}
                    </Text>
                </Text>
                <Text style={styles.meta}>Last seen: {age}s ago</Text>
            </View>
        </GlassCard>
    );
});

export default function NearbyDevicesScreen() {
    const navigation = useNavigation();
    const peers      = useDeviceStore(s => s.peers);
    const isScanning = useDeviceStore(s => s.isScanning);

    const renderItem = useCallback(({ item }: { item: PeerDevice }) => <PeerRow peer={item as any} />, []);
    const keyExtractor = useCallback((item: PeerDevice) => item.id, []);

    return (
        <View style={styles.container}>
            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
                    <Text style={styles.backText}>← Back</Text>
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Peer Network</Text>
                {isScanning && (
                    <View style={styles.scanBadge}>
                        <ActivityIndicator size="small" color={Colors.cyan} />
                        <Text style={styles.scanText}>Scanning</Text>
                    </View>
                )}
            </View>

            {/* Summary bar */}
            <View style={styles.summaryBar}>
                <Text style={styles.summaryText}>
                    {peers.length} device{peers.length !== 1 ? 's' : ''} found
                </Text>
                <View style={[styles.statusDot, { backgroundColor: isScanning ? Colors.cyan : Colors.inactive }]} />
                <Text style={[styles.summaryText, { color: isScanning ? Colors.cyan : Colors.textSecondary }]}>
                    {isScanning ? 'BLE Active' : 'BLE Idle'}
                </Text>
            </View>

            {peers.length === 0 ? (
                <View style={styles.empty}>
                    <Text style={styles.emptyIcon}>📡</Text>
                    <Text style={styles.emptyTitle}>No Peers Detected</Text>
                    <Text style={styles.emptySubtitle}>
                        {isScanning
                            ? 'BLE mesh is scanning. Other Rahat devices will appear here.'
                            : 'BLE mesh is idle. Start will auto-trigger on next app cycle.'}
                    </Text>
                </View>
            ) : (
                <FlatList
                    data={peers}
                    renderItem={renderItem}
                    keyExtractor={keyExtractor}
                    contentContainerStyle={styles.list}
                    initialNumToRender={8}
                    maxToRenderPerBatch={5}
                    windowSize={5}
                />
            )}
        </View>
    );
}

const styles = StyleSheet.create({
    container:    { flex: 1, backgroundColor: Colors.background, paddingTop: 52 },
    header:       { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16, paddingBottom: 12, gap: 8 },
    backBtn:      { paddingRight: 8, paddingVertical: 4 },
    backText:     { color: Colors.cyan, fontSize: 16 },
    headerTitle:  { flex: 1, color: Colors.textPrimary, fontSize: 22, fontWeight: 'bold' },
    scanBadge:    { flexDirection: 'row', alignItems: 'center', gap: 6 },
    scanText:     { color: Colors.cyan, fontSize: 13 },
    summaryBar: {
        flexDirection: 'row', alignItems: 'center', gap: 8,
        paddingHorizontal: 16, paddingVertical: 10,
        borderBottomWidth: 1, borderColor: Colors.glassBorder,
    },
    summaryText: { color: Colors.textSecondary, fontSize: 14 },
    statusDot:   { width: 8, height: 8, borderRadius: 4 },
    list:        { padding: 16 },
    card:        { marginBottom: 12 },
    rowTop:      { flexDirection: 'row', alignItems: 'center', gap: 10, marginBottom: 8 },
    dot:         { width: 12, height: 12, borderRadius: 6 },
    name:        { flex: 1, color: Colors.textPrimary, fontSize: 16, fontWeight: '600' },
    trend:       { fontSize: 20, fontWeight: 'bold' },
    rowMeta:     { gap: 3 },
    meta:        { color: Colors.textSecondary, fontSize: 13 },
    empty:       { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 40 },
    emptyIcon:   { fontSize: 56, marginBottom: 16 },
    emptyTitle:  { color: Colors.textPrimary, fontSize: 20, fontWeight: 'bold', marginBottom: 8 },
    emptySubtitle: { color: Colors.textSecondary, fontSize: 15, textAlign: 'center', lineHeight: 22 },
});
