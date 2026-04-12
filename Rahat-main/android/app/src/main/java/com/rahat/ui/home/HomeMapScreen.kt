package com.rahat.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.content.pm.PackageManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.rahat.data.AccessibilityPreferences
import com.rahat.data.model.PeerState
import com.rahat.data.model.SignalLevel
import com.rahat.service.Narrator
import com.rahat.state.MenuAction
import com.rahat.state.UiState
import com.rahat.ui.theme.*
import kotlinx.coroutines.*
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun HomeMapScreen(
    uiState: UiState,
    mapViewModel: MapViewModel,
    onNearbyHelpClick: () -> Unit,
    onStatusClick: () -> Unit,
    onMenuAction: (MenuAction) -> Unit,
    onOpenAlertFeed: () -> Unit,
    onSafeZoneClick: () -> Unit,
    onWarningBannerClick: () -> Unit,
    activeRiskLevel: RiskLevel,
    activeWarningZone: String?,
    sosManager: com.rahat.ui.sos.SosManager,
    accessibilityPrefs: AccessibilityPreferences,
    narrator: Narrator
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val glass   = MaterialTheme.glass

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val needed = mutableListOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ACCESS_FINE_LOCATION
        ).filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }.toMutableList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            ).forEach {
                if (ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED)
                    needed.add(it)
            }
        }
        if (needed.isNotEmpty()) permissionLauncher.launch(needed.toTypedArray())
    }

    val isNarratorEnabled by accessibilityPrefs.isNarratorEnabled.collectAsState()
    val narratorVolume    by accessibilityPrefs.narratorVolume.collectAsState()
    val sosState          by sosManager.sosState.collectAsState()
    val sosCountdown      by sosManager.countdown.collectAsState()

    LaunchedEffect(Unit) {
        org.osmdroid.config.Configuration.getInstance().load(
            context, androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        )
        org.osmdroid.config.Configuration.getInstance().userAgentValue = context.packageName
    }

    val drawerState  = rememberDrawerState(DrawerValue.Closed)
    val userLocation = mapViewModel.userLocation.collectAsState()
    val alerts       by mapViewModel.alerts.collectAsState()
    val nearbyPeers  by mapViewModel.nearbyPeers.collectAsState()
    var showDebugOverlay by remember { mutableStateOf(false) }
    var shouldCenter     by remember { mutableStateOf(true) }

    // FIX: track bluetooth state for icon
    var bluetoothEnabled by remember {
        mutableStateOf(
            try {
                val bm = context.getSystemService(android.content.Context.BLUETOOTH_SERVICE)
                        as? android.bluetooth.BluetoothManager
                bm?.adapter?.isEnabled == true
            } catch (e: Exception) { false }
        )
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationRequest = remember {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateDistanceMeters(2f).setWaitForAccurateLocation(false).build()
    }
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { mapViewModel.onLocationUpdated(it) }
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) { mapViewModel.onLocationUpdated(loc); shouldCenter = true }
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, context.mainLooper
            )
        } catch (e: SecurityException) {
            android.util.Log.e("HOME_MAP", "Permission lost: ${e.message}")
        }
    }
    DisposableEffect(Unit) {
        onDispose { fusedLocationClient.removeLocationUpdates(locationCallback) }
    }

    ModalNavigationDrawer(
        drawerState = drawerState, gesturesEnabled = false,
        drawerContent = {
            GlassDrawerContent(
                onClose = { scope.launch { drawerState.close() } },
                onMenuAction = onMenuAction, narrator = narrator,
                isNarratorEnabled = isNarratorEnabled, narratorVolume = narratorVolume
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            OsmMapView(
                userLocation = userLocation, alerts = alerts, nearbyPeers = nearbyPeers,
                acknowledgedAlertIds = emptyList(), onAlertAck = {}, onAlertClick = {},
                shouldCenter = shouldCenter, onCentered = { shouldCenter = false }
            )

            // ── FIX 3: Loading spinner while GPS not yet acquired ──────────
            if (userLocation.value == null) {
                LocationLoadingOverlay()
            }

            GlassTopBar(
                riskLevel       = activeRiskLevel,
                onMenuClick     = { scope.launch { drawerState.open() } },
                onAlertsClick   = {
                    narrator.speakIfEnabled("Open Alert Feed", isNarratorEnabled, narratorVolume)
                    onOpenAlertFeed()
                },
                hasLocation      = userLocation.value != null,
                bluetoothEnabled = bluetoothEnabled,   // FIX 1: pass BT state
                modifier         = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
            )

            if (activeRiskLevel != RiskLevel.SAFE) {
                RiskBanner(
                    riskLevel = activeRiskLevel,
                    zone      = activeWarningZone ?: "Your Area",
                    onClick   = onWarningBannerClick,
                    modifier  = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(top = 64.dp, start = 12.dp, end = 12.dp)
                )
            }

            GlassEmergencyPanel(
                peers    = nearbyPeers,
                modifier = Modifier.align(Alignment.TopEnd).padding(
                    top = if (activeRiskLevel != RiskLevel.SAFE) 132.dp else 72.dp,
                    end = 12.dp
                )
            )

            GlassBottomActions(
                riskLevel    = activeRiskLevel,
                onNearbyHelp = onNearbyHelpClick,
                onStatus     = onStatusClick,
                onSafeZone   = onSafeZoneClick,
                modifier     = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, start = 12.dp, end = 12.dp)
            )

            GlasSosButton(
                sosManager        = sosManager,
                isNarratorEnabled = isNarratorEnabled,
                narratorVolume    = narratorVolume,
                userLocation      = userLocation.value,
                modifier          = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 20.dp, bottom = 96.dp)
            )

            if (sosState == com.rahat.ui.sos.SosState.COUNTDOWN) {
                SosCountdownOverlay(countdown = sosCountdown)
            }
            if (showDebugOverlay) {
                DebugOverlay(nearbyPeers, Modifier.align(Alignment.TopCenter))
            }
        }
    }
}

// ─── FIX 3: Location Loading Overlay ─────────────────────────────────────────

@Composable
private fun LocationLoadingOverlay() {
    val glass = MaterialTheme.glass
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .shadow(16.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(glass.cardBackground)
                .border(1.dp, glass.cardBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 32.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color     = RahatBlue,
                    modifier  = Modifier.size(40.dp),
                    strokeWidth = 3.dp
                )
                Text(
                    "Acquiring location…",
                    color      = glass.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 14.sp
                )
                Text(
                    "Connecting to GPS",
                    color    = glass.textSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ─── FIX 1: GlassTopBar — bluetoothEnabled param added ───────────────────────

@Composable
private fun GlassTopBar(
    riskLevel: RiskLevel,
    onMenuClick: () -> Unit,
    onAlertsClick: () -> Unit,
    hasLocation: Boolean,
    bluetoothEnabled: Boolean,        // FIX 1: new param
    modifier: Modifier = Modifier
) {
    val glass = MaterialTheme.glass
    val shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
    Box(
        modifier = modifier
            .shadow(8.dp, shape,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor    = Color.Black.copy(alpha = 0.20f))
            .clip(shape)
            .background(glass.cardBackground)
            .border(1.dp, glass.cardBorder, shape)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, null, tint = glass.textPrimary)
            }
            Text("Rahat", color = glass.textPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Location icon
                GlassStatusIcon(Icons.Default.LocationOn, hasLocation)
                // Wifi icon
                GlassStatusIcon(Icons.Default.Wifi, true)
                // FIX 1: Bluetooth — different icon + red tint when OFF
                BluetoothStatusIcon(enabled = bluetoothEnabled)
                IconButton(onClick = onAlertsClick) {
                    Icon(Icons.Default.Notifications, null, tint = glass.textPrimary)
                }
            }
        }
    }
}

// ─── FIX 1: Bluetooth Icon — clearly shows ON (cyan) vs OFF (red + X icon) ───

@Composable
fun BluetoothStatusIcon(enabled: Boolean) {
    Box(contentAlignment = Alignment.Center) {
        if (enabled) {
            // ON: cyan bluetooth icon
            Icon(
                Icons.Default.Bluetooth,
                contentDescription = "Bluetooth on",
                tint     = RahatCyan,
                modifier = Modifier.size(22.dp).padding(horizontal = 2.dp)
            )
        } else {
            // OFF: show BluetoothDisabled icon with red tint so user clearly sees it's off
            Icon(
                Icons.Default.BluetoothDisabled,
                contentDescription = "Bluetooth off",
                tint     = Color(0xFFE57373),  // soft red — clearly "off" state
                modifier = Modifier.size(22.dp).padding(horizontal = 2.dp)
            )
        }
    }
}

// ─── Risk Banner ──────────────────────────────────────────────────────────────

@Composable
fun RiskBanner(
    riskLevel: RiskLevel, zone: String,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val solidColor  = riskSolidColor(riskLevel)
    val glassColor  = riskGlassColor(riskLevel)
    val borderColor = riskBorderColor(riskLevel)
    val label       = riskLabel(riskLevel)

    val infiniteTransition = rememberInfiniteTransition(label = "bannerPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.7f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "bannerAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(glassColor, glassColor.copy(alpha = 0.1f))))
            .border(1.5.dp,
                borderColor.copy(alpha = if (riskLevel == RiskLevel.CRITICAL) pulseAlpha else 0.7f),
                RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(Modifier.size(10.dp).background(solidColor, CircleShape))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(zone, color = Color.White, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall)
                    Text(label, color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            Icon(Icons.Default.ChevronRight, null,
                tint     = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp))
        }
    }
}

// ─── FIX 2: Glass Bottom Actions — higher opacity on Nearby Help ──────────────

@Composable
private fun GlassBottomActions(
    riskLevel: RiskLevel, onNearbyHelp: () -> Unit,
    onStatus: () -> Unit, onSafeZone: () -> Unit, modifier: Modifier = Modifier
) {
    val glass = MaterialTheme.glass
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .shadow(12.dp, shape,
                ambientColor = Color.Black.copy(alpha = 0.20f),
                spotColor    = Color.Black.copy(alpha = 0.25f))
            .clip(shape)
            // FIX 2: stronger background so it doesn't bleed with the map
            .background(
                if (glass.isDark) Color(0xDD142035)
                else Color(0xF0FFFFFF)
            )
            .border(1.dp, glass.cardBorder, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // FIX 2: Nearby Help — outlined but with much higher bg opacity
            GlassActionButton(
                label    = "Nearby Help",
                icon     = Icons.Default.People,
                color    = RahatBlue,
                outlined = true,
                modifier = Modifier.weight(1f),
                onClick  = onNearbyHelp
            )
            GlassActionButton(
                label    = "Status",
                icon     = Icons.Default.Dashboard,
                color    = RiskSafeGreen,
                outlined = false,
                modifier = Modifier.weight(1f),
                onClick  = onStatus
            )
            if (riskLevel != RiskLevel.SAFE) {
                GlassActionButton(
                    label    = "Safe Zone",
                    icon     = Icons.Default.Shield,
                    color    = riskSolidColor(riskLevel),
                    outlined = false,
                    modifier = Modifier.weight(1f),
                    onClick  = onSafeZone
                )
            }
        }
    }
}

@Composable
private fun GlassActionButton(
    label: String, icon: ImageVector, color: Color, outlined: Boolean,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            // FIX 2: outlined buttons now 0.18f bg (was 0.1f) — more visible on map
            .background(if (outlined) color.copy(alpha = 0.18f) else color.copy(alpha = 0.85f))
            .border(
                width = 1.5.dp,   // slightly thicker border too
                color = color.copy(alpha = if (outlined) 0.75f else 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null,
                tint     = if (outlined) color else Color.White,
                modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(3.dp))
            Text(label,
                color      = if (outlined) color else Color.White,
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold,   // bolder label too
                textAlign  = TextAlign.Center)
        }
    }
}

// ─── SOS Button ───────────────────────────────────────────────────────────────

@Composable
private fun GlasSosButton(
    sosManager: com.rahat.ui.sos.SosManager, isNarratorEnabled: Boolean,
    narratorVolume: Float, userLocation: org.osmdroid.util.GeoPoint?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sosPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue  = 1f, targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "sosScale"
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(Modifier.size(72.dp)
            .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
            .background(SOSRedGlass, CircleShape))
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    Brush.radialGradient(listOf(Color(0xFFFF5252), SOSRed)), CircleShape
                )
                .border(2.dp, GlassBorder, CircleShape)
                .pointerInput(isNarratorEnabled, narratorVolume) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitFirstDown()
                            val coords = if (userLocation != null)
                                "Lat: ${userLocation.latitude}, Lng: ${userLocation.longitude}"
                            else "Lat: 21.1458, Lng: 79.0882"
                            sosManager.startCountdown(
                                isNarratorEnabled, narratorVolume, "User", coords
                            )
                            val result = withTimeoutOrNull(5000) { waitForUpOrCancellation() }
                            if (result != null) sosManager.cancelCountdown()
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("SOS", color = Color.White, fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp, letterSpacing = 1.sp)
        }
    }
}

// ─── SOS Countdown ────────────────────────────────────────────────────────────

@Composable
private fun SosCountdownOverlay(countdown: Int) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(countdown.toString(), color = Color.White, fontSize = 120.sp,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Text("HOLD TO SEND SOS", color = Color.White, fontSize = 22.sp,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("RELEASE TO CANCEL", color = Color.White.copy(alpha = 0.7f), fontSize = 15.sp)
        }
    }
}

// ─── Glass Emergency Panel ────────────────────────────────────────────────────

@Composable
fun GlassEmergencyPanel(peers: List<PeerState>, modifier: Modifier = Modifier) {
    val highPeers = peers.filter { it.severity == "HIGH" }.take(3)
    if (highPeers.isEmpty()) return
    val glass = MaterialTheme.glass
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .width(190.dp)
            .shadow(6.dp, shape,
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor    = Color.Black.copy(alpha = 0.15f))
            .clip(shape)
            .background(glass.cardBackground)
            .border(1.dp, glass.cardBorder, shape)
            .padding(12.dp)
    ) {
        Text("Nearby Emergency", color = glass.textPrimary, fontSize = 13.sp,
            fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        highPeers.forEach { peer ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(RiskWarningOrange, CircleShape))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(peer.name, color = glass.textPrimary, fontSize = 12.sp,
                        fontWeight = FontWeight.Medium)
                    Text("${peer.signalLevel} • ${peer.signalTrend}",
                        color = glass.textMuted, fontSize = 10.sp)
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

// ─── Glass Drawer ─────────────────────────────────────────────────────────────

@Composable
fun GlassDrawerContent(
    onClose: () -> Unit, onMenuAction: (MenuAction) -> Unit,
    narrator: Narrator, isNarratorEnabled: Boolean, narratorVolume: Float
) {
    val context = LocalContext.current
    val glass   = MaterialTheme.glass
    var displayName by remember { mutableStateOf("Rahat User") }

    LaunchedEffect(Unit) {
        val db = com.rahat.data.local.RahatDatabase.getDatabase(context)
        db.rahatDao().getDeviceOneShot()?.let { device ->
            db.rahatDao().getUserProfile(device.rId).collect { profile ->
                profile?.let { displayName = it.name }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(290.dp)
            .background(glass.backgroundGradient)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(glass.cardBackground)
                .border(1.dp, glass.cardBorder,
                    RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Icon(Icons.Default.AccountCircle, null,
                        tint = Color.White, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(displayName, color = Color.White, fontSize = 18.sp,
                        fontWeight = FontWeight.Bold)
                    Text("Active Mode", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                }
                IconButton(onClick = {
                    narrator.speakIfEnabled("Close Menu", isNarratorEnabled, narratorVolume)
                    onClose()
                }) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        GlassDrawerItem(Icons.Default.Person, "Profile", Color.White) {
            narrator.speakIfEnabled("Open Profile", isNarratorEnabled, narratorVolume)
            onMenuAction(MenuAction.PROFILE); onClose()
        }
        GlassDrawerItem(Icons.Default.Settings, "Settings", Color.White) {
            narrator.speakIfEnabled("Open Settings", isNarratorEnabled, narratorVolume)
            onMenuAction(MenuAction.SETTINGS); onClose()
        }
        Spacer(Modifier.weight(1f))
        GlassDrawerItem(Icons.Default.ExitToApp, "Logout", RiskCriticalRed) {
            onMenuAction(MenuAction.LOGOUT); onClose()
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun GlassDrawerItem(
    icon: ImageVector, title: String,
    color: Color = Color.White, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

// ─── Debug / Status helpers ───────────────────────────────────────────────────

@Composable
fun DebugOverlay(peers: List<PeerState>, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(150.dp).background(Color.Black.copy(0.8f)).padding(8.dp)) {
        LazyColumn {
            item {
                Text("DEBUG: MESH PEERS", color = Color.Yellow,
                    fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            items(peers) { peer ->
                Text(
                    "ID: ${peer.rId} | Signal: ${peer.signalLevel} | " +
                            "Trend: ${peer.signalTrend} | " +
                            "Seen: ${System.currentTimeMillis() - peer.lastSeen}ms",
                    color = Color.White, fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun GlassStatusIcon(icon: ImageVector, enabled: Boolean) {
    val glass = MaterialTheme.glass
    Icon(icon, null,
        tint     = if (enabled) RahatCyan else glass.textMuted,
        modifier = Modifier.size(22.dp).padding(horizontal = 2.dp))
}

// ─── OSM Map View ─────────────────────────────────────────────────────────────

@Composable
fun OsmMapView(
    userLocation: androidx.compose.runtime.State<org.osmdroid.util.GeoPoint?>,
    alerts: List<com.rahat.data.model.Alert>,
    nearbyPeers: List<PeerState>,
    acknowledgedAlertIds: List<String>,
    onAlertAck: (String) -> Unit,
    onAlertClick: (com.rahat.data.model.Alert) -> Unit,
    shouldCenter: Boolean,
    onCentered: () -> Unit
) {
    val context     = LocalContext.current
    val peerMarkers = remember { mutableMapOf<String, org.osmdroid.views.overlay.Marker>() }
    val peerAngles  = remember { mutableMapOf<String, Double>() }

    AndroidView(
        factory = { ctx ->
            org.osmdroid.views.MapView(ctx).apply {
                setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                zoomController.setVisibility(
                    org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER
                )
                controller.setZoom(18.0)
                setBuiltInZoomControls(false)
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { mapView ->
            if (shouldCenter) {
                userLocation.value?.let { mapView.controller.animateTo(it); onCentered() }
            }
            mapView.overlays.clear()

            userLocation.value?.let { geo ->
                mapView.overlays.add(org.osmdroid.views.overlay.Marker(mapView).apply {
                    position = geo
                    icon     = createSimpleIcon(context, android.graphics.Color.BLUE)
                    setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                        org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                    setOnMarkerClickListener { _, _ -> true }
                })
            }

            alerts.forEach { alert ->
                mapView.overlays.add(org.osmdroid.views.overlay.Marker(mapView).apply {
                    position = org.osmdroid.util.GeoPoint(alert.lat, alert.lon)
                    icon     = createSleekMarker(context, getSeverityColorInt(alert.severity.name))
                    setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                        org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                    title = alert.severity.name
                    setOnMarkerClickListener { _, _ -> onAlertClick(alert); true }
                })
            }

            val now = System.currentTimeMillis()
            nearbyPeers.forEach { peer ->
                val isStale = now - peer.lastSeen > 30000
                val marker  = peerMarkers.getOrPut(peer.rId) {
                    org.osmdroid.views.overlay.Marker(mapView).apply {
                        setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                            org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                    }
                }
                val userLoc = userLocation.value
                if (peer.latitude != null && peer.longitude != null) {
                    marker.position = org.osmdroid.util.GeoPoint(peer.latitude, peer.longitude)
                } else if (userLoc != null) {
                    val dist = when (peer.signalLevel) {
                        SignalLevel.VERY_STRONG -> 10.0
                        SignalLevel.STRONG      -> 30.0
                        SignalLevel.MODERATE    -> 70.0
                        SignalLevel.WEAK        -> 150.0
                    }
                    val angle = peerAngles.getOrPut(peer.rId) { Math.random() * 2 * Math.PI }
                    marker.position = org.osmdroid.util.GeoPoint(
                        userLoc.latitude  + (dist * Math.cos(angle)) / 111111.0,
                        userLoc.longitude + (dist * Math.sin(angle)) /
                                (111111.0 * Math.cos(Math.toRadians(userLoc.latitude)))
                    )
                }
                marker.icon  = createPeerIcon(context,
                    android.graphics.Color.parseColor("#FF8C00"),
                    if (isStale) 60 else 255, peer.signalLevel)
                marker.title = "${peer.name} (${peer.signalTrend})"
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        }
    )
}

private fun createSimpleIcon(context: android.content.Context, color: Int): android.graphics.drawable.Drawable {
    val size = 50
    val bmp  = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val c    = android.graphics.Canvas(bmp)
    val p    = android.graphics.Paint().apply { isAntiAlias = true; this.color = color }
    c.drawCircle(size / 2f, size / 2f, size / 2f, p.apply { alpha = 50 })
    c.drawCircle(size / 2f, size / 2f, size / 4f, p.apply { alpha = 255 })
    return android.graphics.drawable.BitmapDrawable(context.resources, bmp)
}

fun getSeverityColorInt(sev: String): Int = when (sev.uppercase()) {
    "CRITICAL" -> android.graphics.Color.RED
    "HIGH"     -> android.graphics.Color.parseColor("#FF8C00")
    "MEDIUM"   -> android.graphics.Color.YELLOW
    else       -> android.graphics.Color.GREEN
}

private fun createPeerIcon(
    context: android.content.Context, color: Int, alpha: Int, level: SignalLevel
): android.graphics.drawable.Drawable {
    val baseSize = 56
    val size = when (level) {
        SignalLevel.VERY_STRONG -> 64; SignalLevel.STRONG -> 56
        SignalLevel.MODERATE    -> 48; SignalLevel.WEAK   -> 40
    }
    val bmp = android.graphics.Bitmap.createBitmap(baseSize, baseSize,
        android.graphics.Bitmap.Config.ARGB_8888)
    val c   = android.graphics.Canvas(bmp)
    val p   = android.graphics.Paint().apply { isAntiAlias = true; this.color = color }
    c.drawCircle(baseSize / 2f, baseSize / 2f, size / 2f, p.apply { this.alpha = alpha / 4 })
    c.drawCircle(baseSize / 2f, baseSize / 2f, size / 4f, p.apply { this.alpha = alpha })
    return android.graphics.drawable.BitmapDrawable(context.resources, bmp)
}

private fun createSleekMarker(
    context: android.content.Context, color: Int
): android.graphics.drawable.Drawable {
    val size   = 64
    val bitmap = android.graphics.Bitmap.createBitmap(size, size,
        android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint  = android.graphics.Paint().apply { isAntiAlias = true }
    paint.color = android.graphics.Color.BLACK; paint.alpha = 40
    canvas.drawCircle(size / 2f, size / 2f + 2f, size / 2.5f, paint)
    paint.color = android.graphics.Color.WHITE; paint.alpha = 255
    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)
    paint.color = color
    canvas.drawCircle(size / 2f, size / 2f, size / 3.8f, paint)
    return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
}