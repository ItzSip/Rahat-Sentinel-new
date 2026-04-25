package com.rahat

import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.rahat.data.repo.MeshRepository
import com.rahat.service.EmergencyBleService
import com.rahat.service.ble.BleChannels
import com.rahat.service.ble.DeviceRole
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class RahatMeshModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isObserving = false

    override fun getName(): String = "RahatMesh"

    @ReactMethod
    fun startScanning() {
        Log.i("RahatMeshModule", "startScanning triggered from JS")

        // Wire received-frame callback: GATT server → JS "onDataReceived" event.
        // Set before starting the service so no frames are missed.
        BleChannels.onFrameReceived = { frame -> sendEventToJS(frame) }

        val intent = Intent(reactContext, EmergencyBleService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            reactContext.startForegroundService(intent)
        } else {
            reactContext.startService(intent)
        }
        startObservingPeers()
    }

    @ReactMethod
    fun stopScanning() {
        Log.i("RahatMeshModule", "stopScanning triggered from JS")
        val intent = Intent(reactContext, EmergencyBleService::class.java)
        reactContext.stopService(intent)
    }

    @ReactMethod
    fun bleSend(payload: String) {
        Log.d("RahatMeshModule", "[BLE TX] $payload")
        // Cache DISASTER frames — delivered to peers that connect after initial broadcast
        if (payload.contains("\"DISASTER\"")) {
            BleChannels.pendingDisasterFrame = payload
        }
        BleChannels.send(payload)
    }

    @ReactMethod
    fun updateLocation(lat: Double, lng: Double) {
        val intent = Intent(reactContext, EmergencyBleService::class.java)
        intent.putExtra("lat", lat)
        intent.putExtra("lng", lng)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            reactContext.startForegroundService(intent)
        } else {
            reactContext.startService(intent)
        }
    }

    /** Push current severity level to the BLE advertiser payload.
     *  level: 0=OK, 1=GREEN, 2=ORANGE, 3=RED */
    @ReactMethod
    fun updateSeverity(level: Int) {
        val intent = Intent(reactContext, EmergencyBleService::class.java)
        intent.putExtra("severity", level)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            reactContext.startForegroundService(intent)
        } else {
            reactContext.startService(intent)
        }
    }

    /**
     * Set device role before calling startScanning().
     * role: "SENDER" | "RECEIVER" | "FULL"
     * SENDER   → hosts GATT server + advertises only (no scan, no GATT client)
     * RECEIVER → scans + connects as GATT client only (no advertise, no GATT server)
     * FULL     → both sides active (default)
     */
    @ReactMethod
    fun setDeviceRole(role: String) {
        val parsed = when (role.uppercase()) {
            "SENDER"   -> DeviceRole.SENDER
            "RECEIVER" -> DeviceRole.RECEIVER
            else       -> DeviceRole.FULL
        }
        BleChannels.role = parsed
        Log.i("RahatMeshModule", "DEVICE_ROLE_SET: ${parsed.name}")
    }

    @ReactMethod
    fun addListener(eventName: String) {
        // Keep: Required for RN built-in Event Emitter Calls
    }

    @ReactMethod
    fun removeListeners(count: Int) {
        // Keep: Required for RN built-in Event Emitter Calls
    }

    private fun startObservingPeers() {
        if (isObserving) return
        isObserving = true
        scope.launch {
            MeshRepository.nearbyPeers.collect { peers ->
                try {
                    val array = Arguments.createArray()
                    peers.forEach { p ->
                        val map = Arguments.createMap()
                        map.putString("id", p.rId)
                        map.putString("name", p.name)
                        map.putString("severity", p.severity)
                        map.putString("signalLevel", p.signalLevel.name)
                        map.putString("signalTrend", p.signalTrend.name)
                        map.putDouble("lastSeen", p.lastSeen.toDouble())
                        if (p.latitude != null && p.longitude != null) {
                            map.putDouble("latitude", p.latitude)
                            map.putDouble("longitude", p.longitude)
                        }
                        array.pushMap(map)
                    }
                    sendEvent("onPeersUpdated", array)
                } catch (e: Exception) {
                    Log.e("RahatMeshModule", "onPeersUpdated emit failed: ${e.message}")
                    // Don't rethrow — keeps the collection alive for next peer update
                }
            }
        }
    }

    fun sendEventToJS(frame: String) {
        Log.d("RahatMeshModule", "[BLE RX → JS] ${frame.take(80)}")
        sendEvent("onDataReceived", frame)
    }

    private fun sendEvent(eventName: String, params: Any?) {
        if (!reactContext.hasActiveReactInstance()) {
            Log.w("RahatMeshModule", "sendEvent($eventName) skipped — no active React instance")
            return
        }
        try {
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit(eventName, params)
        } catch (e: Exception) {
            Log.e("RahatMeshModule", "sendEvent($eventName) failed: ${e.message}")
        }
    }
}
