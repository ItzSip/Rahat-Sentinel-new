import React, { memo } from 'react';
import { TouchableOpacity, Text, StyleSheet, ViewStyle } from 'react-native';
import { Colors } from '../../theme/colors';

interface ActionButtonProps {
    label: string;
    onPress: () => void;
    variant?: 'primary' | 'secondary' | 'danger';
    style?: ViewStyle | ViewStyle[];
}

const ActionButtonComponent = ({ label, onPress, variant = 'secondary', style }: ActionButtonProps) => {
    const getVariantStyle = () => {
        switch (variant) {
            case 'primary': return { backgroundColor: Colors.primary, borderColor: Colors.primary };
            case 'danger': return { backgroundColor: Colors.red, borderColor: Colors.red };
            default: return { backgroundColor: Colors.glassBackground, borderColor: Colors.glassBorder };
        }
    };

    return (
        <TouchableOpacity 
            activeOpacity={0.7} 
            onPress={onPress} 
            style={[styles.button, getVariantStyle(), style]}
        >
            <Text style={[styles.label, { color: variant === 'secondary' ? Colors.textPrimary : '#000' }]}>
                {label}
            </Text>
        </TouchableOpacity>
    );
};

const styles = StyleSheet.create({
    button: {
        paddingVertical: 12,
        paddingHorizontal: 24,
        borderRadius: 8,
        borderWidth: 1,
        alignItems: 'center',
        justifyContent: 'center',
        flexDirection: 'row',
    },
    label: {
        fontSize: 14,
        fontWeight: '600',
    },
});

export const ActionButton = memo(ActionButtonComponent);
