package com.rahat.ui.alert

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahat.data.model.*
import com.rahat.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertFeedScreen(
    peers: List<PeerState>,
    onBackClick: () -> Unit,
    // NEW: tap card → open detail screen
    onAlertDetailClick: (zone: String, level: RiskLevel) -> Unit = { _, _ -> }
) {
    val glass = MaterialTheme.glass

    // ── Build feed from peers (BLE) + demo AI alerts ───────────────────────
    // In production, AI alerts come from MapViewModel / Firestore.
    // Here we surface HIGH-severity BLE peers AND synthesize alert cards.
    val highPeers = remember(peers) {
        peers.filter { it.severity == "HIGH" }
            .sortedBy { it.signalLevel.ordinal }
    }

    // Demo AI predictions for feed (replace with real StateFlow in production)
    val demoAlerts = remember {
        listOf(
            FeedItem.PredictionCard(
                id               = "ai_001",
                eventType        = "Flash Flood",
                district         = "Chamoli",
                riskLevel        = RiskLevel.CRITICAL,
                timeToEventHours = 36,
                confidence       = 0.86f,
                status           = AlertStatus.CRITICAL
            ),
            FeedItem.PredictionCard(
                id               = "ai_002",
                eventType        = "Landslide",
                district         = "Uttarakhand",
                riskLevel        = RiskLevel.WARNING,
                timeToEventHours = 48,
                confidence       = 0.82f,
                status           = AlertStatus.WARNING
            ),
            FeedItem.PredictionCard(
                id               = "ai_003",
                eventType        = "Heatwave",
                district         = "Indore",
                riskLevel        = RiskLevel.WATCH,
                timeToEventHours = 72,
                confidence       = 0.74f,
                status           = AlertStatus.WATCH
            )
        )
    }

    // Sort per spec: closest threat (critical first) → highest severity → most recent
    val sortedAlerts = remember(demoAlerts) {
        demoAlerts.sortedWith(
            compareByDescending<FeedItem.PredictionCard> { it.riskLevel.ordinal }
                .thenBy { it.timeToEventHours }
        )
    }

    remember(peers.size) {
        Log.i("ALERT_FEED", "RAHAT_UI_REPORT: Feed updated — ${peers.size} BLE peers, " +
                "${demoAlerts.size} predictions")
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Gradient background ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(glass.backgroundGradient)
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Glass Top Bar ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                    .background(glass.cardBackground)
                    .border(
                        1.dp,
                        glass.cardBorder,
                        RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextOnGlass
                        )
                    }
                    Text(
                        "Alert Feed",
                        color      = TextOnGlass,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                    Icon(
                        Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint     = RahatCyanLight,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }

            // ── Feed ───────────────────────────────────────────────────────
            if (sortedAlerts.isEmpty() && highPeers.isEmpty()) {
                // Empty safe state
                Box(
                    modifier          = Modifier.fillMaxSize(),
                    contentAlignment  = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(RiskSafeGlass)
                                .border(1.dp, RiskSafeBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint     = RiskSafeGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No major risk detected",
                            color      = TextOnGlass,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 18.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Your area is currently safe.\nScanning for updates…",
                            color     = TextOnGlassSecondary,
                            fontSize  = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(
                        start  = 16.dp, end = 16.dp,
                        top    = 16.dp, bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // ── Section: AI Predictions ─────────────────────────
                    if (sortedAlerts.isNotEmpty()) {
                        item {
                            FeedSectionHeader(title = "Active Predictions")
                        }
                        items(sortedAlerts, key = { it.id }) { item ->
                            PredictionAlertCard(
                                item    = item,
                                onClick = {
                                    onAlertDetailClick(item.district, item.riskLevel)
                                }
                            )
                        }
                    }

                    // ── Section: BLE Nearby Devices ─────────────────────
                    if (highPeers.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(4.dp))
                            FeedSectionHeader(title = "Nearby Devices in Emergency")
                        }
                        items(highPeers, key = { it.rId }) { peer ->
                            BleDeviceAlertCard(peer = peer)
                        }
                    }
                }
            }
        }
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
private fun FeedSectionHeader(title: String) {
    Text(
        text  = title.uppercase(),
        color = TextOnGlassMuted,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

// ─── Feed Item Model ──────────────────────────────────────────────────────────

sealed class FeedItem {
    data class PredictionCard(
        val id: String,
        val eventType: String,
        val district: String,
        val riskLevel: RiskLevel,
        val timeToEventHours: Int?,
        val confidence: Float,
        val status: AlertStatus
    ) : FeedItem()
}

// ─── AI Prediction Card ───────────────────────────────────────────────────────

@Composable
fun PredictionAlertCard(
    item: FeedItem.PredictionCard,
    onClick: () -> Unit
) {
    val glass       = MaterialTheme.glass
    val solidColor  = riskSolidColor(item.riskLevel)
    val glassColor  = riskGlassColor(item.riskLevel)
    val borderColor = riskBorderColor(item.riskLevel)

    // Pulse for critical
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.6f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900),
            repeatMode = RepeatMode.Reverse
        )
    )

    var pinned by remember { mutableStateOf(false) }
    var muted  by remember { mutableStateOf(false) }

    if (muted) return  // hide muted cards

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(glassColor, glass.cardBackground)
                )
            )
            .border(
                width = 1.5.dp,
                color = borderColor.copy(
                    alpha = if (item.riskLevel == RiskLevel.CRITICAL) pulseAlpha else 0.5f
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {

            // ── Row 1: Event type + status badge ──────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Event icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(solidColor.copy(alpha = 0.15f))
                            .border(1.dp, solidColor.copy(0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = eventIcon(item.eventType),
                            contentDescription = null,
                            tint     = solidColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            item.eventType,
                            color      = TextOnGlass,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp
                        )
                        Text(
                            item.district,
                            color    = TextOnGlassSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                // Status badge
                StatusBadge(status = item.status, solidColor = solidColor)
            }

            Spacer(Modifier.height(12.dp))

            // ── Divider ───────────────────────────────────────────────────
            Box(
                Modifier.fillMaxWidth().height(1.dp)
                    .background(glass.divider)
            )

            Spacer(Modifier.height(10.dp))

            // ── Row 2: Meta stats ─────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetaStat(
                    label = "Time",
                    value = if (item.timeToEventHours != null)
                        "${item.timeToEventHours}h remaining"
                    else "Unknown"
                )
                MetaStat(
                    label = "Confidence",
                    value = "${(item.confidence * 100).toInt()}%"
                )
                MetaStat(
                    label = "Level",
                    value = item.riskLevel.name
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Row 3: Action buttons ─────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Open Details
                CardActionChip(
                    label  = "Details",
                    icon   = Icons.Default.OpenInNew,
                    color  = RahatBlueLight,
                    modifier = Modifier.weight(1f),
                    onClick = onClick
                )
                // Share via BLE
                CardActionChip(
                    label  = "Share",
                    icon   = Icons.Default.Bluetooth,
                    color  = RahatCyanLight,
                    modifier = Modifier.weight(1f),
                    onClick = { /* TODO: BLE share */ }
                )
                // Pin
                CardActionChip(
                    label   = if (pinned) "Pinned" else "Pin",
                    icon    = if (pinned) Icons.Default.PushPin else Icons.Default.PushPin,
                    color   = if (pinned) RiskSafeGreen else TextOnGlassMuted,
                    modifier = Modifier.weight(1f),
                    onClick = { pinned = !pinned }
                )
                // Mute
                CardActionChip(
                    label   = "Mute",
                    icon    = Icons.Default.NotificationsOff,
                    color   = TextOnGlassMuted,
                    modifier = Modifier.weight(1f),
                    onClick = { muted = true }
                )
            }
        }
    }
}

// ─── BLE Peer Card (existing, glassmorphism applied) ─────────────────────────

@Composable
fun BleDeviceAlertCard(peer: PeerState) {
    val glass = MaterialTheme.glass

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(RiskWarningGlass, glass.cardBackground)
                )
            )
            .border(1.dp, RiskWarningBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    peer.name,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextOnGlass
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Signal: ${peer.signalLevel}",
                    fontSize = 13.sp,
                    color    = TextOnGlassSecondary
                )
                Text(
                    "Trend: ${peer.signalTrend}",
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = when (peer.signalTrend) {
                        com.rahat.data.model.SignalTrend.APPROACHING -> RiskSafeGreen
                        com.rahat.data.model.SignalTrend.RECEDING    -> RiskCriticalRed
                        else -> TextOnGlassMuted
                    }
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(
                    status     = AlertStatus.WARNING,
                    solidColor = RiskWarningOrange
                )
                Spacer(Modifier.height(8.dp))
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint     = RiskCriticalRed,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ─── Small reusable components ────────────────────────────────────────────────

@Composable
private fun StatusBadge(status: AlertStatus, solidColor: Color) {
    val label = when (status) {
        AlertStatus.WATCH    -> "WATCH"
        AlertStatus.WARNING  -> "WARNING"
        AlertStatus.CRITICAL -> "CRITICAL"
        AlertStatus.RESOLVED -> "RESOLVED"
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(solidColor.copy(alpha = 0.15f))
            .border(1.dp, solidColor.copy(0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            color      = solidColor,
            fontSize   = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun MetaStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label.uppercase(),
            color = TextOnGlassMuted,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            color      = TextOnGlass,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 13.sp
        )
    }
}

@Composable
private fun CardActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(0.35f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint     = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                color     = color,
                fontSize  = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Event type icon helper ───────────────────────────────────────────────────

private fun eventIcon(type: String): androidx.compose.ui.graphics.vector.ImageVector =
    when (type.uppercase()) {
        "FLASH FLOOD", "FLOOD" -> Icons.Default.Water
        "LANDSLIDE"            -> Icons.Default.Terrain
        "HEATWAVE"             -> Icons.Default.WbSunny
        "CYCLONE"              -> Icons.Default.Air
        "EARTHQUAKE"           -> Icons.Default.Vibration
        else                   -> Icons.Default.Warning
    }