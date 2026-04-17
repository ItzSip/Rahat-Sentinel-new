import React, { useState, useMemo, memo, useEffect, useCallback } from 'react';
import {
    View,
    StyleSheet,
    TouchableOpacity,
    Text,
    Dimensions,
    PermissionsAndroid,
    Platform,
    AppState,
    AppStateStatus,
} from 'react-native';
import MapLibreGL from '@maplibre/maplibre-react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../app/navigation/RootNavigator';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/ui/GlassCard';
import { useDeviceStore } from '../store/deviceStore';
import { useUserStore } from '../store/userStore';
import { emitTestEvent } from '../core/eventEngine';

type NavigationProp = NativeStackNavigationProp<RootStackParamList, 'Home'>;

const SCREEN_WIDTH = Dimensions.get('window').width;

// ---------------------------------------------------------------------------
// Custom map markers — memoized, no re-renders on map pan/zoom
// ---------------------------------------------------------------------------
const PeerMarker = memo(() => <View style={markerStyles.peerDot} />);
const UserMarker = memo(() => <View style={markerStyles.userDot} />);

const markerStyles = StyleSheet.create({
    peerDot: {
        width: 14,
        height: 14,
        borderRadius: 7,
        backgroundColor: Colors.orange,
        borderWidth: 2,
        borderColor: '#fff',
        elevation: 2,
    },
    userDot: {
        width: 16,
        height: 16,
        borderRadius: 8,
        backgroundColor: Colors.cyan,
        borderWidth: 2,
        borderColor: '#fff',
        elevation: 2,
    },
});

// ---------------------------------------------------------------------------
// Sidebar drawer
// ---------------------------------------------------------------------------
interface SidebarProps {
    visible: boolean;
    onClose: () => void;
    nav: NavigationProp;
    profile: { name: string; phone: string };
    onLogout: () => void;
}

const Sidebar = memo(({ visible, onClose, nav, profile, onLogout }: SidebarProps) => {
    if (!visible) return null;

    const go = (screen: keyof RootStackParamList) => {
        onClose();
        nav.navigate(screen as any);
    };

    const NAV_ITEMS: [string, keyof RootStackParamList][] = [
        ['Alert Feed', 'AlertFeed'],
        ['Early Warning', 'EarlyWarning'],
        ['Nearby Devices', 'NearbyDevices'],
        ['Safe Zones', 'SafeZoneGuide'],
        ['Settings', 'Settings'],
    ];

    return (
        <View style={styles.sidebarOverlay}>
            <TouchableOpacity style={styles.sidebarDim} onPress={onClose} activeOpacity={1} />
            <View style={styles.sidebarPanel}>
                {/* Profile card — tap to open Profile screen */}
                <TouchableOpacity activeOpacity={0.85} onPress={() => go('Profile')}>
                    <GlassCard style={styles.profileCard}>
                        <View style={styles.avatarCircle}>
                            <Text style={styles.avatarLetter}>
                                {profile.name?.charAt(0)?.toUpperCase() || 'U'}
                            </Text>
                        </View>
                        <Text style={styles.profileName} numberOfLines={1}>
                            {profile.name || 'User'}
                        </Text>
                        <View style={styles.activeBadge}>
                            <View style={styles.activePulse} />
                            <Text style={styles.activeLabel}>Active Mode</Text>
                        </View>
                    </GlassCard>
                </TouchableOpacity>

                {/* Navigation items */}
                <View style={styles.navList}>
                    {NAV_ITEMS.map(([label, screen]) => (
                        <TouchableOpacity
                            key={screen}
                            style={styles.navItem}
                            onPress={() => go(screen)}
                            activeOpacity={0.7}
                        >
                            <Text style={styles.navItemText}>{label}</Text>
                            <Text style={styles.navChevron}>›</Text>
                        </TouchableOpacity>
                    ))}
                </View>

                {/* Logout */}
                <TouchableOpacity style={styles.logoutBtn} onPress={onLogout} activeOpacity={0.75}>
                    <Text style={styles.logoutText}>Logout</Text>
                </TouchableOpacity>
            </View>
        </View>
    );
});

// ---------------------------------------------------------------------------
// HomeScreen
// ---------------------------------------------------------------------------
export default function HomeScreen() {
    const navigation = useNavigation<NavigationProp>();
    const [sidebarVisible, setSidebarVisible] = useState(false);
    const [locationOn, setLocationOn] = useState(false);
    const [btOn, setBtOn] = useState(false);

    const peers = useDeviceStore(state => state.peers);
    const isScanning = useDeviceStore(state => state.isScanning);
    const profile = useUserStore(state => state.profile);

    // Max 10 markers, must have coordinates
    const renderedPeers = useMemo(
        () => peers.filter(p => p.latitude != null && p.longitude != null).slice(0, 10),
        [peers],
    );

    // Check real device permission state — proxy for "enabled" without native polling
    const refreshDeviceState = useCallback(async () => {
        if (Platform.OS !== 'android') return;
        try {
            const locOk = await PermissionsAndroid.check(
                PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
            );
            setLocationOn(locOk);

            if ((Platform.Version as number) >= 31) {
                const btOk = await PermissionsAndroid.check(
                    PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
                );
                setBtOn(btOk || isScanning);
            } else {
                // On Android < 12 BT is an install-time permission; use scanning as proxy
                setBtOn(isScanning);
            }
        } catch { /* silent */ }
    }, [isScanning]);

    useEffect(() => {
        refreshDeviceState();
        const sub = AppState.addEventListener('change', (state: AppStateStatus) => {
            if (state === 'active') refreshDeviceState();
        });
        return () => sub.remove();
    }, [refreshDeviceState]);

    // Mirror scanning state into BT indicator without a poll loop
    useEffect(() => {
        if (isScanning) setBtOn(true);
    }, [isScanning]);

    const handleSOS = useCallback(() => {
        emitTestEvent('SOS');
        navigation.navigate('SOSConfirmation');
    }, [navigation]);

    const handleLogout = useCallback(async () => {
        setSidebarVisible(false);
        await AsyncStorage.removeItem('@rahat_name');
        await AsyncStorage.removeItem('@rahat_phone');
        await AsyncStorage.removeItem('@rahat_launched');
        navigation.reset({ index: 0, routes: [{ name: 'Auth' }] });
    }, [navigation]);

    return (
        <View style={styles.container}>
            {/* Full-screen OSM map — no Google dependencies */}
            <MapLibreGL.MapView
                style={StyleSheet.absoluteFillObject}
                styleJSON={JSON.stringify({ version: 8, sources: {}, layers: [] })}
                logoEnabled={false}
                attributionEnabled={false}
                rotateEnabled={false}
                pitchEnabled={false}
            >
                <MapLibreGL.Camera
                    zoomLevel={13}
                    followUserLocation={true}
                    animationDuration={0}
                />
                
                {/* OpenStreetMap tile overlay */}
                <MapLibreGL.RasterSource
                    id="osm-source"
                    tileUrlTemplates={['https://tile.openstreetmap.org/{z}/{x}/{y}.png']}
                    tileSize={256}
                >
                    <MapLibreGL.RasterLayer id="osm-layer" sourceID="osm-source" />
                </MapLibreGL.RasterSource>

                {/* Real User Location */}
                <MapLibreGL.UserLocation
                    visible={true}
                    showsUserHeadingIndicator={true}
                />

                {/* Peers — orange dots, hard cap 10 */}
                {renderedPeers.map(p => (
                    <MapLibreGL.PointAnnotation
                        key={p.id}
                        id={`peer-${p.id}`}
                        coordinate={[p.longitude!, p.latitude!]}
                    >
                        <PeerMarker />
                    </MapLibreGL.PointAnnotation>
                ))}
            </MapLibreGL.MapView>

            {/* ── Top header overlay ── */}
            <View style={styles.header} pointerEvents="box-none">
                <TouchableOpacity style={styles.headerBtn} onPress={() => setSidebarVisible(true)}>
                    <Text style={styles.hamburger}>☰</Text>
                </TouchableOpacity>

                <View pointerEvents="none" style={styles.titleWrap}>
                    <Text style={styles.titleText}>Rahat</Text>
                </View>

                <View style={styles.rightIcons} pointerEvents="none">
                    {/* Location status — display only, no touch */}
                    <View style={[styles.statusIcon, locationOn && styles.statusIconOn]}>
                        <Text style={styles.statusEmoji}>📍</Text>
                        <View style={[
                            styles.statusDot,
                            { backgroundColor: locationOn ? Colors.green : Colors.inactive },
                        ]} />
                    </View>

                    {/* Bluetooth status — display only, no touch */}
                    <View style={[styles.statusIcon, btOn && styles.statusIconOn]}>
                        <Text style={styles.statusEmoji}>⬡</Text>
                        <View style={[
                            styles.statusDot,
                            { backgroundColor: btOn ? Colors.cyan : Colors.inactive },
                        ]} />
                    </View>
                </View>
            </View>

            {/* ── Bottom overlay ── */}
            <View style={styles.bottomOverlay} pointerEvents="box-none">
                {/* LEFT: Nearby Help pill */}
                <TouchableOpacity
                    style={styles.nearbyPill}
                    onPress={() => navigation.navigate('NearbyDevices')}
                    activeOpacity={0.85}
                >
                    <View style={[
                        styles.pillDot,
                        { backgroundColor: isScanning ? Colors.cyan : Colors.green },
                    ]} />
                    <Text style={styles.nearbyPillText}>
                        {isScanning
                            ? 'Scanning...'
                            : `Nearby Help${peers.length > 0 ? ` · ${peers.length}` : ''}`}
                    </Text>
                </TouchableOpacity>

                {/* RIGHT: Alert Feed + SOS */}
                <View style={styles.rightCluster} pointerEvents="box-none">
                    <TouchableOpacity
                        style={styles.alertBtn}
                        onPress={() => navigation.navigate('AlertFeed')}
                        activeOpacity={0.85}
                    >
                        <Text style={styles.alertBtnText}>Alerts</Text>
                    </TouchableOpacity>

                    <TouchableOpacity style={styles.sosBtn} onPress={handleSOS} activeOpacity={0.8}>
                        <Text style={styles.sosBtnText}>SOS</Text>
                    </TouchableOpacity>
                </View>
            </View>

            {/* ── Sidebar ── */}
            <Sidebar
                visible={sidebarVisible}
                onClose={() => setSidebarVisible(false)}
                nav={navigation}
                profile={profile}
                onLogout={handleLogout}
            />
        </View>
    );
}

// ---------------------------------------------------------------------------
// Styles
// ---------------------------------------------------------------------------
const styles = StyleSheet.create({
    container: { flex: 1, backgroundColor: Colors.background },

    // Header
    header: {
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        paddingTop: 48,
        paddingHorizontal: 16,
        paddingBottom: 12,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        backgroundColor: 'rgba(4,11,22,0.72)',
        zIndex: 10,
    },
    headerBtn: {
        width: 44,
        height: 44,
        borderRadius: 22,
        backgroundColor: Colors.glassBackground,
        borderWidth: 1,
        borderColor: Colors.glassBorder,
        alignItems: 'center',
        justifyContent: 'center',
    },
    hamburger: { color: Colors.textPrimary, fontSize: 22 },
    titleWrap: { flex: 1, alignItems: 'center' },
    titleText: { color: Colors.textPrimary, fontSize: 20, fontWeight: 'bold', letterSpacing: 1 },
    rightIcons: { flexDirection: 'row', gap: 8 },
    statusIcon: {
        width: 44,
        height: 44,
        borderRadius: 22,
        backgroundColor: Colors.glassBackground,
        borderWidth: 1,
        borderColor: Colors.glassBorder,
        alignItems: 'center',
        justifyContent: 'center',
    },
    statusIconOn: {
        borderColor: Colors.cyan,
        backgroundColor: 'rgba(50,173,230,0.15)',
    },
    statusEmoji: { fontSize: 18 },
    statusDot: {
        position: 'absolute',
        bottom: 6,
        right: 6,
        width: 8,
        height: 8,
        borderRadius: 4,
    },

    // Bottom overlay
    bottomOverlay: {
        position: 'absolute',
        bottom: 32,
        left: 16,
        right: 16,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        zIndex: 10,
    },
    nearbyPill: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: 'rgba(4,11,22,0.88)',
        borderWidth: 1,
        borderColor: Colors.glassBorder,
        borderRadius: 24,
        paddingHorizontal: 16,
        paddingVertical: 12,
        gap: 8,
        maxWidth: SCREEN_WIDTH * 0.48,
    },
    pillDot: { width: 8, height: 8, borderRadius: 4 },
    nearbyPillText: { color: Colors.textPrimary, fontSize: 14, fontWeight: '600' },
    rightCluster: { flexDirection: 'row', alignItems: 'center', gap: 10 },
    alertBtn: {
        backgroundColor: 'rgba(4,11,22,0.88)',
        borderWidth: 1,
        borderColor: Colors.orange,
        borderRadius: 22,
        paddingHorizontal: 16,
        paddingVertical: 12,
    },
    alertBtnText: { color: Colors.orange, fontSize: 14, fontWeight: '700' },
    sosBtn: {
        width: 68,
        height: 68,
        borderRadius: 34,
        backgroundColor: Colors.red,
        alignItems: 'center',
        justifyContent: 'center',
        elevation: 8,
        shadowColor: Colors.red,
        shadowOpacity: 0.6,
        shadowRadius: 12,
        shadowOffset: { width: 0, height: 0 },
    },
    sosBtnText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },

    // Sidebar
    sidebarOverlay: {
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        flexDirection: 'row',
        zIndex: 999,
    },
    sidebarDim: { flex: 1, backgroundColor: 'rgba(0,0,0,0.55)' },
    sidebarPanel: {
        position: 'absolute',
        left: 0,
        top: 0,
        bottom: 0,
        width: SCREEN_WIDTH * 0.72,
        backgroundColor: Colors.navBackground,
        paddingTop: 52,
        paddingHorizontal: 20,
        borderRightWidth: 1,
        borderColor: Colors.glassBorder,
        zIndex: 1000,
    },

    // Profile card inside sidebar
    profileCard: { marginBottom: 24, padding: 16 },
    avatarCircle: {
        width: 52,
        height: 52,
        borderRadius: 26,
        backgroundColor: Colors.cyan,
        alignItems: 'center',
        justifyContent: 'center',
        marginBottom: 10,
    },
    avatarLetter: { color: '#000', fontSize: 24, fontWeight: 'bold' },
    profileName: {
        color: Colors.textPrimary,
        fontSize: 17,
        fontWeight: 'bold',
        marginBottom: 6,
    },
    activeBadge: { flexDirection: 'row', alignItems: 'center', gap: 6 },
    activePulse: { width: 8, height: 8, borderRadius: 4, backgroundColor: Colors.green },
    activeLabel: { color: Colors.green, fontSize: 13, fontWeight: '600' },

    // Nav list
    navList: { flex: 1 },
    navItem: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingVertical: 15,
        borderBottomWidth: 1,
        borderColor: Colors.glassBorder,
    },
    navItemText: { color: Colors.textPrimary, fontSize: 16 },
    navChevron: { color: Colors.textSecondary, fontSize: 22 },

    // Logout
    logoutBtn: {
        marginBottom: 40,
        paddingVertical: 14,
        borderWidth: 1,
        borderColor: Colors.red,
        borderRadius: 10,
        alignItems: 'center',
    },
    logoutText: { color: Colors.red, fontSize: 16, fontWeight: '600' },
});
