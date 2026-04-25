package com.rahat.service.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.*

/**
 * BleScanner — Range-optimised version
 *
 * RANGE OPTIMISATIONS:
 * 1. SCAN_MODE_LOW_LATENCY — continuous scanning, no duty cycle
 * 2. Reports duty-cycle scan (10s on / 3s off) to avoid Android OS scan throttle (30s rule)
 * 3. Tries both 1M and Coded PHY scanning when hardware supports it
 * 4. Parses lat/lng from 25-byte extended payload
 *
 * Android throttles BLE scans that run > 30s continuously — we cycle 10s/3s to stay
 * under the threshold and avoid SCAN_FAILED_INTERNAL_ERROR (error code 2).
 */
class BleScanner(private val context: Context, private val peerManager: PeerManager) {

    private val TAG = "BLE_SCANNER"
    private val MANUFACTURER_ID = 0xFFFF

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }
    private val scanner: BluetoothLeScanner? by lazy { bluetoothAdapter?.bluetoothLeScanner }

    private var scanJob: Job? = null
    private var scanCallback: ScanCallback? = null

    /**
     * Called whenever a confirmed Rahat peer is discovered.
     * Wired by EmergencyBleService → BleGattClient.connectToPeer so the
     * GATT data channel can be established alongside peer-discovery.
     */
    var onDeviceFound: ((BluetoothDevice) -> Unit)? = null

    // Cycle just under the 30s Android throttle threshold
    private val SCAN_DURATION_MS = 10_000L
    private val SCAN_REST_MS     =  3_000L

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (scanJob?.isActive == true) return

        val adapterLocal = bluetoothAdapter
        if (adapterLocal == null || !adapterLocal.isEnabled) {
            Log.e(TAG, "BLE_SCAN_ABORTED: Bluetooth unavailable")
            return
        }
        val scannerLocal = scanner ?: run {
            Log.e(TAG, "BLE_SCAN_FAILED: No BluetoothLeScanner")
            return
        }

        scanJob = peerManager.getScope().launch {
            Log.i(TAG, "BLE_SCAN_JOB_STARTED")
            while (isActive) {
                try {
                    // Always scan in legacy mode. Extended scanning (setLegacy=false) with
                    // a manufacturer-data ScanFilter silently drops legacy advertising packets
                    // on many Android devices — the filter only matches Extended Advertising Data.
                    // Our advertiser uses legacy mode (ADV_IND PDU) for the same compatibility
                    // reason, so the scanner must match.
                    val settings = ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .setLegacy(true)
                        .build()
                    Log.i(TAG, "BLE_SCAN_MODE: Legacy 1M PHY (manufacturer-filter compatible)")

                    // Filter: manufacturer ID with empty mask (match any payload)
                    val emptyData = byteArrayOf()
                    val filters = listOf(
                        ScanFilter.Builder()
                            .setManufacturerData(MANUFACTURER_ID, emptyData, emptyData)
                            .build()
                    )

                    val cb = createScanCallback()
                    scanCallback = cb

                    Log.i(TAG, "BLE_SCAN_WINDOW_OPEN: ${SCAN_DURATION_MS/1000}s")
                    scannerLocal.startScan(filters, settings, cb)

                    delay(SCAN_DURATION_MS)

                    scannerLocal.stopScan(cb)
                    scanCallback = null
                    Log.d(TAG, "BLE_SCAN_WINDOW_CLOSED: rest ${SCAN_REST_MS/1000}s")

                    delay(SCAN_REST_MS)
                } catch (e: Exception) {
                    Log.e(TAG, "BLE_SCAN_LOOP_ERR: ${e.message}")
                    delay(5000)
                }
            }
        }
    }

    private fun createScanCallback() = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val record = result.scanRecord ?: return
            val mfData = record.getManufacturerSpecificData(MANUFACTURER_ID)
            if (mfData != null) {
                processPayload(result.device.address, mfData, result.rssi)
                // Trigger GATT connection so the bidirectional data channel opens
                onDeviceFound?.invoke(result.device)
            } else {
                Log.v(TAG, "BLE_OTHER: MAC=${result.device.address} RSSI=${result.rssi}")
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach { onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, it) }
        }

        override fun onScanFailed(errorCode: Int) {
            val reason = when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED              -> "ALREADY_STARTED (safe to ignore)"
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "APP_REGISTRATION_FAILED"
                SCAN_FAILED_FEATURE_UNSUPPORTED          -> "FEATURE_UNSUPPORTED"
                SCAN_FAILED_INTERNAL_ERROR               -> "INTERNAL_ERROR (OS throttled?)"
                else -> "CODE_$errorCode"
            }
            Log.e(TAG, "BLE_SCAN_FAILED: $reason")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        try {
            scanJob?.cancel()
            scanJob = null
            scanCallback?.let { scanner?.stopScan(it) }
            scanCallback = null
            Log.w(TAG, "BLE_SCAN_STOPPED")
        } catch (e: Exception) {
            Log.e(TAG, "BLE_SCAN_STOP_ERR: ${e.message}")
        }
    }

    /**
     * Parse 23-byte payload: [0-15] EphID | [16] Status | [17-19] Lat int24 LE | [20-22] Lng int24 LE
     * Also handles legacy 17-byte payload (no GPS) for backward compat.
     */
    private fun processPayload(mac: String, data: ByteArray, rssi: Int) {
        if (data.size < 17) {
            Log.w(TAG, "BLE_SKIP: Too short (${data.size}B) from $mac")
            return
        }
        try {
            val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
            val idBytes = ByteArray(16)
            buffer.get(idBytes)
            val ephId = idBytes.joinToString("") { "%02x".format(it) }

            val status   = buffer.get().toInt()
            val severity = if (status == 1) 2 else 1

            // Parse GPS if payload is extended (23+ bytes) — lat/lng as signed int24 LE × 10000
            var lat: Double? = null
            var lng: Double? = null
            if (data.size >= 23) {
                val b0 = buffer.get().toInt() and 0xFF
                val b1 = buffer.get().toInt() and 0xFF
                val b2 = buffer.get().toInt() and 0xFF
                val latInt = if (b2 and 0x80 != 0) (0xFF shl 24) or (b2 shl 16) or (b1 shl 8) or b0
                             else (b2 shl 16) or (b1 shl 8) or b0
                val b3 = buffer.get().toInt() and 0xFF
                val b4 = buffer.get().toInt() and 0xFF
                val b5 = buffer.get().toInt() and 0xFF
                val lngInt = if (b5 and 0x80 != 0) (0xFF shl 24) or (b5 shl 16) or (b4 shl 8) or b3
                             else (b5 shl 16) or (b4 shl 8) or b3
                val rawLat = latInt / 10000.0
                val rawLng = lngInt / 10000.0
                if (rawLat in -90.0..90.0 && rawLng in -180.0..180.0 && rawLat != 0.0) {
                    lat = rawLat
                    lng = rawLng
                }
            }

            Log.i(TAG, "BLE_RAHAT_PEER: MAC=$mac EphID=${ephId.take(8)} " +
                "Status=${if (status == 1) "SOS" else "OK"} RSSI=$rssi " +
                "GPS=${if (lat != null) "${"%.4f".format(lat)},${"%.4f".format(lng)}" else "none"}")

            peerManager.onRawPeerDiscovery(mac, ephId, severity, false, rssi, lat, lng)

        } catch (e: Exception) {
            Log.e(TAG, "BLE_PARSE_ERR: $mac | ${e.message}")
        }
    }
}
