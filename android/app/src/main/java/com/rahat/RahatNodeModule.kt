package com.rahat

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.io.IOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.Executors

/**
 * RahatNodeModule — JS-callable native module.
 *
 * JS API:
 *   connect(deviceName)               → Promise<string>  paired BT device whose name contains deviceName
 *   disconnect()                       → void
 *   sendLocation(lat, lon, severity)  → Promise<string>  sends "lat,lon,severity" over BT + HTTP POST
 *
 * JS events:
 *   onNodeConnected    (deviceName: string)
 *   onNodeDisconnected ()
 */
class RahatNodeModule(private val ctx: ReactApplicationContext)
    : ReactContextBaseJavaModule(ctx) {

    companion object {
        private const val TAG      = "RahatNode"
        private val   SPP_UUID     = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val NODE_URL = "http://192.168.4.1/location"
        private const val TIMEOUT  = 3_000
    }

    // Single background thread — all BT + HTTP work runs here to avoid blocking JS bridge
    private val executor = Executors.newSingleThreadExecutor()

    @Volatile private var socket: BluetoothSocket? = null
    @Volatile private var out: OutputStream?       = null

    override fun getName() = "RahatNodeModule"

    // Required stubs so JS NativeEventEmitter doesn't warn
    @ReactMethod fun addListener(eventName: String) {}
    @ReactMethod fun removeListeners(count: Int) {}

    // ── connect ───────────────────────────────────────────────────────────────

    @ReactMethod
    fun connect(deviceName: String, promise: Promise) {
        executor.execute {
            try {
                val adapter = BluetoothAdapter.getDefaultAdapter()
                if (adapter == null) { promise.reject("NO_BT", "Bluetooth not available"); return@execute }

                val device = adapter.bondedDevices?.firstOrNull {
                    it.name?.contains(deviceName, ignoreCase = true) == true
                }
                if (device == null) { promise.reject("NOT_FOUND", "No paired device matching '$deviceName'"); return@execute }

                closeSilently()
                adapter.cancelDiscovery()

                // createRfcommSocketToServiceRecord fails with ESP32 BluetoothSerial
                // because SDP lookup times out. Reflection on channel 1 is reliable.
                val s = openRfcommSocket(device)
                s.connect()
                socket = s
                out    = s.outputStream

                Log.i(TAG, "Connected to ${device.name}")
                emit("onNodeConnected", device.name)
                promise.resolve(device.name)
            } catch (e: Exception) {
                Log.e(TAG, "connect failed: ${e.message}")
                closeSilently()
                promise.reject("CONNECT_FAIL", e.message ?: "Unknown error")
            }
        }
    }

    // ── disconnect ────────────────────────────────────────────────────────────

    @ReactMethod
    fun disconnect() { executor.execute { closeSilently() } }

    // ── sendLocation ──────────────────────────────────────────────────────────

    @ReactMethod
    fun sendLocation(lat: Double, lon: Double, severity: String, promise: Promise) {
        val payload = "%.6f,%.6f,%s".format(lat, lon, severity)

        executor.execute {
            val btOk   = sendBluetooth(payload)
            val httpOk = sendHttp(payload)
            promise.resolve("bt=$btOk http=$httpOk")
        }
    }

    // ── internals ─────────────────────────────────────────────────────────────

    private fun sendBluetooth(payload: String): Boolean {
        val stream = out ?: return false
        return try {
            stream.write((payload + "\n").toByteArray(Charsets.UTF_8))
            stream.flush()
            Log.d(TAG, "[BT] sent: $payload")
            true
        } catch (e: IOException) {
            Log.w(TAG, "[BT] send failed — dropping connection: ${e.message}")
            closeSilently()
            emit("onNodeDisconnected", null)
            false
        }
    }

    private fun sendHttp(payload: String): Boolean {
        return try {
            val conn = URL(NODE_URL).openConnection() as HttpURLConnection
            conn.requestMethod  = "POST"
            conn.connectTimeout = TIMEOUT
            conn.readTimeout    = TIMEOUT
            conn.doOutput       = true
            conn.setRequestProperty("Content-Type", "text/plain")
            conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            conn.disconnect()
            Log.d(TAG, "[HTTP] response $code")
            code == 200
        } catch (e: Exception) {
            Log.w(TAG, "[HTTP] failed: ${e.message}")
            false
        }
    }

    // ESP32 BluetoothSerial uses RFCOMM channel 1.
    // createRfcommSocketToServiceRecord() does an SDP lookup which times out on ESP32.
    // Reflection method bypasses SDP and directly opens channel 1 — always works.
    private fun openRfcommSocket(device: android.bluetooth.BluetoothDevice): BluetoothSocket {
        return try {
            val method = device.javaClass.getMethod("createRfcommSocket", Int::class.java)
            method.invoke(device, 1) as BluetoothSocket
        } catch (e: Exception) {
            Log.w(TAG, "Reflection socket failed, falling back to insecure SPP: ${e.message}")
            device.createInsecureRfcommSocketToServiceRecord(SPP_UUID)
        }
    }

    private fun closeSilently() {
        try { out?.close()    } catch (_: Exception) {}
        try { socket?.close() } catch (_: Exception) {}
        out    = null
        socket = null
    }

    private fun emit(event: String, data: Any?) {
        ctx.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(event, data)
    }

    override fun onCatalystInstanceDestroy() {
        closeSilently()
        executor.shutdown()
        super.onCatalystInstanceDestroy()
    }
}
