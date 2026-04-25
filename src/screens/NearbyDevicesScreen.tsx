import React, { memo, useCallback, useEffect } from 'react';
import {
    View, StyleSheet, Text, FlatList,
    TouchableOpacity, ActivityIndicator,
} from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useDeviceStore, PeerDevice } from '../store/deviceStore';
import { useDisasterStore } from '../store/disasterStore';
import { useSeverity } from '../hooks/useSeverity';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/ui/GlassCard';
import { useStrings } from '../i18n/strings';
import { useNarrator } from '../hooks/useNarrator';

const SEV_COLOR = { GREEN: Colors.green, ORANGE: Colors.orange, RED: Colors.red } as const;
const SEV_BG    = { GREEN: 'rgba(52,199,89,0.12)', ORANGE: 'rgba(255,159,10,0.12)', RED: 'rgba(255,59,59,0.12)' } as const;

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

const PeerRow = memo(({ peer, showLocation }: { peer: PeerDevice & { name?: string; severity?: string; signalLevel?: string; signalTrend?: string }; showLocation: boolean }) => {
    const level = peer.signalLevel;
    const trend = peer.signalTrend;
    const age   = Math.round((Date.now() - peer.lastSeen) / 1000);
    const hasLoc = peer.latitude != null && peer.longitude != null;

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
                {showLocation && hasLoc && (
                    <Text style={styles.meta}>
                        Location:{' '}
                        <Text style={{ color: Colors.primary }}>
                            {peer.latitude!.toFixed(5)}, {peer.longitude!.toFixed(5)}
                        </Text>
                    </Text>
                )}
                {showLocation && !hasLoc && (
                    <Text style={styles.meta}>Location: <Text style={{ color: Colors.inactive }}>no fix yet</Text></Text>
                )}
            </View>
        </GlassCard>
    );
});

export default function NearbyDevicesScreen() {
    const navigation       = useNavigation();
    const peers            = useDeviceStore(s => s.peers);
    const isScanning       = useDeviceStore(s => s.isScanning);
    const isDisasterActive = useDisasterStore(s => s.isDisasterActive);
    const severity         = useSeverity();
    const str              = useStrings();
    const { speak }        = useNarrator();

    useEffect(() => { speak(str.screenNearbyDevices); }, []);

    const renderItem = useCallback(({ item }: { item: PeerDevice }) => <PeerRow peer={item as any} showLocation={isDisasterActive} />, [isDisasterActive]);
    const keyExtractor = useCallback((item: PeerDevice) => item.id, []);

    return (
        <View style={styles.container}>
            {/* Header */}
            <View style={styles.header}>
                <TouchableOpacity onPress={() => navigation.goBack()} style={styles.backBtn}>
                    <Text style={styles.backText}>{str.back}</Text>
                </TouchableOpacity>
                <Text style={styles.headerTitle}>{str.peerNetwork}</Text>
                {isScanning && (
                    <View style={styles.scanBadge}>
                        <ActivityIndicator size="small" color={Colors.cyan} />
                        <Text style={styles.scanText}>Scanning</Text>
                    </View>
                )}
            </View>

            {/* Own severity banner — only shown when disaster is active */}
            {isDisasterActive && (
                <View style={[styles.severityBanner, { backgroundColor: SEV_BG[severity] }]}>
                    <View style={[styles.severityDot, { backgroundColor: SEV_COLOR[severity] }]} />
                    <Text style={[styles.severityLabel, { color: SEV_COLOR[severity] }]}>
                        Your status: {severity}
                    </Text>
                    <Text style={styles.severityHint}>
                        {severity === 'GREEN'  && '— shake phone or say YES to reset timer'}
                        {severity === 'ORANGE' && '— check in soon to avoid RED status'}
                        {severity === 'RED'    && '— help has been alerted'}
                    </Text>
                </View>
            )}

            {/* Summary bar */}
            <View style={styles.summaryBar}>
                <Text style={styles.summaryText}>{str.devicesFound(peers.length)}</Text>
                <View style={[styles.statusDot, { backgroundColor: isScanning ? Colors.cyan : Colors.inactive }]} />
                <Text style={[styles.summaryText, { color: isScanning ? Colors.cyan : Colors.textSecondary }]}>
                    {isScanning ? str.bleActive : str.bleIdle}
                </Text>
            </View>

            {peers.length === 0 ? (
                <View style={styles.empty}>
                    <Text style={styles.emptyIcon}>📡</Text>
                    <Text style={styles.emptyTitle}>{str.noPeers}</Text>
                    <Text style={styles.emptySubtitle}>
                        {isScanning ? str.noPeersBleHint : str.noPeersIdleHint}
                    </Text>
                    {!isDisasterActive && (
                        <Text style={[styles.emptySubtitle, { marginTop: 8, color: Colors.inactive }]}>
                            Activate disaster mode to see device locations on the map.
                        </Text>
                    )}
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
    backText:     { color: Colors.primary, fontSize: 16 },
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
    severityBanner: {
        flexDirection: 'row', alignItems: 'center', flexWrap: 'wrap', gap: 8,
        paddingHorizontal: 16, paddingVertical: 10,
        borderBottomWidth: 1, borderColor: 'rgba(255,255,255,0.06)',
    },
    severityDot:  { width: 9, height: 9, borderRadius: 5 },
    severityLabel:{ fontSize: 13, fontWeight: '700' },
    severityHint: { color: Colors.textSecondary, fontSize: 12, flex: 1 },
    empty:       { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 40 },
    emptyIcon:   { fontSize: 56, marginBottom: 16 },
    emptyTitle:  { color: Colors.textPrimary, fontSize: 20, fontWeight: 'bold', marginBottom: 8 },
    emptySubtitle: { color: Colors.textSecondary, fontSize: 15, textAlign: 'center', lineHeight: 22 },
});
