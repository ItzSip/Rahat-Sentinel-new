import React, { memo } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Colors } from '../../theme/colors';

export type SeverityType = 'CRITICAL' | 'WARNING' | 'WATCH' | 'SAFE' | 'BLE';

interface PillBadgeProps {
    label: string;
    type: SeverityType;
}

const getBadgeColor = (type: SeverityType) => {
    switch (type) {
        case 'CRITICAL': return Colors.red;
        case 'WARNING': return Colors.orange;
        case 'WATCH': return Colors.yellow;
        case 'SAFE': return Colors.green;
        case 'BLE': return Colors.cyan;
        default: return Colors.glassBorder;
    }
};

const PillBadgeComponent = ({ label, type }: PillBadgeProps) => {
    const color = getBadgeColor(type);
    
    return (
        <View style={[styles.badge, { borderColor: color, backgroundColor: `${color}20` }]}>
            <Text style={[styles.text, { color }]}>{label}</Text>
        </View>
    );
};

const styles = StyleSheet.create({
    badge: {
        paddingHorizontal: 12,
        paddingVertical: 4,
        borderRadius: 16,
        borderWidth: 1,
        alignSelf: 'flex-start',
    },
    text: {
        fontSize: 12,
        fontWeight: '700',
        textTransform: 'uppercase',
    },
});

export const PillBadge = memo(PillBadgeComponent);
