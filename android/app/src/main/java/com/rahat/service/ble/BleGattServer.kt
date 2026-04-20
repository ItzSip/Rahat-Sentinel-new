package com.rahat.service.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import java.util.UUID

/**
 * BleGattServer — receives event frames written by peer GATT clients.
 *
 * Design:
 *   • One primary service with one writable characteristic.
 *   • On write: decodes UTF-8, logs [BLE RX NATIVE], publishes via BleChannels.
 *   • Responds GATT_SUCCESS when the client requests a response.
 *   • No notifications / indications needed — write-only channel.
 *
 * Threading: BluetoothGattServerCallback fires on a binder thread; all operations
 * are stateless or thread-safe (BleChannels.publish is just a lambda call).
 */
class BleGattServer(private val context: Context) {

    private val TAG = "BLE_GATT_SERVER"

    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
        val CHAR_UUID:    UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
    }

    private val bluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private var gattServer: BluetoothGattServer? = null

    @SuppressLint("MissingPermission") // BLUETOOTH_CONNECT checked in EmergencyBleService
    fun start() {
        if (gattServer != null) return // already running

        val serverCallback = object : BluetoothGattServerCallback() {

            override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
                val label = if (newState == BluetoothProfile.STATE_CONNECTED) "CONNECTED" else "DISCONNECTED"
                Log.i(TAG, "GATT_SERVER $label: ${device.address.takeLast(5)}")
            }

            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray?
            ) {
                if (characteristic.uuid == CHAR_UUID && value != null && value.isNotEmpty()) {
                    val frame = String(value, Charsets.UTF_8)
                    Log.d(TAG, "[BLE RX NATIVE] ${frame.take(120)}")
                    BleChannels.publish(frame)
                }
                if (responseNeeded) {
                    gattServer?.sendResponse(
                        device, requestId,
                        BluetoothGatt.GATT_SUCCESS, 0, null
                    )
                }
            }
        }

        gattServer = bluetoothManager.openGattServer(context, serverCallback)

        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val characteristic = BluetoothGattCharacteristic(
            CHAR_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(characteristic)
        gattServer?.addService(service)

        Log.i(TAG, "GATT_SERVER_STARTED: listening for peer writes")
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        try {
            gattServer?.close()
        } catch (e: Exception) {
            Log.e(TAG, "GATT_SERVER_STOP_ERR: ${e.message}")
        }
        gattServer = null
        Log.i(TAG, "GATT_SERVER_STOPPED")
    }
}
