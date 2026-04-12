package com.rahat.ui.sos

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.rahat.service.Narrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.rahat.data.local.entity.EmergencyContactEntity

import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log

enum class SosState {
    IDLE, COUNTDOWN, SENDING, SENT
}

class SosManager(private val context: Context, private val narrator: Narrator) {
    private val _sosState = MutableStateFlow(SosState.IDLE)
    val sosState: StateFlow<SosState> = _sosState.asStateFlow()

    private val _countdown = MutableStateFlow(5)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)
    private var countdownJob: kotlinx.coroutines.Job? = null

    fun startCountdown(isNarratorEnabled: Boolean, volume: Float, userName: String, coords: String) {
        if (_sosState.value != SosState.IDLE) {
            Log.d("SosManager", "Cannot start countdown: State is ${_sosState.value}")
            return
        }
        Log.d("SosManager", "Starting countdown for $userName at $coords")
        _sosState.value = SosState.COUNTDOWN
        _countdown.value = 5

        countdownJob = scope.launch {
            while (_countdown.value > 0) {
                Log.d("SosManager", "Countdown: ${_countdown.value}")
                if (isNarratorEnabled) {
                    narrator.speak(_countdown.value.toString(), volume)
                }
                delay(1000)
                _countdown.value -= 1
            }
            triggerSos(userName, coords)
        }
    }

    fun cancelCountdown() {
        if (_sosState.value == SosState.COUNTDOWN) {
            Log.d("SosManager", "Cancelling countdown")
            countdownJob?.cancel()
            _sosState.value = SosState.IDLE
            narrator.speak("SOS cancelled")
        }
    }

    private fun sendSmsToContacts(message: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val db = com.rahat.data.local.RahatDatabase.getDatabase(context)
                val device = db.rahatDao().getDeviceOneShot()
                if (device == null) return@launch

                val contactList: List<EmergencyContactEntity> = db.rahatDao().getContacts(device.rId).first()

                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                contactList.forEach { contact ->
                    smsManager.sendTextMessage(contact.phone, null, message, null, null)
                    Log.d("SosManager", "SMS sent to ${contact.phone}")
                }

            } catch (e: Exception) {
                Log.e("SosManager", "Failed to send SMS: ${e.message}")
            }
        }
    }

    private fun triggerSos(userName: String, coords: String) {
        Log.d("SosManager", "Triggering SOS now!")
        _sosState.value = SosState.SENDING
        vibrate()

        scope.launch {
            val rawParts = coords.replace("Lat:", "").replace("Lng:", "").replace(" ", "")
            val mapLink = "https://www.google.com/maps/search/?api=1&query=$rawParts"
            val message = "SOS! $userName need HELP! Loc: $coords. Map: $mapLink"

            sendSmsToContacts(message)
            predialEmergencyNumber("112")

            delay(2000)
            _sosState.value = SosState.SENT
            narrator.speak("SOS sent. Emergency contacts notified.", 1.0f)
        }
    }

    private fun predialEmergencyNumber(number: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$number")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("SosManager", "Failed to predial: ${e.message}")
        }
    }

    fun reset() {
        _sosState.value = SosState.IDLE
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(500)
        }
    }
}