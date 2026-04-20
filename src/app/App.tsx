import React, { useState, useEffect } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { PermissionsAndroid, Platform, LogBox } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import MapLibreGL from '@maplibre/maplibre-react-native';
import RootNavigator from './navigation/RootNavigator';
import AuthScreen from '../screens/AuthScreen';
import { initLocationBridge } from '../core/locationBridge';
import { setTransportStrategy } from '../core/syncLayer';
import { getDeviceId } from '../core/deviceIdentity';

// Disable token requirement — OSM needs no token
MapLibreGL.setAccessToken(null);

// Wire location bridge once — before any component renders.
// Validated LOCATION events from the event engine flow into deviceStore here.
initLocationBridge();

LogBox.ignoreLogs([
    'The result of getSnapshot should be cached',
    'Open debugger to view warnings',
    'MapLibre error',
    '[MapLibre]',
]);

// ─── Permission bootstrap ────────────────────────────────────────────────────

const PERMISSIONS_TO_REQUEST: string[] = [
    PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
];

if (Platform.OS === 'android' && (Platform.Version as number) >= 31) {
    PERMISSIONS_TO_REQUEST.push(PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN);
    PERMISSIONS_TO_REQUEST.push(PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT);
    PERMISSIONS_TO_REQUEST.push(PermissionsAndroid.PERMISSIONS.BLUETOOTH_ADVERTISE);
}

if (Platform.OS === 'android' && (Platform.Version as number) >= 33) {
    PERMISSIONS_TO_REQUEST.push(PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS);
}

const requestPermissions = async () => {
    if (Platform.OS !== 'android') return;
    try {
        await PermissionsAndroid.requestMultiple(
            PERMISSIONS_TO_REQUEST as Parameters<typeof PermissionsAndroid.requestMultiple>[0],
        );
    } catch { /* non-fatal */ }
};

// ─── Root gate ───────────────────────────────────────────────────────────────

const App = () => {
    const [hasOnboarded, setHasOnboarded] = useState<boolean | null>(null);

    useEffect(() => {
        const bootstrap = async () => {
            // 1. Resolve persistent device identity before any events are emitted
            await getDeviceId();

            // 2. Check onboarding state
            try {
                const flag = await AsyncStorage.getItem('has_onboarded');
                setHasOnboarded(flag === 'true');
            } catch {
                setHasOnboarded(false);
            }

            // 3. Request permissions, then start location + BLE
            await requestPermissions();
            MapLibreGL.locationManager.start();

            // 4. Wire transport strategy: outbound events → BLE send.
            //    Must run once, before startScanning, so the channel is ready.
            const { startScanning, bleSend } = await import('../features/bluetoothMesh/bluetoothService');
            setTransportStrategy((frame: string) => {
                bleSend(frame);
            });

            startScanning();
        };

        bootstrap();

        return () => {
            MapLibreGL.locationManager.stop();
            import('../features/bluetoothMesh/bluetoothService').then(({ stopScanning }) => stopScanning());
        };
    }, []);

    if (hasOnboarded === null) return null;

    return (
        <SafeAreaProvider>
            {hasOnboarded ? (
                <NavigationContainer>
                    <RootNavigator />
                </NavigationContainer>
            ) : (
                <AuthScreen
                    onComplete={() => setHasOnboarded(true)}
                />
            )}
        </SafeAreaProvider>
    );
};

export default App;
