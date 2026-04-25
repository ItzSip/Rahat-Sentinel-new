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
        elevation: 0,
    },
});

export const GlassCard = memo(GlassCardComponent);
