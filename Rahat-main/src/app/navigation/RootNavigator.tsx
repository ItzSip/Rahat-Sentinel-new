import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import AlertStack from './AlertStack';
import SettingsStack from './SettingsStack';

export type RootStackParamList = {
  Alerts: undefined;
  Settings: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function RootNavigator() {
  return (
    <Stack.Navigator
      initialRouteName='Alerts'
      screenOptions={{
        headerShown: false,
      }}>
      <Stack.Screen name='Alerts' component={AlertStack} />
      <Stack.Screen name='Settings' component={SettingsStack} />
    </Stack.Navigator>
  );
}
