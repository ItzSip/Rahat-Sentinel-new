package com.rahat.ui.warning

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahat.ui.theme.*

@Composable
fun EarlyWarningDetailScreen(
    zone: String,
    riskLevel: RiskLevel,
    onBack: () -> Unit,
    onViewOnMap: () -> Unit,
    onSafeZone: () -> Unit
) {
    val glass       = MaterialTheme.glass
    val solidColor  = riskSolidColor(riskLevel)
    val glassColor  = riskGlassColor(riskLevel)
    val borderColor = riskBorderColor(riskLevel)
    val label       = riskLabel(riskLevel)

    val infiniteTransition = rememberInfiniteTransition(label = "detailPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "pulse"
    )
    val effectiveBorder = borderColor.copy(alpha = if (riskLevel == RiskLevel.CRITICAL) pulseAlpha else 0.5f)

    val confidence    = 86
    val timeToEvent   = 18
    val rainfallPct   = 91
    val terrainPct    = 78
    val historicalPct = 83
    val eventLabel    = "Flood risk"

    Box(modifier = Modifier.fillMaxSize().background(glass.backgroundGradient)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // Top bar
            Box(
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp)).background(glass.cardBackground)
                    .border(1.dp, effectiveBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextOnGlass)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Early Warning Detail", color = TextOnGlass, fontWeight = FontWeight.Bold,
                        fontSize = 17.sp, modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            .background(solidColor.copy(alpha = 0.2f))
                            .border(1.dp, solidColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) { Text(label, color = solidColor, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
            }

            // Hero card
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(glassColor, glass.cardBackground)))
                    .border(1.5.dp, effectiveBorder, RoundedCornerShape(20.dp)).padding(20.dp)
            ) {
                Column {
                    Text("$eventLabel detected", color = TextOnGlass, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetaPill(zone, solidColor)
                        MetaPill(label, solidColor)
                        MetaPill("~${timeToEvent}h away", TextOnGlassSecondary)
                    }
                }
            }

            // Explanation card
            SectionCard("Why this alert?", glass, borderColor.copy(alpha = 0.3f)) {
                ExplainRow(Icons.Default.WaterDrop, RahatCyan, "Rising rainfall anomaly", "$rainfallPct%")
                Spacer(Modifier.height(10.dp))
                ExplainRow(Icons.Default.Terrain, RiskWatchYellow, "Terrain instability", "$terrainPct%")
                Spacer(Modifier.height(10.dp))
                ExplainRow(Icons.Default.History, RiskWarningOrange, "Historical pattern match", "$historicalPct%")
                Spacer(Modifier.height(10.dp))
                ExplainRow(Icons.Default.Psychology, solidColor, "Model confidence", "$confidence%")
            }

            // Timeline card
            SectionCard("Risk timeline", glass, borderColor.copy(alpha = 0.3f)) {
                TimelineBar(riskLevel, solidColor, borderColor)
            }

            Spacer(Modifier.height(16.dp))

            // Action buttons
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionButton("View on Map", Icons.Default.Map,
                    Brush.linearGradient(listOf(RahatBlue, RahatCyan.copy(alpha = 0.8f))),
                    RahatCyan.copy(alpha = 0.5f), onViewOnMap)
                ActionButton("Find Safe Zone", Icons.Default.Shield,
                    Brush.linearGradient(listOf(RiskSafeGreen.copy(alpha = 0.4f), glass.cardBackground)),
                    RiskSafeGreen.copy(alpha = 0.5f), onSafeZone)
                ActionButton("Share via BLE", Icons.Default.Bluetooth,
                    Brush.linearGradient(listOf(RahatCyan.copy(alpha = 0.2f), glass.cardBackground)),
                    RahatCyan.copy(alpha = 0.3f)) { }
                ActionButton("Save Offline", Icons.Default.Download,
                    Brush.linearGradient(listOf(glass.cardBackground, glass.cardBackground)),
                    glass.cardBorder) { }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TimelineBar(riskLevel: RiskLevel, solidColor: Color, borderColor: Color) {
    val steps = listOf("Now", "+24h", "+48h", "+72h")
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        steps.forEachIndexed { index, _ ->
            val isActive = index == 0
            Box(
                modifier = Modifier
                    .size(if (isActive) 14.dp else 10.dp)
                    .clip(CircleShape)
                    .background(if (isActive) solidColor else solidColor.copy(alpha = 0.3f))
                    .border(1.dp, borderColor.copy(alpha = if (isActive) 0.8f else 0.3f), CircleShape)
            )
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier.weight(1f).height(2.dp)
                        .background(Brush.linearGradient(listOf(solidColor.copy(alpha = 0.6f), solidColor.copy(alpha = 0.1f))))
                )
            }
        }
    }
    Spacer(Modifier.height(6.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        steps.forEach { step ->
            Text(step,
                color = if (step == "Now") TextOnGlass else TextOnGlassMuted,
                fontSize = 11.sp,
                fontWeight = if (step == "Now") FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SectionCard(title: String, glass: GlassTheme, border: Color, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp)).background(glass.cardBackground)
            .border(1.dp, border, RoundedCornerShape(18.dp)).padding(16.dp)
    ) {
        Text(title, color = TextOnGlassSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun ExplainRow(icon: ImageVector, color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = TextOnGlass, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MetaPill(text: String, color: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)
    ) { Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
}

@Composable
private fun ActionButton(label: String, icon: ImageVector, gradient: Brush, border: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(gradient).border(1.dp, border, RoundedCornerShape(14.dp))
    ) {
        TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
            Icon(icon, null, tint = TextOnGlass, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, color = TextOnGlass, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}