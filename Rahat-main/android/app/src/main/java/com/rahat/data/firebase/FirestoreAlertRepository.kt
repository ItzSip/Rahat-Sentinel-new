package com.rahat.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.rahat.data.model.Alert
import com.rahat.data.model.AlertEventType
import com.rahat.data.model.AlertSeverity
import com.rahat.data.model.AlertStatus
import kotlinx.coroutines.tasks.await

data class FirestoreAlert(
    val type: String = "",
    val severity: String = "",
    val location: Map<String, Double> = mapOf(),
    val radius: Int = 2000,
    val triggeredBy: String = "",
    val createdAt: com.google.firebase.Timestamp? = null,
    val active: Boolean = true,
    // NEW AI prediction fields
    val district: String = "",
    val eventType: String = "",
    val confidence: Double = 0.0,
    val timeToEventHours: Int = 0
)

class FirestoreAlertRepository {

    private val db               = FirebaseFirestore.getInstance()
    private val alertsCollection = db.collection("alerts")

    // ── Write ──────────────────────────────────────────────────────────────

    suspend fun createAlert(
        type: String,
        severity: String,
        lat: Double,
        lng: Double,
        triggeredBy: String,
        radius: Int = 2000,
        district: String = "",
        confidence: Float = 0f,
        timeToEventHours: Int = 0
    ): String {
        val alert = hashMapOf(
            "type"             to type,
            "severity"         to severity,
            "location"         to hashMapOf("lat" to lat, "lng" to lng),
            "radius"           to radius,
            "triggeredBy"      to triggeredBy,
            "createdAt"        to com.google.firebase.Timestamp.now(),
            "active"           to true,
            "district"         to district,
            "eventType"        to type,
            "confidence"       to confidence.toDouble(),
            "timeToEventHours" to timeToEventHours
        )
        val docRef = alertsCollection.add(alert).await()
        return docRef.id
    }

    // ── Read ───────────────────────────────────────────────────────────────

    suspend fun getActiveAlerts(): List<FirestoreAlert> {
        val snapshot = alertsCollection
            .whereEqualTo("active", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toObject(FirestoreAlert::class.java) }
    }

    /**
     * Convert Firestore alerts → local Alert model for use in UI.
     */
    suspend fun getActiveAlertsAsModel(): List<Alert> {
        return getActiveAlerts().mapIndexed { i, fa ->
            val sev = try {
                AlertSeverity.valueOf(fa.severity.uppercase())
            } catch (e: Exception) { AlertSeverity.MEDIUM }

            val eventEnum = when (fa.eventType.uppercase()) {
                "FLOOD", "FLASH FLOOD" -> AlertEventType.FLOOD
                "LANDSLIDE"            -> AlertEventType.LANDSLIDE
                "HEATWAVE"             -> AlertEventType.HEATWAVE
                "CYCLONE"              -> AlertEventType.CYCLONE
                "EARTHQUAKE"           -> AlertEventType.EARTHQUAKE
                else                   -> AlertEventType.UNKNOWN
            }

            val status = when (sev) {
                AlertSeverity.CRITICAL -> AlertStatus.CRITICAL
                AlertSeverity.HIGH     -> AlertStatus.WARNING
                else                   -> AlertStatus.WATCH
            }

            Alert(
                id               = "fs_$i",
                lat              = fa.location["lat"] ?: 0.0,
                lon              = fa.location["lng"] ?: 0.0,
                severity         = sev,
                message          = "${fa.type} detected in ${fa.district}",
                timestamp        = fa.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
                district         = fa.district,
                eventType        = fa.eventType,
                alertEventType   = eventEnum,
                confidence       = fa.confidence.toFloat(),
                timeToEventHours = fa.timeToEventHours.takeIf { it > 0 },
                status           = status
            )
        }
    }

    // ── Delete ─────────────────────────────────────────────────────────────

    suspend fun deactivateAlert(alertId: String) {
        alertsCollection.document(alertId).update("active", false).await()
    }
}