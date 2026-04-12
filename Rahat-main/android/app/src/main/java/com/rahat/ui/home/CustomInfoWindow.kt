package com.rahat.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahat.ui.theme.*

@Composable
fun CustomInfoWindow(
    alert: com.rahat.data.model.Alert,
    userLocation: org.osmdroid.util.GeoPoint?,
    onDismiss: () -> Unit
) {
    val glass = MaterialTheme.glass

    // ── Animation ──────────────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        )
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300)
    )

    // ── Distance ───────────────────────────────────────────────────────────
    val distance = if (userLocation != null) {
        val res = FloatArray(1)
        android.location.Location.distanceBetween(
            userLocation.latitude, userLocation.longitude,
            alert.lat, alert.lon, res
        )
        if (res[0] > 1000) String.format("%.1f km", res[0] / 1000)
        else "${res[0].toInt()} m"
    } else "--"

    // ── Risk colors from theme ─────────────────────────────────────────────
    val riskLevel = when (alert.severity.name.uppercase()) {
        "CRITICAL" -> RiskLevel.CRITICAL
        "HIGH"     -> RiskLevel.WARNING
        "MEDIUM"   -> RiskLevel.WATCH
        else       -> RiskLevel.SAFE
    }
    val severityColor = riskSolidColor(riskLevel)
    val glassColor    = riskGlassColor(riskLevel)
    val borderColor   = riskBorderColor(riskLevel)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        // ── Glass Card ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 88.dp)
                .graphicsLayer {
                    scaleX     = scale
                    scaleY     = scale
                    this.alpha = alpha
                }
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            glassColor,
                            glass.cardBackground
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(borderColor, glass.cardBorder)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Severity badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(severityColor.copy(alpha = 0.2f))
                            .border(
                                width  = 1.dp,
                                color  = severityColor.copy(alpha = 0.6f),
                                shape  = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text       = alert.severity.name,
                            color      = if (glass.isDark) Color.White else severityColor,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextOnGlassSecondary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Distance row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint     = severityColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text     = "$distance away",
                        fontSize = 16.sp,
                        color    = TextOnGlass,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(glass.divider)
                )

                Spacer(Modifier.height(12.dp))

                // Alert message
                Text(
                    text     = alert.message,
                    fontSize = 14.sp,
                    color    = TextOnGlassSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}