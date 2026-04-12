package com.rahat.ui.safezone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.rahat.data.model.SafeZone
import com.rahat.data.model.SafeZoneType
import com.rahat.ui.theme.*

@Composable
fun SafeZoneScreen(
    riskLevel: RiskLevel,
    userLat: Double,
    userLng: Double,
    onBack: () -> Unit
) {
    val glass      = MaterialTheme.glass
    val solidColor = riskSolidColor(riskLevel)
    val label      = riskLabel(riskLevel)

    val mockZones = listOf(
        SafeZone("1", "Higher Ground — North Ridge",   SafeZoneType.HIGH_GROUND,    userLat + 0.02, userLng + 0.01, 2200, 500, true),
        SafeZone("2", "Govt Relief Shelter — Block A", SafeZoneType.RELIEF_CAMP,    userLat - 0.01, userLng + 0.03, 3800, 300, true),
        SafeZone("3", "Elevated Zone — South Hill",    SafeZoneType.ELEVATION_SAFE, userLat + 0.04, userLng - 0.02, 5100, 0,   false)
    )

    val guidance: List<Pair<ImageVector, String>> = when (riskLevel) {
        RiskLevel.CRITICAL, RiskLevel.WARNING -> listOf(
            Pair(Icons.Default.ArrowUpward,  "Move to higher ground immediately"),
            Pair(Icons.Default.DoNotDisturb, "Avoid riverbed and low-lying routes"),
            Pair(Icons.Default.PhoneAndroid, "Keep phone charged and on"),
            Pair(Icons.Default.Group,        "Stay near other people and devices")
        )
        RiskLevel.WATCH -> listOf(
            Pair(Icons.Default.Inventory,    "Pack essentials — documents, water, medicine"),
            Pair(Icons.Default.Notifications,"Monitor alerts every 30 minutes"),
            Pair(Icons.Default.PhoneAndroid, "Keep phone charged"),
            Pair(Icons.Default.Group,        "Inform family of your location")
        )
        else -> listOf(
            Pair(Icons.Default.PhoneAndroid, "Keep phone charged"),
            Pair(Icons.Default.Bluetooth,    "Stay near other BLE devices"),
            Pair(Icons.Default.Notifications,"Stay tuned to local alerts"),
            Pair(Icons.Default.Group,        "Check on neighbours")
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(glass.backgroundGradient)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // Top bar
            Box(
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp)).background(glass.cardBackground)
                    .border(1.dp, glass.cardBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextOnGlass)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Safe Zone Guide", color = TextOnGlass, fontWeight = FontWeight.Bold,
                        fontSize = 17.sp, modifier = Modifier.weight(1f))
                    if (riskLevel != RiskLevel.SAFE) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                .background(solidColor.copy(alpha = 0.2f))
                                .border(1.dp, solidColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) { Text(label, color = solidColor, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }

            SectionHeader("Nearest Safe Zones")
            mockZones.forEach { zone -> SafeZoneCard(zone, glass); Spacer(Modifier.height(10.dp)) }

            Spacer(Modifier.height(8.dp))

            SectionHeader("What to do now")
            guidance.forEach { (icon, text) -> GuidanceCard(icon, text, glass); Spacer(Modifier.height(8.dp)) }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SafeZoneCard(zone: SafeZone, glass: GlassTheme) {
    val typeColor = when (zone.type) {
        SafeZoneType.HIGH_GROUND    -> RahatCyan
        SafeZoneType.RELIEF_CAMP    -> RiskSafeGreen
        SafeZoneType.ELEVATION_SAFE -> RiskWatchYellow
        SafeZoneType.SHELTER        -> RiskWarningOrange
    }
    val typeLabel = when (zone.type) {
        SafeZoneType.HIGH_GROUND    -> "High Ground"
        SafeZoneType.RELIEF_CAMP    -> "Relief Camp"
        SafeZoneType.ELEVATION_SAFE -> "Elevation Safe"
        SafeZoneType.SHELTER        -> "Shelter"
    }
    val distKm = "%.1f km".format(zone.distanceMeters / 1000f)

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(typeColor.copy(alpha = 0.1f), glass.cardBackground)))
            .border(1.dp, typeColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp)).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(typeColor.copy(alpha = 0.15f))
                    .border(1.dp, typeColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (zone.type) {
                        SafeZoneType.HIGH_GROUND    -> Icons.Default.Terrain
                        SafeZoneType.RELIEF_CAMP    -> Icons.Default.Home
                        SafeZoneType.ELEVATION_SAFE -> Icons.Default.ArrowUpward
                        SafeZoneType.SHELTER        -> Icons.Default.Shield
                    }, null, tint = typeColor, modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(zone.name, color = TextOnGlass, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SmallTag(typeLabel, typeColor)
                    SmallTag(distKm, TextOnGlassMuted)
                    if (zone.isVerified) SmallTag("✓ Verified", RiskSafeGreen)
                    if (zone.capacity > 0) SmallTag("${zone.capacity} cap", TextOnGlassMuted)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = TextOnGlassMuted, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun GuidanceCard(icon: ImageVector, text: String, glass: GlassTheme) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp)).background(glass.cardBackground)
            .border(1.dp, glass.cardBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = RahatCyan, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(text, color = TextOnGlass, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.clip(RoundedCornerShape(5.dp))
                    .background(RahatCyan.copy(alpha = 0.1f)).padding(horizontal = 6.dp, vertical = 2.dp)
            ) { Text("Offline", color = RahatCyan, fontSize = 9.sp, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, color = TextOnGlass, fontWeight = FontWeight.Bold, fontSize = 15.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
}

@Composable
private fun SmallTag(text: String, color: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f)).padding(horizontal = 6.dp, vertical = 2.dp)
    ) { Text(text, color = color, fontSize = 10.sp) }
}