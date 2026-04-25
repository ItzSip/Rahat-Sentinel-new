package com.rahat.service.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * BleAdvertiser — Range-optimised with hardware-safe fallback
 *
 * Strategy (tried in order):
 *   1. LE Coded PHY (S=8), non-legacy — 4x range  [if hardware supports it AND start succeeds]
 *   2. Legacy 1M PHY at TX_POWER_MAX              [guaranteed fallback]
 *
 * Payload layout (25 bytes):
 *   [0-15] EphID (16B) | [16] Status (1B) | [17-20] Lat float LE | [21-24] Lng float LE
 */
class BleAdvertiser(private val context: Context) {

    private val TAG = "BLE_ADVERTISER"
    private val MANUFACTURER_ID = 0xFFFF

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }
    private val bleAdvertiser: BluetoothLeAdvertiser? get() = bluetoothAdapter?.bluetoothLeAdvertiser

    private var currentAdvertisingSet: AdvertisingSet? = null
    var isAdvertising = false
        private set
    private var lastPayload: ByteArray? = null
    private var startTime = 0L

    private val callback = object : AdvertisingSetCallback() {
        override fun onAdvertisingSetStarted(set: AdvertisingSet?, txPower: Int, status: Int) {
            if (status == ADVERTISE_SUCCESS && set != null) {
                startTime = System.currentTimeMillis()
                currentAdvertisingSet = set
                isAdvertising = true
                Log.i(TAG, "BLE_ADVERTISER_STARTED: TX=${txPower}dBm (legacy 1M, MAX RANGE)")
            } else {
                isAdvertising = false
                currentAdvertisingSet = null
                Log.e(TAG, "BLE_ADVERTISER_START_FAILED: status=$status")
            }
        }

        override fun onAdvertisingSetStopped(set: AdvertisingSet?) {
            Log.w(TAG, "BLE_ADVERTISER_STOPPED after ${(System.currentTimeMillis()-startTime)/1000}s")
            isAdvertising = false
            currentAdvertisingSet = null
        }

        override fun onAdvertisingDataSet(set: AdvertisingSet?, status: Int) {
            if (status == ADVERTISE_SUCCESS) {
                Log.d(TAG, "BLE_PAYLOAD_UPDATED: OK")
            } else {
                Log.e(TAG, "BLE_PAYLOAD_UPDATE_FAILED: status=$status")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startOrUpdateAdvertising(ephemeralId: String, severity: Int, lat: Double, lng: Double) {
        val payload = buildPayload(ephemeralId, severity, lat, lng)

        // Reset if not advertising (e.g. after BT restart)
        if (!isAdvertising) lastPayload = null

        if (lastPayload?.contentEquals(payload) == true && isAdvertising) return
        lastPayload = payload

        // If already running — zero-downtime payload update
        val currentSet = currentAdvertisingSet
        if (currentSet != null && isAdvertising) {
            currentSet.setAdvertisingData(buildAdvertiseData(payload))
            Log.d(TAG, "BLE_PAYLOAD_ROTATED: GPS=${"%.4f".format(lat)},${"%.4f".format(lng)}")
            return
        }

        // Start fresh — always legacy 1M for maximum compatibility
        // Coded PHY extended advertising blocks scanners from reading manufacturer data
        startLegacyAdvertising(ephemeralId, severity, lat, lng)
    }

    @SuppressLint("MissingPermission")
    private fun startLegacyAdvertising(ephemeralId: String, severity: Int, lat: Double, lng: Double) {
        val advertiser = bleAdvertiser ?: return
        val payload = buildPayload(ephemeralId, severity, lat, lng)
        Log.i(TAG, "BLE_ADVERTISER_STARTING: Legacy 1M MAX_POWER (ephId=${ephemeralId.take(8)})")
        try {
            val params = AdvertisingSetParameters.Builder()
                .setInterval(AdvertisingSetParameters.INTERVAL_MIN)
                .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MAX)
                .setConnectable(true)  // required for GATT data channel
                .setScannable(true)    // legacy+connectable requires scannable (ADV_IND PDU)
                .setLegacyMode(true)
                .setPrimaryPhy(BluetoothDevice.PHY_LE_1M)
                .setSecondaryPhy(BluetoothDevice.PHY_LE_1M)
                .build()
            advertiser.startAdvertisingSet(params, buildAdvertiseData(payload), null, null, null, callback)
        } catch (e: Exception) {
            Log.e(TAG, "BLE_ADVERTISER_LEGACY_FAIL: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        try {
            bleAdvertiser?.stopAdvertisingSet(callback)
        } catch (e: Exception) {
            Log.e(TAG, "BLE_STOP_ERR: ${e.message}")
        }
    }

    private fun buildAdvertiseData(payload: ByteArray): AdvertiseData =
        AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addManufacturerData(MANUFACTURER_ID, payload)
            .build()

    /**
     * 23-byte payload: [0-15] EphID | [16] Status | [17-19] Lat int24 LE | [20-22] Lng int24 LE
     *
     * Lat/Lng encoded as signed 24-bit integers × 10000 (≈11m precision).
     * Total AD frame: 3 (flags) + 4 (mfr header) + 23 = 30 bytes ≤ 31-byte legacy limit.
     */
    private fun buildPayload(ephId: String, severity: Int, lat: Double, lng: Double): ByteArray {
        val buf = ByteBuffer.allocate(23).order(ByteOrder.LITTLE_ENDIAN)
        try {
            val idBytes = ByteArray(16)
            for (i in 0 until 16) idBytes[i] = ephId.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            buf.put(idBytes)
            buf.put(severity.coerceIn(0, 3).toByte()) // 0=OK,1=GREEN,2=ORANGE,3=RED
            // Encode lat/lng as 3-byte little-endian signed integers (×10000)
            val latInt = (lat * 10000).toInt().coerceIn(-900000, 900000)
            val lngInt = (lng * 10000).toInt().coerceIn(-1800000, 1800000)
            buf.put(latInt.toByte()); buf.put((latInt shr 8).toByte()); buf.put((latInt shr 16).toByte())
            buf.put(lngInt.toByte()); buf.put((lngInt shr 8).toByte()); buf.put((lngInt shr 16).toByte())
            Log.v(TAG, "BLE_PAYLOAD_BUILT: lat=$lat lng=$lng severity=$severity")
        } catch (e: Exception) {
            Log.e(TAG, "BLE_PAYLOAD_ERR: $e")
        }
        return buf.array()
    }
}
