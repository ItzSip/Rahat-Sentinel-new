package com.rahat.service.ble

import android.util.Log

/**
 * DeviceRole — controls which half of the BLE stack is active.
 *
 *   SENDER   → GATT Server + Advertising only   (no scanning, no GATT client)
 *   RECEIVER → Scanning + GATT Client only       (no advertising, no GATT server)
 *   FULL     → Both sides active (default, original behaviour)
 */
enum class DeviceRole { SENDER, RECEIVER, FULL }

/**
 * BleChannels — decoupled pub/sub bridge between EmergencyBleService and RahatMeshModule.
 *
 * Follows the same singleton pattern as MeshRepository so the two components
 * can communicate without holding references to each other.
 *
 * Lifecycle:
 *   EmergencyBleService  sets [sender]           after BleGattClient is ready.
 *   RahatMeshModule      sets [onFrameReceived]   in startScanning().
 */
object BleChannels {

    /** Active role — set from JS via RahatMeshModule.setDeviceRole() before startScanning(). */
    @Volatile var role: DeviceRole = DeviceRole.FULL

    private const val TAG = "BleChannels"

    /**
     * Wired by EmergencyBleService → BleGattClient.sendToAll.
     * Called by RahatMeshModule.bleSend(frame).
     */
    var sender: ((String) -> Unit)? = null

    /**
     * Wired by RahatMeshModule → sendEventToJS.
     * Called by BleGattServer when a frame is received from a peer.
     */
    var onFrameReceived: ((String) -> Unit)? = null

    /** Route an outbound frame to the GATT client layer. */
    fun send(frame: String) {
        val fn = sender
        if (fn == null) {
            Log.w(TAG, "[BLE TX] No sender registered — GATT client not ready yet")
        } else {
            fn(frame)
        }
    }

    /** Deliver a received frame to the JS bridge. */
    fun publish(frame: String) {
        onFrameReceived?.invoke(frame)
            ?: Log.w(TAG, "[BLE RX] No JS listener registered — frame discarded")
    }
}
