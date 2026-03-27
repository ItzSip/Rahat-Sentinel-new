package com.rahat.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.rahat.data.AccessibilityPreferences
import com.rahat.service.Narrator
import com.rahat.state.MenuAction
import com.rahat.state.UiState
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

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
    sosManager: com.rahat.ui.sos.SosManager,
    accessibilityPrefs: AccessibilityPreferences,
    narrator: Narrator
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions -> }

    LaunchedEffect(Unit) {
        val permissionsNeeded = mutableListOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsNeeded.addAll(listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            ))
        }
        permissionLauncher.launch(permissionsNeeded.toTypedArray())

        Configuration.getInstance().load(context, androidx.preference.PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val isNarratorEnabled by accessibilityPrefs.isNarratorEnabled.collectAsState()
    val narratorVolume by accessibilityPrefs.narratorVolume.collectAsState()
    val sosState by sosManager.sosState.collectAsState()
    val sosCountdown by sosManager.countdown.collectAsState()
    val userLocation = mapViewModel.userLocation.collectAsState()
    val alerts by mapViewModel.alerts.collectAsState()
    val nearbyPeers by mapViewModel.nearbyPeers.collectAsState()

    var showMapOptions by remember { mutableStateOf(false) }
    var selectedAlert by remember { mutableStateOf<com.rahat.data.model.Alert?>(null) }
    var shouldCenter by remember { mutableStateOf(true) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationRequest = remember { LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L).build() }
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { mapViewModel.onLocationUpdated(it) }
            }
        }
    }

    LaunchedEffect(Unit) {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, context.mainLooper)
    }

    ModalNavigationDrawer(
        drawerState = rememberDrawerState(DrawerValue.Closed),
        gesturesEnabled = false,
        drawerContent = {
            DrawerContent(
                onClose = { /* Close logic */ },
                onMenuAction = onMenuAction,
                narrator = narrator,
                isNarratorEnabled = isNarratorEnabled,
                narratorVolume = narratorVolume
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Rahat", fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = { }) { Icon(Icons.Default.Menu, null) } },
                    actions = {
                        IconButton(onClick = { onOpenAlertFeed() }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                        }
                        StatusIcon(Icons.Default.LocationOn, userLocation.value != null)
                        StatusIcon(Icons.Default.Bluetooth, true)
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {

                // 1. THE MAP (Base Layer - Fixed Visibility)
                OsmMapView(
                    userLocation = remember { derivedStateOf { userLocation.value?.let { GeoPoint(it.latitude, it.longitude) } } },
                    alerts = alerts,
                    nearbyPeers = nearbyPeers,
                    acknowledgedAlertIds = emptyList(),
                    onAlertAck = {},
                    onAlertClick = { selectedAlert = it },
                    shouldCenter = shouldCenter,
                    onCentered = { shouldCenter = false }
                )

                // 2. EMERGENCY OVERLAY (Top Right)
                EmergencyPanel(
                    peers = nearbyPeers,
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                )

                // 3. MAP LAYERS BUTTON
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 160.dp)
                        .size(48.dp).background(Color.Black.copy(0.6f), CircleShape)
                        .clickable { showMapOptions = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Layers, null, tint = Color.White)
                }

                // 4. SOS BUTTON (Pressure Input)
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 80.dp)
                        .size(72.dp).background(Color(0xFFFF3B30), CircleShape)
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                sosManager.startCountdown(isNarratorEnabled, narratorVolume, "User", "Live")
                                tryAwaitRelease()
                                sosManager.cancelCountdown()
                            })
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("SOS", color = Color.White, fontWeight = FontWeight.Black)
                }

                // 5. BOTTOM BUTTONS (Integrated AI Forecast)
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onNearbyHelpClick() },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Icon(Icons.Default.Group, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Nearby")
                    }

                    // HIGH VISIBILITY RADAR BUTTON
                    Button(
                        onClick = { onStatusClick() },
                        modifier = Modifier.weight(1.2f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Radar, null)
                        Spacer(Modifier.width(8.dp))
                        Text("AI FORECAST", fontWeight = FontWeight.Bold)
                    }
                }

                // 6. SOS COUNTDOWN OVERLAY
                if (sosState == com.rahat.ui.sos.SosState.COUNTDOWN) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.7f)), contentAlignment = Alignment.Center) {
                        Text(sosCountdown.toString(), color = Color.White, fontSize = 100.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (showMapOptions) {
                    ModalBottomSheet(onDismissRequest = { showMapOptions = false }, containerColor = Color(0xFF1E1E1E)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                            Text("Map Layers", color = Color.White, fontWeight = FontWeight.Bold)
                            MapOptionItem("Offline Topo") { showMapOptions = false }
                            MapOptionItem("Rain Forecast") { showMapOptions = false }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OsmMapView(
    userLocation: State<GeoPoint?>,
    alerts: List<com.rahat.data.model.Alert>,
    nearbyPeers: List<com.rahat.data.model.PeerState>,
    acknowledgedAlertIds: List<String>,
    onAlertAck: (String) -> Unit,
    onAlertClick: (com.rahat.data.model.Alert) -> Unit,
    shouldCenter: Boolean,
    onCentered: () -> Unit
) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
                controller.setZoom(17.0)
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { mapView ->
            if (shouldCenter) {
                userLocation.value?.let { geo ->
                    mapView.controller.animateTo(geo)
                    onCentered()
                }
            }
            mapView.overlays.clear()
            userLocation.value?.let { geo ->
                val marker = Marker(mapView).apply {
                    position = geo
                    icon = createSimpleIcon(context, android.graphics.Color.BLUE)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        }
    )
}

@Composable
fun EmergencyPanel(peers: List<com.rahat.data.model.PeerState>, modifier: Modifier) {
    val highPeers = peers.filter { it.severity == "HIGH" }.take(3)
    if (highPeers.isEmpty()) return
    Column(modifier = modifier.width(200.dp).background(Color.Black.copy(0.7f), RoundedCornerShape(12.dp)).padding(12.dp)) {
        Text("Nearby Alerts", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        highPeers.forEach { peer ->
            Text("${peer.name}: ${peer.signalLevel}", color = Color.White.copy(0.8f), fontSize = 12.sp)
        }
    }
}

private fun createSimpleIcon(context: Context, color: Int): android.graphics.drawable.Drawable {
    val bmp = android.graphics.Bitmap.createBitmap(40, 40, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bmp)
    val paint = android.graphics.Paint().apply { isAntiAlias = true; this.color = color }
    canvas.drawCircle(20f, 20f, 18f, paint.apply { alpha = 100 })
    canvas.drawCircle(20f, 20f, 8f, paint.apply { alpha = 255 })
    return android.graphics.drawable.BitmapDrawable(context.resources, bmp)
}

@Composable
fun DrawerContent(onClose: () -> Unit, onMenuAction: (MenuAction) -> Unit, narrator: Narrator, isNarratorEnabled: Boolean, narratorVolume: Float) {
    Column(modifier = Modifier.fillMaxHeight().width(280.dp).background(Color.DarkGray).padding(16.dp)) {
        Text("Rahat Menu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(20.dp))
        DrawerItem(Icons.Default.Person, "Profile", Color.White) { onMenuAction(MenuAction.PROFILE) }
        DrawerItem(Icons.Default.Settings, "Settings", Color.White) { onMenuAction(MenuAction.SETTINGS) }
    }
}

@Composable
fun DrawerItem(icon: ImageVector, title: String, color: Color, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(12.dp)) {
        Icon(icon, null, tint = color); Spacer(Modifier.width(12.dp)); Text(title, color = color)
    }
}

@Composable
fun StatusIcon(icon: ImageVector, enabled: Boolean) {
    Icon(icon, null, tint = if (enabled) Color(0xFF2196F3) else Color.Gray, modifier = Modifier.padding(horizontal = 4.dp))
}

@Composable
fun MapOptionItem(title: String, onClick: () -> Unit) {
    Text(title, color = Color.White, modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(12.dp))
}