import React, { useEffect, useRef, useState } from 'react';
import { View, StyleSheet, Text, Animated } from 'react-native';
import { useNavigation, useRoute, RouteProp } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../app/navigation/RootNavigator';
import { Colors } from '../theme/colors';
import { ActionButton } from '../components/ui/ActionButton';

const NODE_NAME = 'RAHAT_NODE';
const NODE_IP   = '192.168.4.1';

type SOSRoute = RouteProp<RootStackParamList, 'SOSConfirmation'>;

export default function SOSConfirmationScreen() {
    const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
    const route = useRoute<SOSRoute>();
    const { message, lat, lng } = route.params;

    const nodeOpacity   = useRef(new Animated.Value(0)).current;
    const nodeTranslate = useRef(new Animated.Value(16)).current;
    const [nodeStatus, setNodeStatus] = useState<'sending' | 'sent'>('sending');

    useEffect(() => {
        const t1 = setTimeout(() => {
            Animated.parallel([
                Animated.timing(nodeOpacity,   { toValue: 1, duration: 350, useNativeDriver: true }),
                Animated.timing(nodeTranslate, { toValue: 0, duration: 350, useNativeDriver: true }),
            ]).start();
        }, 600);

        const t2 = setTimeout(() => setNodeStatus('sent'), 950);

        return () => { clearTimeout(t1); clearTimeout(t2); };
    }, [nodeOpacity, nodeTranslate]);

    return (
        <View style={styles.container}>
            {/* ── Confirmation circle ── */}
            <View style={styles.circle}>
                <Text style={styles.check}>✓</Text>
            </View>

            <Text style={styles.title}>SOS Sent</Text>
            <Text style={styles.subtitle}>Rescue teams have been alerted</Text>

            {/* ── Mesh status ── */}
            <View style={styles.statusBox}>
                <Text style={styles.statusText}>• Location sent securely via offline mesh</Text>
                <Text style={styles.statusText}>• 112 dialled (if network available)</Text>
            </View>

            {/* ── Rahat Node dispatch card ── */}
            <Animated.View style={[
                styles.nodeCard,
                { opacity: nodeOpacity, transform: [{ translateY: nodeTranslate }] },
            ]}>
                {/* Header row */}
                <View style={styles.nodeHeader}>
                    <View style={styles.nodeIconWrap}>
                        <Text style={styles.nodeIcon}>📡</Text>
                    </View>
                    <View style={{ flex: 1 }}>
                        <Text style={styles.nodeTitle}>RAHAT NODE</Text>
                        <Text style={styles.nodeIp}>{NODE_IP}</Text>
                    </View>
                    <View style={[
                        styles.nodeBadge,
                        nodeStatus === 'sent' ? styles.badgeSent : styles.badgeSending,
                    ]}>
                        <Text style={styles.badgeText}>
                            {nodeStatus === 'sent' ? 'SENT' : 'SENDING…'}
                        </Text>
                    </View>
                </View>

                {/* Divider */}
                <View style={styles.divider} />

                {/* Message rows */}
                <View style={styles.nodeRow}>
                    <Text style={styles.nodeLabel}>Node</Text>
                    <Text style={styles.nodeValue}>{NODE_NAME}</Text>
                </View>
                <View style={styles.nodeRow}>
                    <Text style={styles.nodeLabel}>Latitude</Text>
                    <Text style={[styles.nodeValue, styles.nodeValueCode]}>{lat.toFixed(4)}</Text>
                </View>
                <View style={styles.nodeRow}>
                    <Text style={styles.nodeLabel}>Longitude</Text>
                    <Text style={[styles.nodeValue, styles.nodeValueCode]}>{lng.toFixed(4)}</Text>
                </View>
                <View style={styles.nodeRow}>
                    <Text style={styles.nodeLabel}>Payload</Text>
                    <Text style={[styles.nodeValue, styles.nodeValueCode]}>{message}</Text>
                </View>
                <View style={styles.nodeRow}>
                    <Text style={styles.nodeLabel}>Protocol</Text>
                    <Text style={styles.nodeValue}>Wi-Fi / HTTP</Text>
                </View>
            </Animated.View>

            <ActionButton
                style={styles.backBtn}
                label="Return to Home"
                variant="primary"
                onPress={() => navigation.navigate('Home')}
            />
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1, backgroundColor: Colors.background,
        justifyContent: 'center', alignItems: 'center', padding: 24,
    },
    circle: {
        width: 110, height: 110, borderRadius: 55,
        backgroundColor: Colors.green,
        justifyContent: 'center', alignItems: 'center',
        marginBottom: 24,
        elevation: 12,
        shadowColor: Colors.green, shadowOpacity: 0.55, shadowRadius: 18,
    },
    check: { color: '#fff', fontSize: 56, fontWeight: 'bold' },
    title: { color: Colors.textPrimary, fontSize: 30, fontWeight: 'bold', marginBottom: 8 },
    subtitle: { color: Colors.textSecondary, fontSize: 16, marginBottom: 28, textAlign: 'center' },

    statusBox: {
        width: '100%', padding: 16,
        backgroundColor: Colors.glassBackground,
        borderColor: Colors.glassBorder, borderWidth: 1,
        borderRadius: 12, marginBottom: 16,
    },
    statusText: { color: Colors.textSecondary, fontSize: 14, marginBottom: 6 },

    // ── Node card ──────────────────────────────────────────────
    nodeCard: {
        width: '100%',
        backgroundColor: 'rgba(50,173,230,0.07)',
        borderColor: Colors.cyan,
        borderWidth: 1,
        borderRadius: 14,
        padding: 16,
        marginBottom: 28,
    },
    nodeHeader: {
        flexDirection: 'row', alignItems: 'center', marginBottom: 12,
    },
    nodeIconWrap: {
        width: 40, height: 40, borderRadius: 20,
        backgroundColor: 'rgba(50,173,230,0.15)',
        justifyContent: 'center', alignItems: 'center',
        marginRight: 12,
    },
    nodeIcon: { fontSize: 20 },
    nodeTitle: { color: Colors.cyan, fontSize: 15, fontWeight: '700', letterSpacing: 1 },
    nodeIp: { color: Colors.textSecondary, fontSize: 12, marginTop: 1 },

    nodeBadge: {
        borderRadius: 6, paddingHorizontal: 10, paddingVertical: 4,
    },
    badgeSending: { backgroundColor: 'rgba(255,159,10,0.18)', borderColor: Colors.orange, borderWidth: 1 },
    badgeSent:    { backgroundColor: 'rgba(52,199,89,0.18)',  borderColor: Colors.green,  borderWidth: 1 },
    badgeText: { fontSize: 11, fontWeight: '700', letterSpacing: 0.8, color: Colors.textPrimary },

    divider: {
        height: 1, backgroundColor: 'rgba(50,173,230,0.15)', marginBottom: 12,
    },
    nodeRow: {
        flexDirection: 'row', justifyContent: 'space-between',
        alignItems: 'center', marginBottom: 8,
    },
    nodeLabel: { color: Colors.textSecondary, fontSize: 13 },
    nodeValue: { color: Colors.textPrimary, fontSize: 13, fontWeight: '600' },
    nodeValueCode: {
        color: Colors.cyan, fontFamily: 'monospace',
        fontSize: 13, fontWeight: '700', letterSpacing: 1,
    },

    backBtn: { width: '100%', paddingVertical: 16 },
});
