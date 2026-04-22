import React, { useState, useMemo, memo, useEffect, useCallback, useRef } from 'react';
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
import { NativeModules } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../app/navigation/RootNavigator';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/ui/GlassCard';
import { useDeviceStore } from '../store/deviceStore';
import { useUserStore } from '../store/userStore';
import { emitTestEvent } from '../core/eventEngine';
import { useDisasterStore } from '../store/disasterStore';
import { useDisasterEffect } from '../hooks/useDisasterEffect';
import { useSeverity } from '../hooks/useSeverity';

// ---------------------------------------------------------------------------
// Geo helpers
// ---------------------------------------------------------------------------
function haversineMeters(lat1: number, lng1: number, lat2: number, lng2: number): number {
    const R = 6_371_000;
    const toRad = (v: number) => (v * Math.PI) / 180;
    const dLat = toRad(lat2 - lat1);
    const dLng = toRad(lng2 - lng1);
    const a =
        Math.sin(dLat / 2) ** 2 +
        Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2;
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

// Approximate a circle (in metres) as a 32-point GeoJSON Polygon.
// Coordinates are [lng, lat] as required by the GeoJSON spec.
function createAccuracyCircle(lat: number, lng: number, radiusM: number) {
    const PTS = 32;
    const toRad = (v: number) => (v * Math.PI) / 180;
    const coords: [number, number][] = [];
    for (let i = 0; i <= PTS; i++) {
        const angle = (i / PTS) * 2 * Math.PI;
        const dLat = (radiusM / 111_320) * Math.cos(angle);
        const dLng = (radiusM / (111_320 * Math.cos(toRad(lat)))) * Math.sin(angle);
        coords.push([lng + dLng, lat + dLat]); // [lng, lat] ← GeoJSON order
    }
    return {
        type: 'Feature' as const,
        geometry: { type: 'Polygon' as const, coordinates: [coords] },
        properties: {},
    };
}

const JITTER_M         =  5;  // < 5 m  → pure GPS noise, ignore completely
const SNAP_M           = 10;  // ≥ 10 m from last render → accept, re-render
const DEFAULT_ACC_M    = 30;  // fallback accuracy radius when device doesn't report it

// Severity colour maps — keyed by SeverityLevel string
const SEV_COLOR  = { GREEN: Colors.green,                  ORANGE: Colors.orange,                  RED: Colors.red                  } as const;
const SEV_BG     = { GREEN: 'rgba(52,199,89,0.15)',        ORANGE: 'rgba(255,159,10,0.15)',        RED: 'rgba(255,59,59,0.15)'        } as const;
const SEV_BORDER = { GREEN: 'rgba(52,199,89,0.45)',        ORANGE: 'rgba(255,159,10,0.45)',        RED: 'rgba(255,59,59,0.45)'        } as const;

// MapLibre paint objects extracted to module-level to satisfy react-native/no-inline-styles
const ACCURACY_FILL_STYLE   = { fillColor: 'rgba(50,173,230,0.12)', fillOutlineColor: Colors.cyan };
const PEER_OUTLINE_STYLE    = { circleRadius: 10, circleColor: '#ffffff' };
const PEER_FILL_STYLE       = {
    circleRadius: 7,
    // MapLibre expression — cast needed because TS can't infer nested array as Expression
    circleColor: ['match', ['get', 'severity'], 'HIGH', Colors.red, 'CRITICAL', Colors.red, Colors.orange] as any,
};

type NavigationProp = NativeStackNavigationProp<RootStackParamList, 'Home'>;

const SCREEN_WIDTH = Dimensions.get('window').width;

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

    // Latest accepted GPS fix — null until first real fix arrives.
    // accuracy is in metres; used to draw the uncertainty circle.
    const [userLocation, setUserLocation] = useState<{
        latitude: number;
        longitude: number;
        accuracy: number;
    } | null>(null);
    // lastFixRef: last raw fix received — used for the 5 m jitter guard
    const lastFixRef = useRef<{ latitude: number; longitude: number } | null>(null);
    // lastRenderedRef: last position that triggered a state update — used for the 10 m snap guard
    const lastRenderedRef = useRef<{ latitude: number; longitude: number } | null>(null);

    const peers = useDeviceStore(state => state.peers);
    const isScanning = useDeviceStore(state => state.isScanning);
    const profile = useUserStore(state => state.profile);

    const isDisasterActive = useDisasterStore(s => s.isDisasterActive);
    const severity = useSeverity();
    useDisasterEffect();

    // Peers eligible for map rendering:
    //  • must have coordinates
    //  • location fix must be < 20 s old (stale fixes stay in memory but aren't shown)
    //  • cap at 5 markers for GPU/render budget
    const FRESHNESS_MS = 20_000;
    const renderedPeers = useMemo(() => {
        const now = Date.now();
        return peers
            .filter(p =>
                p.latitude != null &&
                p.longitude != null &&
                p.lastSeen != null &&
                now - p.lastSeen < FRESHNESS_MS,
            )
            .slice(0, 5);
    }, [peers]);

    // GeoJSON polygon representing GPS accuracy uncertainty — recomputed only when
    // userLocation changes (i.e. after a ≥ 10 m snap), not on every map frame.
    const accuracyCircle = useMemo(() => {
        if (userLocation == null) return null;
        return createAccuracyCircle(
            userLocation.latitude,
            userLocation.longitude,
            userLocation.accuracy,
        );
    }, [userLocation]);

    // GeoJSON FeatureCollection for MapLibre GPU rendering
    const peerFeatures = useMemo(() => {
        return {
            type: 'FeatureCollection' as const,
            features: renderedPeers.map(p => ({
                type: 'Feature' as const,
                id: p.id,
                geometry: {
                    type: 'Point' as const,
                    coordinates: [p.longitude!, p.latitude!]
                },
                properties: {
                    id: p.id,
                    name: (p as any).name || `Device`,
                    severity: (p as any).severity || 'NORMAL'
                }
            }))
        };
    }, [renderedPeers]);

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

    // Keep a ref so the 30-min interval always reads the latest fix without stale closure
    const userLocationRef = useRef(userLocation);
    useEffect(() => { userLocationRef.current = userLocation; }, [userLocation]);

    // Periodic location beacon — fires every 30 min while app is open
    useEffect(() => {
        const id = setInterval(() => {
            const loc = userLocationRef.current;
            if (!loc) return;
            const msg = `LAT:${loc.latitude.toFixed(4)},LNG:${loc.longitude.toFixed(4)}`;
            fetch(`http://192.168.4.1/send?data=${encodeURIComponent(msg)}`).catch(() => {});
        }, 30 * 60 * 1000);
        return () => clearInterval(id);
    }, []);

    const handleSOS = useCallback(() => {
        emitTestEvent('SOS');
        const lat = userLocation?.latitude ?? 0;
        const lng = userLocation?.longitude ?? 0;
        const message = userLocation
            ? `LAT:${lat.toFixed(4)},LNG:${lng.toFixed(4)}`
            : 'NO_FIX';
        fetch(`http://192.168.4.1/send?data=${encodeURIComponent(message)}`).catch(() => {});
        navigation.navigate('SOSConfirmation', { message, lat, lng });
    }, [navigation, userLocation]);

    // Continuous location watcher — called by MapLibreGL.UserLocation on every fix.
    // Two-tier filtering keeps the map stable:
    //   < JITTER_M (5 m)  → pure GPS noise, discard entirely
    //   < SNAP_M  (10 m)  → real but small movement, track position but don't re-render
    //   ≥ SNAP_M  (10 m)  → accept fix, update state (triggers re-render + camera move)
    const handleLocationUpdate = useCallback((location: any) => {
        const lat: number | undefined = location?.coords?.latitude;
        const lng: number | undefined = location?.coords?.longitude;
        const accuracy: number = location?.coords?.accuracy ?? DEFAULT_ACC_M;
        if (lat == null || lng == null) return;

        // ── Jitter guard: compare against last raw fix ──────────────────────
        const lastFix = lastFixRef.current;
        if (lastFix != null) {
            const jitter = haversineMeters(lastFix.latitude, lastFix.longitude, lat, lng);
            if (jitter < JITTER_M) return; // noise — don't even update the tracking ref
        }
        lastFixRef.current = { latitude: lat, longitude: lng };

        // ── Snap guard: compare against last rendered position ───────────────
        const lastRendered = lastRenderedRef.current;
        const distFromRender = lastRendered != null
            ? haversineMeters(lastRendered.latitude, lastRendered.longitude, lat, lng)
            : Infinity; // first fix always accepted

        if (distFromRender < SNAP_M) return; // moved, but not enough to re-render

        // ── Accept — update rendered state and notify BLE layer ─────────────
        lastRenderedRef.current = { latitude: lat, longitude: lng };
        setUserLocation({ latitude: lat, longitude: lng, accuracy });

        if (NativeModules.RahatMesh) {
            NativeModules.RahatMesh.updateLocation(lat, lng);
        }
    }, []);

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
                logoEnabled={false}
                attributionEnabled={false}
                rotateEnabled={false}
                pitchEnabled={false}
            >
                {/* Camera only moves when we have a real GPS fix — prevents snapping to (0,0) */}
                {userLocation != null && (
                    <MapLibreGL.Camera
                        zoomLevel={13}
                        centerCoordinate={[userLocation.longitude, userLocation.latitude]}
                        animationDuration={300}
                    />
                )}
                
                {/* OpenStreetMap tile overlay */}
                <MapLibreGL.RasterSource
                    id="osm-source"
                    tileUrlTemplates={['https://tile.openstreetmap.org/{z}/{x}/{y}.png']}
                    tileSize={256}
                >
                    <MapLibreGL.RasterLayer id="osm-layer" sourceID="osm-source" />
                </MapLibreGL.RasterSource>

                {/* Accuracy circle — semi-transparent cyan ring around user position.
                    Rendered below the blue dot so the dot stays visible on top.
                    Radius = reported GPS accuracy (metres), fallback = 30 m. */}
                {accuracyCircle != null && (
                    <MapLibreGL.ShapeSource id="accuracy-source" shape={accuracyCircle}>
                        <MapLibreGL.FillLayer
                            id="accuracy-fill"
                            style={ACCURACY_FILL_STYLE}
                        />
                    </MapLibreGL.ShapeSource>
                )}

                {/* User location — blue dot (MapLibre built-in) */}
                <MapLibreGL.UserLocation
                    visible={true}
                    showsUserHeadingIndicator={true}
                    onUpdate={handleLocationUpdate}
                />

                {/* Peer markers — only visible when disaster mode is active */}
                {isDisasterActive && peerFeatures.features.length > 0 && (
                    <MapLibreGL.ShapeSource id="peers-source" shape={peerFeatures}>
                        <MapLibreGL.CircleLayer
                            id="peers-layer-outline"
                            style={PEER_OUTLINE_STYLE}
                        />
                        <MapLibreGL.CircleLayer
                            id="peers-layer-fill"
                            style={PEER_FILL_STYLE}
                        />
                    </MapLibreGL.ShapeSource>
                )}
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
                    {/* Severity badge — only visible when disaster is active */}
                    {isDisasterActive && (
                        <View style={[
                            styles.severityBadge,
                            { borderColor: SEV_BORDER[severity], backgroundColor: SEV_BG[severity] },
                        ]}>
                            <View style={[styles.severityDot, { backgroundColor: SEV_COLOR[severity] }]} />
                            <Text style={[styles.severityText, { color: SEV_COLOR[severity] }]}>
                                {severity}
                            </Text>
                        </View>
                    )}

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

    // Severity badge in header
    severityBadge: {
        flexDirection: 'row' as const,
        alignItems: 'center' as const,
        gap: 5,
        borderRadius: 14,
        borderWidth: 1,
        paddingHorizontal: 10,
        paddingVertical: 5,
    },
    severityDot: { width: 7, height: 7, borderRadius: 4 },
    severityText: { fontSize: 11, fontWeight: '700' as const, letterSpacing: 0.6 },

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
