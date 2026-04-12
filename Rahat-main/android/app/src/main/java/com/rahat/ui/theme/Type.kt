package com.rahat.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Font Family ────────────────────────────────────────────────────────────
// Using system default (inter-compatible). Replace with custom font if needed.
val RahatFontFamily = FontFamily.Default

// ─── Typography Scale ────────────────────────────────────────────────────────

val RahatTypography = Typography(

    // Large screen/hero titles
    displayLarge = TextStyle(
        fontFamily = RahatFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp
    ),

    displayMedium = TextStyle(
        fontFamily = RahatFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.25).sp
    ),

    // Section / screen titles
    headlineLarge = TextStyle(
        fontFamily = RahatFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),

    headlineMedium = TextStyle(
        fontFamily = RahatFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),

    headlineSmall = TextStyle(
        fontFamily = RahatFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),

    // Card titles, risk labels
    titleLarge = TextStyle(
        fontFamily = RahatFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),

    titleMedium = TextStyle(
        fontFamily = RahatFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),

    titleSmall = TextStyle(
        fontFamily = RahatFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body text
    bodyLarge = TextStyle(
        fontFamily = RahatFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = RahatFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.1.sp
    ),

    bodySmall = TextStyle(
        fontFamily = RahatFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    ),

    // Labels, buttons, tags
    labelLarge = TextStyle(
        fontFamily = RahatFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),

    labelMedium = TextStyle(
        fontFamily = RahatFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    labelSmall = TextStyle(
        fontFamily = RahatFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)

// ─── Semantic Text Style Aliases ─────────────────────────────────────────────
// Use these for survival-copy UI text for consistency

val RiskTitleStyle = TextStyle(
    fontFamily = RahatFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize   = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

val RiskMetaStyle = TextStyle(
    fontFamily = RahatFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize   = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.3.sp
)

val SOSButtonStyle = TextStyle(
    fontFamily = RahatFontFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize   = 20.sp,
    lineHeight = 24.sp,
    letterSpacing = 1.sp
)

val BannerTitleStyle = TextStyle(
    fontFamily = RahatFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize   = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp
)

val BannerSubtitleStyle = TextStyle(
    fontFamily = RahatFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize   = 12.sp,
    lineHeight = 17.sp,
    letterSpacing = 0.1.sp
)

val CardLabelStyle = TextStyle(
    fontFamily = RahatFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize   = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.6.sp
)

val OfflineBadgeStyle = TextStyle(
    fontFamily = RahatFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize   = 10.sp,
    lineHeight = 13.sp,
    letterSpacing = 0.4.sp
)