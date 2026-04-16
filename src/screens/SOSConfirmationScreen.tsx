import React from 'react';
import { View, StyleSheet, Text } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { RootStackParamList } from '../app/navigation/RootNavigator';
import { Colors } from '../theme/colors';
import { ActionButton } from '../components/ui/ActionButton';

export default function SOSConfirmationScreen() {
    const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();

    return (
        <View style={styles.container}>
            <View style={styles.circle}>
                <Text style={styles.check}>✓</Text>
            </View>
            
            <Text style={styles.title}>SOS Sent</Text>
            <Text style={styles.subtitle}>Rescue teams have been alerted</Text>
            
            <View style={styles.statusBox}>
                <Text style={styles.statusText}>• SMS sent securely via offline mesh</Text>
                <Text style={styles.statusText}>• 112 dialled (if network available)</Text>
            </View>

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
        justifyContent: 'center', alignItems: 'center', padding: 20 
    },
    circle: {
        width: 120, height: 120, borderRadius: 60,
        backgroundColor: Colors.green,
        justifyContent: 'center', alignItems: 'center',
        marginBottom: 30,
        elevation: 10, shadowColor: Colors.green, shadowOpacity: 0.5, shadowRadius: 15
    },
    check: { color: '#fff', fontSize: 60, fontWeight: 'bold' },
    title: { color: Colors.textPrimary, fontSize: 32, fontWeight: 'bold', marginBottom: 10 },
    subtitle: { color: Colors.textSecondary, fontSize: 18, marginBottom: 40, textAlign: 'center' },
    statusBox: {
        width: '100%', padding: 20,
        backgroundColor: Colors.glassBackground, borderColor: Colors.glassBorder, borderWidth: 1,
        borderRadius: 12, marginBottom: 40
    },
    statusText: { color: Colors.textPrimary, fontSize: 16, marginBottom: 10 },
    backBtn: { width: '100%', paddingVertical: 16 }
});
