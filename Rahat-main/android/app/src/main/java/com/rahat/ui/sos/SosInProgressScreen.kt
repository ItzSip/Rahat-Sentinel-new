package com.rahat.ui.sos

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahat.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SosInProgressScreen(
    onSosSent: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2500)
        onSosSent()
    }

    val glass = MaterialTheme.glass

    // Pulsing ring animation
    val infiniteTransition = rememberInfiniteTransition(label = "sosRing")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ringScale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "ringAlpha"
    )
    val innerPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "innerPulse"
    )

    // Dark glass overlay on gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(glass.backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        // Dark dimming layer for drama
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SOSRed.copy(alpha = 0.08f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Pulsing ring stack ─────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                // Outer pulse ring
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(ringScale)
                        .clip(CircleShape)
                        .border(3.dp, SOSRed.copy(alpha = ringAlpha), CircleShape)
                )
                // Middle ring
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(1f)
                        .clip(CircleShape)
                        .background(SOSRedGlass)
                        .border(2.dp, SOSRed.copy(alpha = 0.5f), CircleShape)
                )
                // Inner solid circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    SOSRed.copy(alpha = innerPulse),
                                    SOSRed.copy(alpha = innerPulse * 0.6f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = TextOnGlass,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Status text ────────────────────────────────────────────────
            Text(
                text = "Sending SOS",
                color = TextOnGlass,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Stay calm. Help is being notified.",
                color = TextOnGlassSecondary,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(32.dp))

            // ── Glass info card ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 40.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(SOSRedGlass, glass.cardBackground))
                    )
                    .border(1.5.dp, SOSRed.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Contacting emergency services", color = TextOnGlass, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text("SMS sent to family contacts", color = TextOnGlassSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Location shared", color = TextOnGlassSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}