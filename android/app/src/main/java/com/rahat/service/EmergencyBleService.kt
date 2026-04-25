package com.rahat.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.rahat.MainActivity
import com.rahat.R
import com.rahat.data.local.RahatDatabase
import javax.crypto.SecretKey
import com.rahat.data.repo.PeerResolver
import com.rahat.data.model.PeerState
import com.rahat.data.model.PeerSource
import com.rahat.service.ble.BleAdvertiser
import com.rahat.service.ble.BleChannels
import com.rahat.service.ble.DeviceRole
import com.rahat.service.ble.BleGattClient
import com.rahat.service.ble.BleGattServer
import com.rahat.service.ble.BleScanner
import com.rahat.service.ble.PeerManager
import com.rahat.security.IdentityManager
import com.rahat.data.repo.MeshRepository
import kotlinx.coroutines.*
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent

/**
 * Senior Architect Implementation: EmergencyBleService
 * 
 * DESIGN PRINCIPLES:
 * 1. ADVERTISING STABILITY: Start once, stay alive. No redundant cycles.
 * 2. DECOUPLED ROTATION: EphID rotates every 10 mins without breaking BLE session.
 * 3. TRANSPARENCY: Mandatory audit logs for all orchestration events.
 */
class EmergencyBleService : Service() {

    private val TAG = "BLE_SERVICE"
    private val NOTIFICATION_ID = 101
    private val CHANNEL_ID = "rahat_ble_channel"
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private lateinit var advertiser: BleAdvertiser
    private lateinit var scanner: BleScanner
    private lateinit var peerManager: PeerManager
    private lateinit var identityManager: IdentityManager
    private lateinit var peerResolver: PeerResolver
    private lateinit var database: RahatDatabase
    private lateinit var gattServer: BleGattServer
    private lateinit var gattClient: BleGattClient
    
    // Rotation Settings
    private val ROTATION_INTERVAL_MS = IdentityManager.TIME_WINDOW_MS // 10 Minutes

    private var currentSeverity = 0 // 0=OK,1=GREEN,2=ORANGE,3=RED — updated from JS
    private var lastLat = 0.0
    private var lastLng = 0.0

    private var orchestrationJob: Job? = null
    private var currentEphId: String? = null
    
    private val bluetoothStateReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(android.bluetooth.BluetoothAdapter.EXTRA_STATE, android.bluetooth.BluetoothAdapter.ERROR)
            when (state) {
                android.bluetooth.BluetoothAdapter.STATE_ON -> {
                    Log.i(TAG, "BLUETOOTH_STATE_ON: Re-activating Mesh Orchestration")
                    if (checkAllPermissions()) startMeshOrchestration()
                }
                android.bluetooth.BluetoothAdapter.STATE_OFF -> {
                    Log.w(TAG, "BLUETOOTH_STATE_OFF: Suspending Mesh Operations")
                    stopMeshOrchestration()
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "BLE_SERVICE_CREATED: Initializing Architecture")
        
        database = RahatDatabase.getDatabase(this)
        identityManager = IdentityManager(this)
        peerResolver = PeerResolver(this, identityManager)
        
        // Initialize PeerManager (The Brain)
        peerManager = PeerManager(serviceScope)

        // Initialize BLE Comms
        advertiser = BleAdvertiser(this)
        scanner = BleScanner(this, peerManager)

        // Initialize GATT data channel (bidirectional event frame transport)
        gattServer = BleGattServer(this)
        gattClient = BleGattClient(this)
        
        // Register Bluetooth State Monitor
        val filter = android.content.IntentFilter(android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStateReceiver, filter)
        
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "BLE_SERVICE_ON_START_COMMAND: Activating Mesh")
        
        intent?.let {
            val newLat = it.getDoubleExtra("lat", Double.MIN_VALUE)
            val newLng = it.getDoubleExtra("lng", Double.MIN_VALUE)
            val newSev = it.getIntExtra("severity", -1)

            if (newSev >= 0) {
                currentSeverity = newSev.coerceIn(0, 3)
                Log.d(TAG, "BLE_SEVERITY_UPDATE: $currentSeverity")
            }
            if (newLat != Double.MIN_VALUE && newLng != Double.MIN_VALUE) {
                lastLat = newLat
                lastLng = newLng
            }
            // Push updated payload if advertiser is running
            if (newSev >= 0 || (newLat != Double.MIN_VALUE)) {
                currentEphId?.let { ephId ->
                    if (this::advertiser.isInitialized) {
                        advertiser.startOrUpdateAdvertising(ephId, currentSeverity, lastLat, lastLng)
                    }
                }
            }
        }

        if (checkAllPermissions()) {
            startMeshOrchestration()
        } else {
            Log.e(TAG, "BLE_SERVICE_ABORTED: Fatal Permission Denied")
        }

        return START_STICKY
    }

    private fun startMeshOrchestration() {
        if (orchestrationJob?.isActive == true) return

        orchestrationJob = serviceScope.launch {
            Log.i(TAG, "BLE_MESH_ORCHESTRATOR: Starting — waiting for Bluetooth to be ON")

            // Poll every 2s indefinitely until BT is ON or job is cancelled.
            // The bluetoothStateReceiver will also re-trigger this if BT turns on later.
            while (isActive) {
                val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                if (adapter?.isEnabled == true) break
                Log.w(TAG, "BLE_MESH_ORCHESTRATOR: Waiting for BT...")
                delay(2000)
            }
            if (!isActive) return@launch

            val role = BleChannels.role
            Log.i(TAG, "BLE_MESH_ORCHESTRATOR: Role=${role.name}")

            // ── SENDER: host GATT server + advertise ────────────────────────────
            if (role == DeviceRole.SENDER || role == DeviceRole.FULL) {
                gattServer.start()
                BleChannels.sender = { frame -> gattClient.sendToAll(frame) }
                Log.i(TAG, "GATT_SERVER_READY")
            }

            // ── RECEIVER: scan + connect as GATT client ─────────────────────────
            if (role == DeviceRole.RECEIVER || role == DeviceRole.FULL) {
                scanner.onDeviceFound = { device -> gattClient.connectToPeer(device) }
                scanner.startScanning()
                Log.i(TAG, "BLE_SCANNER_STARTED")
            }

            // ── Advertise + rotate EphID loop (SENDER or FULL only) ─────────────
            if (role == DeviceRole.SENDER || role == DeviceRole.FULL) {
                while (isActive) {
                    try {
                        val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                        if (adapter?.isEnabled == true) {
                            val secret = identityManager.getOrCreateDeviceSecret()
                            val selfEphId = identityManager.generateEphemeralIdAtOffset(secret, ROTATION_INTERVAL_MS, 0L)
                            currentEphId = selfEphId

                            Log.i(TAG, "BLE_EPHID_ROTATION_EVENT: New ID: $selfEphId [${ROTATION_INTERVAL_MS/60000} min rotation]")
                            advertiser.startOrUpdateAdvertising(selfEphId, currentSeverity, lastLat, lastLng)
                        } else {
                            Log.w(TAG, "BLE_ORCHESTRATION_WAITING: Bluetooth is OFF — pausing advertising")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "BLE_ORCHESTRATION_ERROR: ${e.message}")
                    }
                    delay(ROTATION_INTERVAL_MS - 5000)
                }
            } else {
                // RECEIVER-only: keep coroutine alive so scanner stays running
                while (isActive) { delay(60_000) }
            }
        }
    }

    private fun stopMeshOrchestration() {
        val role = BleChannels.role
        orchestrationJob?.cancel()
        if (role == DeviceRole.RECEIVER || role == DeviceRole.FULL) {
            scanner.stopScanning()
            scanner.onDeviceFound = null
            gattClient.stop()
        }
        if (role == DeviceRole.SENDER || role == DeviceRole.FULL) {
            advertiser.stopAdvertising()
            gattServer.stop()
            BleChannels.sender = null
        }
    }

    private fun checkAllPermissions(): Boolean {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(android.Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(android.Manifest.permission.BLUETOOTH)
            permissions.add(android.Manifest.permission.BLUETOOTH_ADMIN)
        }
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)

        return permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Rahat BLE Network", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rahat Emergency Mesh")
            .setContentText("Mesh network active and secure")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "BLE_SERVICE_STOPPED: Tearing down Mesh")
        try {
            unregisterReceiver(bluetoothStateReceiver)
        } catch (e: Exception) {
            // Already unregistered or not registered
        }
        stopMeshOrchestration()
        serviceScope.cancel()
    }
}
