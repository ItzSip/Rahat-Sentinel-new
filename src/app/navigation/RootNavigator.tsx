import React, { useEffect, useState } from 'react';
import { View, ActivityIndicator } from 'react-native';
import { createNativeStackNavigator, NativeStackScreenProps } from '@react-navigation/native-stack';
import AsyncStorage from '@react-native-async-storage/async-storage';

import { Colors } from '../../theme/colors';
import { useUserStore } from '../../store/userStore';

// Screens
import AuthScreen from '../../screens/AuthScreen';
import HomeScreen from '../../screens/HomeScreen';
import AlertFeedScreen from '../../screens/AlertFeedScreen';
import EarlyWarningScreen from '../../screens/EarlyWarningScreen';
import NearbyDevicesScreen from '../../screens/NearbyDevicesScreen';
import SafeZoneGuideScreen from '../../screens/SafeZoneGuideScreen';
import SOSConfirmationScreen from '../../screens/SOSConfirmationScreen';
import ProfileScreen from '../../screens/ProfileScreen';
import SettingsScreen from '../../screens/SettingsScreen';

export type RootStackParamList = {
    Auth: undefined;
    Home: undefined;
    AlertFeed: undefined;
    EarlyWarning: undefined;
    NearbyDevices: undefined;
    SafeZoneGuide: undefined;
    SOSConfirmation: { message: string; lat: number; lng: number };
    Profile: undefined;
    Settings: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();

// Adapter: gives AuthScreen the onComplete callback it expects
function AuthScreenAdapter({ navigation }: NativeStackScreenProps<RootStackParamList, 'Auth'>) {
    return <AuthScreen onComplete={() => navigation.replace('Home')} />;
}

export default function RootNavigator() {
    const [initialRoute, setInitialRoute] = useState<'Auth' | 'Home' | null>(null);
    const setProfile = useUserStore(state => state.setProfile);

    useEffect(() => {
        (async () => {
            try {
                const launched = await AsyncStorage.getItem('@rahat_launched');
                if (launched === 'true') {
                    const name = await AsyncStorage.getItem('@rahat_name');
                    const phone = await AsyncStorage.getItem('@rahat_phone');
                    if (name) setProfile({ name, phone: phone ?? '' });
                    setInitialRoute('Home');
                } else {
                    setInitialRoute('Auth');
                }
            } catch {
                setInitialRoute('Auth');
            }
        })();
    }, [setProfile]);

    if (!initialRoute) {
        return (
            <View style={{ flex: 1, backgroundColor: Colors.background, alignItems: 'center', justifyContent: 'center' }}>
                <ActivityIndicator color={Colors.primary} size="large" />
            </View>
        );
    }

    return (
        <Stack.Navigator
            initialRouteName={initialRoute}
            screenOptions={{
                headerShown: false,
                animation: 'fade',
            }}
        >
            <Stack.Screen name="Auth" component={AuthScreenAdapter} />
            <Stack.Screen name="Home" component={HomeScreen} />
            <Stack.Screen name="AlertFeed" component={AlertFeedScreen} />
            <Stack.Screen name="EarlyWarning" component={EarlyWarningScreen} />
            <Stack.Screen name="NearbyDevices" component={NearbyDevicesScreen} />
            <Stack.Screen name="SafeZoneGuide" component={SafeZoneGuideScreen} />
            <Stack.Screen name="SOSConfirmation" component={SOSConfirmationScreen} />
            <Stack.Screen name="Profile" component={ProfileScreen} />
            <Stack.Screen name="Settings" component={SettingsScreen} />
        </Stack.Navigator>
    );
}
