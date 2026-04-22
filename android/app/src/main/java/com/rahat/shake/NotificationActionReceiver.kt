package com.rahat.shake

import android.app.NotificationManager
import android.content.*
import com.rahat.MainActivity

/**
 * Handles YES / NO taps on the "Are you safe?" disaster notification.
 * Works whether the app is in foreground, background, or completely killed.
 *
 * Flow:
 *   1. Write action to SharedPreferences (durable across process death)
 *   2. Cancel the disaster notification
 *   3. Broadcast so ShakeModule can forward to JS immediately if app is alive
 *   4. Bring app to foreground so the user lands back in Rahat
 */
class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_YES   = "com.rahat.ACTION_YES"
        const val ACTION_NO    = "com.rahat.ACTION_NO"
        const val BCAST_ACTION = "com.rahat.NOTIFICATION_ACTION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences(
            ShakeDetectorService.PREFS_NAME, Context.MODE_PRIVATE
        )

        val value = when (intent.action) {
            ACTION_YES -> "YES"
            ACTION_NO  -> "NO"
            else       -> return
        }

        // 1. Persist action
        prefs.edit().putString(ShakeDetectorService.KEY_PENDING, value).apply()

        // 2. Dismiss disaster notification
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .cancel(ShakeModule.DISASTER_NOTIF_ID)

        // 3. Signal ShakeModule (works if app is alive in background)
        context.sendBroadcast(
            Intent(BCAST_ACTION).setPackage(context.packageName)
        )

        // 4. Open the app
        context.startActivity(
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        )
    }
}
