package com.rahat.ui.alert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahat.data.model.PeerState
import com.rahat.data.model.SignalTrend

data class ClimateRisk(
    val title: String,
    val riskLevel: String,
    val description: String,
    val area: String,
    val time: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertFeedScreen(
    peers: List<PeerState>,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Nearby SOS", "Climate Forecast")

    val climateAlerts = remember {
        listOf(
            ClimateRisk(
                "Abnormal Rainfall", "CRITICAL",
                "Satellite data detects high-intensity cloud formation. 80% flood risk.",
                "Sector 7, Bhilai", "In 2 hours", Color(0xFF1976D2)
            ),
            ClimateRisk(
                "Heatwave Anomaly", "HIGH",
                "Geospatial sensors report 4.5°C departure from normal. Risk of heatstroke.",
                "Durg Region", "Next 24h", Color(0xFFE65100)
            ),
            ClimateRisk(
                "Storm Surge", "MODERATE",
                "Spatiotemporal analysis predicts high-velocity winds.",
                "Raipur East", "In 6 hours", Color(0xFF455A64)
            )
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("🚨 Emergency Feed", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surface) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFF121212))) {
            if (selectedTab == 0) {
                PeerListContent(peers)
            } else {
                ClimateForecastContent(climateAlerts)
            }
        }
    }
}

@Composable
fun ClimateForecastContent(alerts: List<ClimateRisk>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "AI SPATIOTEMPORAL ANALYSIS",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(alerts) { alert ->
            ClimateRiskCard(alert)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Fixed: Added OptIn for Badge
@Composable
fun ClimateRiskCard(alert: ClimateRisk) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, alert.color.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(alert.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = alert.color)
                Badge(containerColor = alert.color) { Text(alert.riskLevel, color = Color.White) }
            }
            Spacer(Modifier.height(8.dp))
            Text(alert.description, color = Color.White, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            Divider(color = Color.White.copy(alpha = 0.1f))
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Text(" ${alert.area}", color = Color.Gray, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Text(" ${alert.time}", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PeerListContent(peers: List<PeerState>) {
    val sortedPeers = remember(peers) {
        peers.filter { it.severity == "HIGH" }.sortedBy { it.signalLevel.ordinal }
    }

    if (sortedPeers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color.Red)
                Spacer(Modifier.height(16.dp))
                Text("Scanning mesh network...", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sortedPeers) { peer -> PeerAlertCard(peer) }
        }
    }
}

@Composable
fun PeerAlertCard(peer: PeerState) {
    val cardColor = if (peer.severity == "HIGH") Color(0xFFB71C1C) else Color(0xFF1E1E1E)
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = peer.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Signal: ${peer.signalLevel}", color = Color.LightGray, fontSize = 14.sp)
                Text(
                    text = "Trend: ${peer.signalTrend}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (peer.signalTrend) {
                        SignalTrend.APPROACHING -> Color.Green
                        SignalTrend.RECEDING -> Color.Red
                        else -> Color.Gray
                    }
                )
            }
            if (peer.severity == "HIGH") {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Yellow)
            }
        }
    }
}