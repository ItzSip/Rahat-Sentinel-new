package com.rahat.service.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * BleGattClient — fire-and-forget GATT writer for outbound event frames.
 *
 * Design constraints (from spec):
 *   • Max 5 simultaneous peer connections
 *   • No retries, no queues, no ACKs
 *   • Payload: UTF-8 string, max ~200 bytes
 *   • Connect when a peer is discovered by BleScanner; disconnect on BT off/stop
 *
 * Threading:
 *   connectToPeer / sendToAll may be called from any thread.
 *   ConcurrentHashMap guards the connections map.
 *   BluetoothGattCallback fires on a binder thread.
 */
@SuppressLint("MissingPermission") // BLUETOOTH_CONNECT checked in EmergencyBleService
class BleGattClient(private val context: Context) {

    private val TAG = "BLE_GATT_CLIENT"
    private val MAX_PEERS = 5

    // mac → active BluetoothGatt (services discovered and ready)
    private val connections = ConcurrentHashMap<String, BluetoothGatt>()
    // mac → in-progress connecting gatt (not yet ready for writes)
    private val connecting  = ConcurrentHashMap<String, BluetoothGatt>()

    // ── Connect ────────────────────────────────────────────────────────────────

    fun connectToPeer(device: BluetoothDevice) {
        val mac = device.address
        if (connections.containsKey(mac) || connecting.containsKey(mac)) return
        if (connections.size + connecting.size >= MAX_PEERS) {
            Log.d(TAG, "GATT_CONNECT_SKIP: max peers ($MAX_PEERS) reached")
            return
        }
        Log.i(TAG, "GATT_CONNECT → $mac")

        val gatt = device.connectGatt(context, false, buildCallback(mac), BluetoothDevice.TRANSPORT_LE)
        connecting[mac] = gatt
    }

    private fun buildCallback(mac: String) = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "GATT_CONNECTED: $mac")
                    Log.i(TAG, "[BLE CONNECTION STATE] device=${mac.takeLast(5)} state=CONNECTED")
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "GATT_DISCONNECTED: $mac (status=$status)")
                    Log.i(TAG, "[BLE CONNECTION STATE] device=${mac.takeLast(5)} state=DISCONNECTED (status=$status)")
                    connections.remove(mac)
                    connecting.remove(mac)
                    gatt.close()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            connecting.remove(mac)
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "GATT_DISCOVER_FAIL: $mac status=$status")
                gatt.disconnect()
                return
            }
            // Only keep the connection if the peer actually has our service
            val service = gatt.getService(BleGattServer.SERVICE_UUID)
            if (service == null) {
                Log.d(TAG, "GATT_NO_RAHAT_SERVICE: $mac — disconnecting")
                gatt.disconnect()
                return
            }
            connections[mac] = gatt
            Log.i(TAG, "GATT_READY: $mac (${connections.size} total peers)")
            // Deliver any pending disaster frame to this newly-connected peer
            BleChannels.pendingDisasterFrame?.let { frame ->
                Log.d(TAG, "GATT_READY_DISASTER_REPLAY → $mac")
                writeFrame(gatt, frame)
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            val ok = status == BluetoothGatt.GATT_SUCCESS
            Log.d(TAG, "GATT_WRITE ${if (ok) "OK" else "FAIL(status=$status)"}: ${mac.takeLast(5)}")
        }
    }

    // ── Send ───────────────────────────────────────────────────────────────────

    fun sendToAll(frame: String) {
        Log.d(TAG, "[BLE CONNECTED DEVICES COUNT]: ${connections.size}")
        if (connections.isEmpty()) {
            Log.w(TAG, "[BLE WARNING] No connected devices, skipping send")
            return
        }
        Log.d(TAG, "[BLE TX NATIVE] → ${connections.size} peer(s) | ${frame.take(80)}")
        connections.forEach { (_, gatt) -> writeFrame(gatt, frame) }
    }

    private fun writeFrame(gatt: BluetoothGatt, frame: String) {
        val service = gatt.getService(BleGattServer.SERVICE_UUID) ?: run {
            Log.w(TAG, "GATT_WRITE: service gone on ${gatt.device.address.takeLast(5)}")
            return
        }
        val char = service.getCharacteristic(BleGattServer.CHAR_UUID) ?: run {
            Log.w(TAG, "GATT_WRITE: characteristic gone on ${gatt.device.address.takeLast(5)}")
            return
        }
        val bytes = frame.toByteArray(Charsets.UTF_8)

        // API 33+ uses a new non-deprecated overload; fall back for older devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(char, bytes, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
        } else {
            @Suppress("DEPRECATION")
            char.value = bytes
            @Suppress("DEPRECATION")
            char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            @Suppress("DEPRECATION")
            gatt.writeCharacteristic(char)
        }
    }

    // ── Cleanup ────────────────────────────────────────────────────────────────

    fun stop() {
        (connections.values + connecting.values).forEach {
            try { it.disconnect(); it.close() } catch (_: Exception) {}
        }
        connections.clear()
        connecting.clear()
        Log.i(TAG, "GATT_CLIENT_STOPPED")
    }
}
