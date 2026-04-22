package com.rahat.shake

import android.app.*
import android.content.*
import android.hardware.*
import android.os.*
import androidx.core.app.NotificationCompat
import com.rahat.MainActivity

class ShakeDetectorService : Service(), SensorEventListener {

    companion object {
        const val CHANNEL_ID       = "rahat_shake_monitor"
        const val NOTIF_ID         = 2001
        const val PREFS_NAME       = "rahat_disaster_prefs"
        const val KEY_PENDING      = "pending_action"
        const val VAL_SHAKE        = "SHAKE"
        const val BCAST_SHAKE      = "com.rahat.SHAKE_DETECTED"

        private const val SHAKE_THRESHOLD = 20f   // m/s² total vector magnitude
        private const val COOLDOWN_MS     = 1500L  // min ms between registered shakes
    }

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var prefs: SharedPreferences
    private var lastShakeAt = 0L

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        sensorManager  = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer  = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        prefs          = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildPersistentNotification())
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        return START_NOT_STICKY   // do NOT auto-restart; JS re-starts it when app resumes
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Sensor ────────────────────────────────────────────────────────────────

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        val mag = Math.sqrt(
            (event.values[0] * event.values[0] +
             event.values[1] * event.values[1] +
             event.values[2] * event.values[2]).toDouble()
        ).toFloat()

        if (mag > SHAKE_THRESHOLD) {
            val now = System.currentTimeMillis()
            if (now - lastShakeAt > COOLDOWN_MS) {
                lastShakeAt = now
                onShakeDetected()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // ── Shake event ───────────────────────────────────────────────────────────

    private fun onShakeDetected() {
        // 1. Persist for background case (JS picks this up on next foreground)
        prefs.edit().putString(KEY_PENDING, VAL_SHAKE).apply()

        // 2. Broadcast for ShakeModule to forward to JS when app is alive
        sendBroadcast(Intent(BCAST_SHAKE).setPackage(packageName))
    }

    // ── Notification helpers ──────────────────────────────────────────────────

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager.IMPORTANCE_LOW.let { imp ->
                val ch = NotificationChannel(CHANNEL_ID, "Rahat Shake Monitor", imp).apply {
                    description = "Background shake detection for disaster simulation"
                    setShowBadge(false)
                }
                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(ch)
            }
        }
    }

    private fun buildPersistentNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Rahat — Disaster Monitoring")
            .setContentText("Shake detection active. Shake phone to confirm safety.")
            .setContentIntent(openIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}
