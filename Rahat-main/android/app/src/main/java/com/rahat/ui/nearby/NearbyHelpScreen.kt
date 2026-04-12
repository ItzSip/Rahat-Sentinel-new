package com.rahat.ui.nearby

import android.Manifest
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahat.data.model.PeerState
import com.rahat.data.model.SignalLevel
import com.rahat.data.model.SignalTrend
import com.rahat.ui.theme.*

@Composable
fun NearbyHelpScreen(
    viewModel: NearbyViewModel,
    userLat: Double,
    userLng: Double,
    activeRiskLevel: RiskLevel = RiskLevel.SAFE,
    activeZone: String? = null,
    onBack: () -> Unit
) {
    val devices by viewModel.nearbyDevices.collectAsState()
    val glass = MaterialTheme.glass

    val solidColor  = riskSolidColor(activeRiskLevel)
    val glassColor  = riskGlassColor(activeRiskLevel)
    val borderColor = riskBorderColor(activeRiskLevel)

    val screenTitle = when (activeRiskLevel) {
        RiskLevel.CRITICAL -> "Alert — Notify Nearby"
        RiskLevel.WARNING  -> "Spread Warning Nearby"
        RiskLevel.WATCH    -> "Nearby Devices"
        else               -> "Nearby Help (P2P)"
    }

    val alertReachedCount = devices.count { it.hasReceivedAlert }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulseAlpha"
    )

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION)
    } else {
        listOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION)
    }
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { }
    LaunchedEffect(Unit) { launcher.launch(permissions.toTypedArray()) }

    Box(modifier = Modifier.fillMaxSize().background(glass.backgroundGradient)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top bar
            Box(
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp)).background(glass.cardBackground)
                    .border(1.dp, borderColor.copy(alpha = if (activeRiskLevel == RiskLevel.CRITICAL) pulseAlpha else 0.4f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextOnGlass)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(screenTitle, color = TextOnGlass, fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Bluetooth, null, tint = RahatCyan, modifier = Modifier.size(22.dp))
                }
            }

            // Alert spread banner
            if (activeRiskLevel != RiskLevel.SAFE && activeRiskLevel != RiskLevel.OFFLINE) {
                val zoneText = if (!activeZone.isNullOrBlank()) " · $activeZone" else ""
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(glassColor, glass.cardBackground)))
                        .border(1.5.dp, borderColor.copy(alpha = if (activeRiskLevel == RiskLevel.CRITICAL) pulseAlpha else 0.5f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CellTower, null, tint = solidColor, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Alert spreading locally$zoneText", color = TextOnGlass,
                                fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("$alertReachedCount device${if (alertReachedCount != 1) "s" else ""} reached · ${riskLabel(activeRiskLevel)}",
                                color = TextOnGlassSecondary, fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                .background(solidColor.copy(alpha = 0.2f))
                                .border(1.dp, solidColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) { Text(riskLabel(activeRiskLevel), color = solidColor, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }

            // Device list
            if (devices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        CircularProgressIndicator(color = RahatCyan, modifier = Modifier.size(48.dp), strokeWidth = 3.dp)
                        Spacer(Modifier.height(20.dp))
                        Text("Scanning for nearby devices...", color = TextOnGlass, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (activeRiskLevel != RiskLevel.SAFE) "Alert will spread as devices are found"
                            else "Devices will appear when in range",
                            color = TextOnGlassSecondary, fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(devices, key = { it.rId }) { device ->
                        NearbyDeviceCard(device, activeRiskLevel, glass)
                    }
                }
            }
        }
    }
}

@Composable
private fun NearbyDeviceCard(device: PeerState, activeRiskLevel: RiskLevel, glass: GlassTheme) {
    val (cardGlass, cardBorder, cardSolid) = when (device.severity.uppercase()) {
        "CRITICAL" -> Triple(SOSRedGlass,           SOSRed.copy(alpha = 0.6f),           SOSRed)
        "HIGH"     -> Triple(RiskWarningGlass,       RiskWarningBorder,                   RiskWarningOrange)
        else       -> Triple(glass.cardBackground,   glass.cardBorder,                    RahatCyan)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cardPulse_${device.rId}")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "cardPulse"
    )
    val effectiveBorder = cardBorder.copy(alpha = if (device.severity.uppercase() == "CRITICAL") pulseAlpha else cardBorder.alpha)

    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(cardGlass, glass.cardBackground)))
            .border(1.5.dp, effectiveBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(cardSolid.copy(alpha = 0.15f))
                        .border(1.dp, cardSolid.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (device.isInSosMode) Icons.Default.Warning else Icons.Default.PhoneAndroid,
                        null, tint = cardSolid, modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(device.name, color = TextOnGlass, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(peerSourceLabel(device), color = TextOnGlassMuted, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        .background(cardSolid.copy(alpha = 0.18f))
                        .border(1.dp, cardSolid.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) { Text(device.severity, color = cardSolid, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                DeviceMetaStat("Signal", signalLevelLabel(device.signalLevel), signalLevelColor(device.signalLevel))
                DeviceMetaStat("Trend", signalTrendLabel(device.signalTrend), TextOnGlassSecondary)
                if (device.latitude != null && device.longitude != null) {
                    DeviceMetaStat("GPS", "%.4f, %.4f".format(device.latitude, device.longitude), TextOnGlassMuted)
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (device.isInSosMode) StatusTag("SOS Active", SOSRed)
                if (device.hasReceivedAlert && activeRiskLevel != RiskLevel.SAFE)
                    StatusTag("Alert Received", RiskWarningOrange)
                if (activeRiskLevel != RiskLevel.SAFE && activeRiskLevel != RiskLevel.OFFLINE)
                    StatusTag("In Risk Zone", riskSolidColor(activeRiskLevel))
            }
        }
    }
}

@Composable
private fun DeviceMetaStat(label: String, value: String, color: Color) {
    Column {
        Text(label, color = TextOnGlassMuted, fontSize = 10.sp)
        Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusTag(text: String, color: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) { Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
}

private fun peerSourceLabel(device: PeerState): String {
    val src  = if (device.source.name == "MESH") "via mesh" else "direct"
    val time = ((System.currentTimeMillis() - device.lastSeen) / 1000).toInt()
    return "$src · ${time}s ago"
}

private fun signalLevelLabel(level: SignalLevel) = when (level) {
    SignalLevel.VERY_STRONG -> "Very Strong"; SignalLevel.STRONG -> "Strong"
    SignalLevel.MODERATE    -> "Moderate";    SignalLevel.WEAK   -> "Weak"
}
private fun signalLevelColor(level: SignalLevel) = when (level) {
    SignalLevel.VERY_STRONG, SignalLevel.STRONG -> RiskSafeGreen
    SignalLevel.MODERATE                        -> RiskWatchYellow
    SignalLevel.WEAK                            -> TextOnGlassMuted
}
private fun signalTrendLabel(trend: SignalTrend) = when (trend) {
    SignalTrend.APPROACHING -> "↑ Approaching"; SignalTrend.RECEDING -> "↓ Receding"; SignalTrend.STABLE -> "→ Stable"
}