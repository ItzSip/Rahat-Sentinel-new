import React, { memo } from 'react';
import { View, StyleSheet, ViewProps, ViewStyle } from 'react-native';
import { Colors } from '../../theme/colors';

interface GlassCardProps extends ViewProps {
    children: React.ReactNode;
    style?: ViewStyle | ViewStyle[];
}

const GlassCardComponent = ({ children, style, ...props }: GlassCardProps) => {
    return (
        <View style={[styles.card, style]} {...props}>
            {children}
        </View>
    );
};

const styles = StyleSheet.create({
    card: {
        backgroundColor: Colors.glassBackground,
        borderColor: Colors.glassBorder,
        borderWidth: 1,
        borderRadius: 16,
        padding: 16,
        marginVertical: 8,
        // Soft shadows without heavy blur
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.3,
        shadowRadius: 5,
        elevation: 5,
    },
});

export const GlassCard = memo(GlassCardComponent);
