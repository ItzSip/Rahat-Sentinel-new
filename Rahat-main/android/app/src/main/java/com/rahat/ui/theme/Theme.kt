package com.rahat.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary           = RahatBlue,
    onPrimary         = Color.White,
    primaryContainer  = RahatBlueLight,
    onPrimaryContainer = Color.White,
    secondary         = RahatCyan,
    onSecondary       = Color.White,
    secondaryContainer = RahatCyanLight,
    onSecondaryContainer = TextPrimary,
    background        = SurfaceLight,
    onBackground      = TextPrimary,
    surface           = CardLight,
    onSurface         = TextPrimary,
    surfaceVariant    = GradientStartLight,
    onSurfaceVariant  = TextSecondary,
    outline           = GlassBorderSubtle,
    error             = RiskCriticalRed,
    onError           = Color.White
)

private val DarkColors = darkColorScheme(
    primary           = RahatBlueLight,
    onPrimary         = Color.Black,
    primaryContainer  = RahatBlueDark,
    onPrimaryContainer = Color.White,
    secondary         = RahatCyanLight,
    onSecondary       = Color.Black,
    secondaryContainer = RahatCyan,
    onSecondaryContainer = Color.Black,
    background        = SurfaceDark,
    onBackground      = TextPrimaryDark,
    surface           = CardDark,
    onSurface         = TextPrimaryDark,
    surfaceVariant    = Color(0xFF1A2C45),
    onSurfaceVariant  = TextSecondaryDark,
    outline           = GlassDarkBorder,
    error             = RiskCriticalRed,
    onError           = Color.White
)

data class GlassTheme(
    val cardBackground: Color,
    val cardBorder: Color,
    val strongBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val divider: Color,
    val backgroundGradient: Brush,
    val isDark: Boolean
)

val LocalGlassTheme = staticCompositionLocalOf<GlassTheme> {
    error("No GlassTheme provided")
}

// ── FIXED: Light theme uses DARK text so it's visible on white glass cards ──
private val LightGlassTheme = GlassTheme(
    cardBackground    = Color(0xEEFFFFFF),
    cardBorder        = Color(0x44000000),
    strongBackground  = Color(0xFAFFFFFF),
    textPrimary       = Color(0xFF0D1B2A),        // dark navy — visible on white
    textSecondary     = Color(0xFF4A6572),        // dark slate — visible on white
    textMuted         = Color(0xFF4A6572).copy(alpha = 0.6f),
    divider           = Color(0x1A000000),
    backgroundGradient = Brush.linearGradient(
        colors = listOf(GradientStart, GradientMid, GradientEnd)
    ),
    isDark = false
)

// ── Dark theme unchanged — white text on dark gradient ───────────────────────
private val DarkGlassTheme = GlassTheme(
    cardBackground    = GlassDark,
    cardBorder        = GlassDarkBorder,
    strongBackground  = GlassDarkStrong,
    textPrimary       = TextPrimaryDark,
    textSecondary     = TextSecondaryDark,
    textMuted         = TextSecondaryDark.copy(alpha = 0.6f),
    divider           = DividerDark,
    backgroundGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0A1628),
            Color(0xFF0D2A4A),
            Color(0xFF0A3D55)
        )
    ),
    isDark = true
)

enum class RiskLevel { SAFE, WATCH, WARNING, CRITICAL, OFFLINE }

fun riskGlassColor(level: RiskLevel): Color = when (level) {
    RiskLevel.SAFE     -> RiskSafeGlass
    RiskLevel.WATCH    -> RiskWatchGlass
    RiskLevel.WARNING  -> RiskWarningGlass
    RiskLevel.CRITICAL -> RiskCriticalGlass
    RiskLevel.OFFLINE  -> OfflineGlassGray
}

fun riskBorderColor(level: RiskLevel): Color = when (level) {
    RiskLevel.SAFE     -> RiskSafeBorder
    RiskLevel.WATCH    -> RiskWatchBorder
    RiskLevel.WARNING  -> RiskWarningBorder
    RiskLevel.CRITICAL -> RiskCriticalBorder
    RiskLevel.OFFLINE  -> OfflineGray
}

fun riskSolidColor(level: RiskLevel): Color = when (level) {
    RiskLevel.SAFE     -> RiskSafeGreen
    RiskLevel.WATCH    -> RiskWatchYellow
    RiskLevel.WARNING  -> RiskWarningOrange
    RiskLevel.CRITICAL -> RiskCriticalRed
    RiskLevel.OFFLINE  -> OfflineGray
}

fun riskLabel(level: RiskLevel): String = when (level) {
    RiskLevel.SAFE     -> "No major risk detected"
    RiskLevel.WATCH    -> "Watch — Monitor the situation"
    RiskLevel.WARNING  -> "Warning — Prepare now"
    RiskLevel.CRITICAL -> "Critical — Act immediately"
    RiskLevel.OFFLINE  -> "Offline — Last synced data"
}

@Composable
fun RahatTheme(
    isDarkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkMode) DarkColors else LightColors
    val glassTheme  = if (isDarkMode) DarkGlassTheme else LightGlassTheme

    CompositionLocalProvider(LocalGlassTheme provides glassTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = RahatTypography,
            content     = content
        )
    }
}

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val glass = LocalGlassTheme.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = glass.backgroundGradient)
    ) {
        content()
    }
}

val MaterialTheme.glass: GlassTheme
    @Composable get() = LocalGlassTheme.current