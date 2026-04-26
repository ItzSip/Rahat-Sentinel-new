import React, { useState, useMemo, memo, useEffect, useCallback, useRef } from 'react';
import {
    View,
    StyleSheet,
    TouchableOpacity,
    Text,
    Image,
    Pressable,
    Dimensions,
    PermissionsAndroid,
    Platform,
    AppState,
    AppStateStatus,
    Linking,
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
import { useRahatNode } from '../hooks/useRahatNode';
import { useStrings } from '../i18n/strings';
import { useNarrator } from '../hooks/useNarrator';

const { RahatNodeModule } = NativeModules;

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
    // Data-driven: severity string from GeoJSON feature → circle color
    // Values emitted by PeerManager: "GREEN", "ORANGE", "RED", "NORMAL"
    circleColor: ['match', ['get', 'severity'],
        'RED',    Colors.red,
        'ORANGE', Colors.orange,
        'GREEN',  Colors.green,
        Colors.green,  // NORMAL (no disaster active on peer)
    ] as any,
};

// Minimal MapLibre base style — provides a dark background so the GL context
// initialises correctly. OSM raster tiles are added via RasterSource inside MapView.
// Without this, MapLibre v10 renders a black canvas.
const MAP_BASE_STYLE = JSON.stringify({
    version: 8,
    sources: {},
    layers: [{ id: 'bg', type: 'background', paint: { 'background-color': '#040b16' } }],
});

// ── Icon components ──────────────────────────────────────────────────────────
// Drawn with pure Views — no icon library needed.

/** Classic map-pin: circle outline + centre dot + downward triangle tip */
const GpsIcon = ({ color }: { color: string }) => (
    <View style={{ alignItems: 'center', width: 18, height: 24 }}>
        <View style={{
            width: 16, height: 16, borderRadius: 8,
            borderWidth: 2.5, borderColor: color,
            alignItems: 'center', justifyContent: 'center',
        }}>
            <View style={{ width: 5, height: 5, borderRadius: 2.5, backgroundColor: color }} />
        </View>
        {/* pin tip */}
        <View style={{
            width: 0, height: 0, marginTop: -1,
            borderLeftWidth: 4, borderRightWidth: 4, borderTopWidth: 7,
            borderLeftColor: 'transparent', borderRightColor: 'transparent',
            borderTopColor: color,
        }} />
    </View>
);

/** Bluetooth broadcast icon: 3 concentric signal arcs */
const BleIcon = ({ color }: { color: string }) => {
    const arc = (size: number) => ({
        width: size, height: size / 2,
        borderTopLeftRadius: size / 2, borderTopRightRadius: size / 2,
        borderWidth: 2, borderBottomWidth: 0,
        borderColor: color,
        marginBottom: 2,
    });
    return (
        <View style={{ alignItems: 'center', justifyContent: 'flex-end', width: 20, height: 20 }}>
            <View style={arc(14)} />
            <View style={arc(9)} />
            <View style={{ width: 4, height: 4, borderRadius: 2, backgroundColor: color }} />
        </View>
    );
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
    const s = useStrings();

    const go = (screen: keyof RootStackParamList) => {
        onClose();
        nav.navigate(screen as any);
    };

    const NAV_ITEMS: [string, keyof RootStackParamList][] = [
        [s.alertFeed, 'AlertFeed'],
        [s.earlyWarning, 'EarlyWarning'],
        [s.nearbyDevices, 'NearbyDevices'],
        [s.safeZones, 'SafeZoneGuide'],
        [s.settings, 'Settings'],
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
                            <Text style={styles.activeLabel}>{s.activeMode}</Text>
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
                    <Text style={styles.logoutText}>{s.logout}</Text>
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
    const setMyLocation = useDeviceStore(state => state.setMyLocation);
    const profile = useUserStore(state => state.profile);

    const isDisasterActive = useDisasterStore(s => s.isDisasterActive);
    const severity = useSeverity();
    useDisasterEffect(severity);
    useRahatNode(severity);
    const str = useStrings();
    const { speak } = useNarrator();

    // Announce screen on mount if narrator is on
    useEffect(() => { speak(str.screenHome); }, []);

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

    // Worst severity across all nearby peers — drives the pill dot colour.
    // HIGH (distress) → red   |   all NORMAL → green   |   no peers → inactive/cyan
    const worstPeerSeverity = useMemo(() => {
        if (peers.some(p => p.severity === 'RED'))    return 'RED';
        if (peers.some(p => p.severity === 'ORANGE')) return 'ORANGE';
        if (peers.some(p => p.severity === 'GREEN'))  return 'GREEN';
        return peers.length > 0 ? 'NORMAL' : 'NONE';
    }, [peers]);

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

    // Keep refs so intervals always read the latest values without stale closures
    const userLocationRef = useRef(userLocation);
    const severityRef = useRef(severity);
    useEffect(() => { userLocationRef.current = userLocation; }, [userLocation]);
    useEffect(() => { severityRef.current = severity; }, [severity]);

    // Periodic location+severity beacon — fires every 30 min while app is open
    useEffect(() => {
        const id = setInterval(() => {
            const loc = userLocationRef.current;
            if (!loc) return;
            const sev = severityRef.current;
            fetch('http://192.168.4.1/location', {
                method: 'POST',
                body: `${loc.latitude.toFixed(6)},${loc.longitude.toFixed(6)},${sev}`,
            }).catch(() => {});
        }, 30 * 60 * 1000);
        return () => clearInterval(id);
    }, []);

    const handleSOS = useCallback(() => {
        emitTestEvent('SOS');
        const loc = userLocationRef.current;
        const lat = loc?.latitude ?? 0;
        const lng = loc?.longitude ?? 0;
        const sev = severityRef.current;
        // Dual send: BT + HTTP POST via native module (non-blocking)
        if (loc && RahatNodeModule) {
            RahatNodeModule.sendLocation(lat, lng, sev + '_SOS').catch(() => {});
        }
        // Fallback HTTP GET (works even without BT connection)
        fetch(
            `http://192.168.4.1/location`,
            {
                method: 'POST',
                body: `${lat.toFixed(6)},${lng.toFixed(6)},${sev}_SOS`,
            }
        ).catch(() => {});
        const message = loc ? `${lat.toFixed(4)}, ${lng.toFixed(4)}` : 'NO_FIX';
        navigation.navigate('SOSConfirmation', { message, lat, lng });
    }, [navigation]);

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
        setMyLocation({ latitude: lat, longitude: lng }); // shared with disaster hooks

        if (NativeModules.RahatMesh) {
            NativeModules.RahatMesh.updateLocation(lat, lng);
        }
    }, []);

    const handleLocationPress = useCallback(() => {
        if (Platform.OS === 'android') {
            Linking.sendIntent('android.settings.LOCATION_SOURCE_SETTINGS').catch(() => {
                Linking.openSettings();
            });
        } else {
            Linking.openSettings();
        }
    }, []);

    const handleBtPress = useCallback(() => {
        if (Platform.OS === 'android') {
            Linking.sendIntent('android.settings.BLUETOOTH_SETTINGS').catch(() => {
                Linking.openSettings();
            });
        } else {
            Linking.openSettings();
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
            {/* Full-screen OSM map — no Google dependencies.
                styleURL provides a minimal base style so the GL context
                initialises (without it MapLibre v10 renders a black canvas). */}
            <MapLibreGL.MapView
                style={StyleSheet.absoluteFillObject}
                mapStyle={MAP_BASE_STYLE}
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

                <View style={styles.rightIcons}>
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

                    {/* Location status button — tap opens device location settings */}
                    <TouchableOpacity
                        style={[
                            styles.statusBtn,
                            locationOn ? styles.statusBtnGpsOn : styles.statusBtnOff,
                        ]}
                        onPress={handleLocationPress}
                        activeOpacity={0.75}
                    >
                        <GpsIcon color={locationOn ? Colors.green : Colors.inactive} />
                        <Text style={[
                            styles.statusLabel,
                            { color: locationOn ? Colors.green : Colors.inactive },
                        ]}>GPS</Text>
                    </TouchableOpacity>

                    {/* BLE status button — tap opens device Bluetooth settings */}
                    <TouchableOpacity
                        style={[
                            styles.statusBtn,
                            btOn ? styles.statusBtnBleOn : styles.statusBtnOff,
                        ]}
                        onPress={handleBtPress}
                        activeOpacity={0.75}
                    >
                        <BleIcon color={btOn ? Colors.cyan : Colors.inactive} />
                        <Text style={[
                            styles.statusLabel,
                            { color: btOn ? Colors.cyan : Colors.inactive },
                        ]}>BLE</Text>
                    </TouchableOpacity>
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
                        {
                            backgroundColor:
                                worstPeerSeverity === 'RED'    ? Colors.red    :
                                worstPeerSeverity === 'ORANGE' ? Colors.orange :
                                worstPeerSeverity === 'GREEN'  ? Colors.green  :
                                worstPeerSeverity === 'NORMAL' ? Colors.green  :
                                isScanning                     ? Colors.cyan   :
                                Colors.inactive,
                        },
                    ]} />
                    <Text style={styles.nearbyPillText}>
                        {peers.length > 0
                            ? str.nearbyCount(peers.length)
                            : isScanning ? str.scanning : str.nearbyHelp}
                    </Text>
                </TouchableOpacity>

                {/* RIGHT: Alert Feed + SOS */}
                <View style={styles.rightCluster} pointerEvents="box-none">
                    <TouchableOpacity
                        style={styles.alertBtn}
                        onPress={() => navigation.navigate('AlertFeed')}
                        activeOpacity={0.85}
                    >
                        <Text style={styles.alertBtnText}>{str.alerts}</Text>
                    </TouchableOpacity>

                    <Pressable
                        onPress={handleSOS}
                        style={({ pressed }) => [styles.sosBtn, pressed && styles.sosBtnPressed]}
                    >
                        <Image
                            source={require('../assets/sos_snitch.png')}
                            style={styles.sosBtnImage}
                            resizeMode="contain"
                        />
                    </Pressable>
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
        backgroundColor: 'rgba(11,11,15,0.82)',
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
    rightIcons: { flexDirection: 'row', gap: 8, alignItems: 'center' },
    statusBtn: {
        width: 48,
        height: 52,
        borderRadius: 14,
        borderWidth: 1,
        alignItems: 'center',
        justifyContent: 'center',
        gap: 4,
        paddingVertical: 6,
    },
    statusBtnOff: {
        backgroundColor: Colors.glassBackground,
        borderColor: Colors.glassBorder,
    },
    statusBtnGpsOn: {
        backgroundColor: 'rgba(52,199,89,0.12)',
        borderColor: 'rgba(52,199,89,0.5)',
    },
    statusBtnBleOn: {
        backgroundColor: 'rgba(50,173,230,0.12)',
        borderColor: 'rgba(50,173,230,0.5)',
    },
    statusLabel: {
        fontSize: 9,
        fontWeight: '700' as const,
        letterSpacing: 0.5,
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
        backgroundColor: 'rgba(11,11,15,0.92)',
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
        backgroundColor: 'rgba(11,11,15,0.92)',
        borderWidth: 1,
        borderColor: Colors.orange,
        borderRadius: 22,
        paddingHorizontal: 16,
        paddingVertical: 12,
    },
    alertBtnText: { color: Colors.orange, fontSize: 14, fontWeight: '700' },
    sosBtn: {
        width: 140,
        height: 140,
        alignItems: 'center',
        justifyContent: 'center',
    },
    sosBtnPressed: {
        opacity: 0.75,
    },
    sosBtnImage: {
        width: 140,
        height: 140,
    },

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
        backgroundColor: Colors.primary,
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
