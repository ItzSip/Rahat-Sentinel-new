package com.rahat.shake

import android.app.*
import android.content.*
import android.os.Build
import androidx.core.app.NotificationCompat
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.rahat.MainActivity

/**
 * Native module exposed to JS as "ShakeModule".
 *
 * JS API:
 *   startShakeService()           — start background foreground service
 *   stopShakeService()            — stop it
 *   postDisasterNotification()    — show "Are you safe?" in notification bar
 *   dismissDisasterNotification() — remove it (e.g. when disaster deactivated)
 *   consumePendingAction(cb)      — read + clear any SharedPrefs action (YES/NO/SHAKE)
 *
 * JS events emitted:
 *   onShakeDetected   — shake while app in foreground
 *   onDisasterAction  — YES / NO notification tap while app alive
 *
 * Background path (app killed / locked screen):
 *   Action is written to SharedPreferences; JS calls consumePendingAction()
 *   when it next comes to foreground to drain the queue.
 */
class ShakeModule(private val reactContext: ReactApplicationContext)
    : ReactContextBaseJavaModule(reactContext) {

    companion object {
        const val DISASTER_CHANNEL  = "rahat_disaster"
        const val DISASTER_NOTIF_ID = 2000
    }

    private val prefs by lazy {
        reactContext.getSharedPreferences(
            ShakeDetectorService.PREFS_NAME, Context.MODE_PRIVATE
        )
    }

    // ── Broadcast receivers (alive only while JS bridge is up) ────────────────

    private val shakeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Clear prefs immediately so consumePendingAction doesn't double-fire
            prefs.edit().remove(ShakeDetectorService.KEY_PENDING).apply()
            emit("onShakeDetected", null)
        }
    }

    private val actionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = prefs.getString(ShakeDetectorService.KEY_PENDING, null) ?: return
            prefs.edit().remove(ShakeDetectorService.KEY_PENDING).apply()
            emit("onDisasterAction", action)
        }
    }

    init {
        createDisasterChannel()
        registerReceiver(shakeReceiver, ShakeDetectorService.BCAST_SHAKE)
        registerReceiver(actionReceiver, NotificationActionReceiver.BCAST_ACTION)
    }

    override fun getName() = "ShakeModule"

    // ── Required stubs for NativeEventEmitter ─────────────────────────────────
    @ReactMethod fun addListener(eventName: String) {}
    @ReactMethod fun removeListeners(count: Int) {}

    // ── Service lifecycle ─────────────────────────────────────────────────────

    @ReactMethod
    fun startShakeService() {
        val intent = Intent(reactContext, ShakeDetectorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            reactContext.startForegroundService(intent)
        } else {
            reactContext.startService(intent)
        }
    }

    @ReactMethod
    fun stopShakeService() {
        reactContext.stopService(Intent(reactContext, ShakeDetectorService::class.java))
    }

    // ── Disaster notification ─────────────────────────────────────────────────

    @ReactMethod
    fun postDisasterNotification() {
        val nm = reactContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openPi = openAppIntent(0)

        val yesPi = PendingIntent.getBroadcast(
            reactContext, 10,
            Intent(NotificationActionReceiver.ACTION_YES)
                .setClass(reactContext, NotificationActionReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val noPi = PendingIntent.getBroadcast(
            reactContext, 11,
            Intent(NotificationActionReceiver.ACTION_NO)
                .setClass(reactContext, NotificationActionReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notif = NotificationCompat.Builder(reactContext, DISASTER_CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 Rahat — Disaster Alert")
            .setContentText("Are you safe?")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("A disaster simulation has been activated.\nAre you safe? Tap YES to confirm safety or NO to request help.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openPi)
            .addAction(0, "✓  YES — I am safe", yesPi)
            .addAction(0, "✕  NO — Need help",  noPi)
            .setAutoCancel(false)
            .setOngoing(false)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .build()

        nm.notify(DISASTER_NOTIF_ID, notif)
    }

    @ReactMethod
    fun dismissDisasterNotification() {
        (reactContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(DISASTER_NOTIF_ID)
    }

    // ── Pending action drain (called by JS on foreground resume) ──────────────

    @ReactMethod
    fun consumePendingAction(callback: Callback) {
        val action = prefs.getString(ShakeDetectorService.KEY_PENDING, null)
        if (action != null) {
            prefs.edit().remove(ShakeDetectorService.KEY_PENDING).apply()
            callback.invoke(action)
        } else {
            callback.invoke(null as String?)
        }
    }

    // ── Teardown ──────────────────────────────────────────────────────────────

    override fun onCatalystInstanceDestroy() {
        try {
            reactContext.unregisterReceiver(shakeReceiver)
            reactContext.unregisterReceiver(actionReceiver)
        } catch (_: Exception) {}
        super.onCatalystInstanceDestroy()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun emit(event: String, data: Any?) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(event, data)
    }

    private fun openAppIntent(requestCode: Int): PendingIntent =
        PendingIntent.getActivity(
            reactContext, requestCode,
            Intent(reactContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )

    private fun registerReceiver(receiver: BroadcastReceiver, action: String) {
        val filter = IntentFilter(action)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            reactContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            reactContext.registerReceiver(receiver, filter)
        }
    }

    private fun createDisasterChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                DISASTER_CHANNEL,
                "Rahat Disaster Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical safety check notifications"
                enableVibration(true)
                enableLights(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            (reactContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(ch)
        }
    }
}
