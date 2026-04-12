package com.rahat.ui.sos

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahat.ui.theme.*

@Composable
fun SosSuccessScreen(
    activeRiskLevel: RiskLevel = RiskLevel.SAFE,
    activeZone: String? = null,
    onDismiss: () -> Unit = {},
    onBackHome: () -> Unit = onDismiss
) {
    val glass = MaterialTheme.glass

    val infiniteTransition = rememberInfiniteTransition(label = "successPulse")
    val checkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "checkAlpha"
    )

    val showRiskCard = activeRiskLevel != RiskLevel.SAFE && activeRiskLevel != RiskLevel.OFFLINE
    val riskSolid  = riskSolidColor(activeRiskLevel)
    val riskGlass  = riskGlassColor(activeRiskLevel)
    val riskBorder = riskBorderColor(activeRiskLevel)
    val riskLbl    = riskLabel(activeRiskLevel)

    Box(
        modifier = Modifier.fillMaxSize().background(glass.backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            // Check icon
            Box(
                modifier = Modifier.size(100.dp).clip(CircleShape)
                    .background(RiskSafeGreen.copy(alpha = 0.15f))
                    .border(2.dp, RiskSafeGreen.copy(alpha = checkAlpha * 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, null,
                    tint = RiskSafeGreen.copy(alpha = checkAlpha), modifier = Modifier.size(56.dp))
            }

            Spacer(Modifier.height(24.dp))
            Text("SOS Sent", color = TextOnGlass, fontWeight = FontWeight.Bold, fontSize = 26.sp)
            Spacer(Modifier.height(6.dp))
            Text("Rescue teams have been alerted.\nStay where you are.",
                color = TextOnGlassSecondary, fontSize = 14.sp,
                textAlign = TextAlign.Center, lineHeight = 20.sp)

            Spacer(Modifier.height(28.dp))

            // Confirmation chips
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SosConfirmChip(Icons.Default.Sms, "SMS sent", RiskSafeGreen, Modifier.weight(1f))
                SosConfirmChip(Icons.Default.Phone, "112 dialled", RiskSafeGreen, Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            // Risk zone card
            if (showRiskCard) {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(riskGlass, glass.cardBackground)))
                        .border(1.5.dp, riskBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = riskSolid, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("High-risk zone detected", color = TextOnGlass,
                                fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            if (!activeZone.isNullOrBlank())
                                Text(activeZone, color = TextOnGlassSecondary, fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                .background(riskSolid.copy(alpha = 0.2f))
                                .border(1.dp, riskSolid.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) { Text(riskLbl, color = riskSolid, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(glass.cardBackground)
                        .border(1.dp, glass.cardBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bluetooth, null, tint = RahatCyan, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Nearby devices notified via BLE mesh", color = TextOnGlassSecondary, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(24.dp))
            } else {
                Spacer(Modifier.height(8.dp))
            }

            // Dismiss button
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(RahatBlue, RahatCyan.copy(alpha = 0.8f))))
                    .border(1.dp, RahatCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("Back to Home", color = TextOnGlass, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun SosConfirmChip(icon: ImageVector, label: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}