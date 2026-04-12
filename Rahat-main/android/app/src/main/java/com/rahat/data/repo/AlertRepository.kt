package com.rahat.data.repo

import com.rahat.data.model.Alert
import com.rahat.data.model.AlertEventType
import com.rahat.data.model.AlertSeverity
import com.rahat.data.model.AlertStatus
import kotlin.random.Random

class AlertRepository {

    private val _alerts = mutableListOf<Alert>()

    fun getAlerts(): List<Alert> = _alerts.toList()

    /**
     * Generates realistic demo seeds near the user's location.
     * These simulate AI prediction output for UI testing.
     * Replace with real backend feed when prediction API is live.
     */
    fun generateDemoSeeds(centerLat: Double, centerLon: Double) {
        if (_alerts.isNotEmpty()) return  // Only seed once per session

        val seeds = listOf(
            Alert(
                id               = "demo_001",
                lat              = centerLat + 0.002,
                lon              = centerLon + 0.003,
                severity         = AlertSeverity.CRITICAL,
                message          = "Flash flood risk detected. Rising rainfall anomaly and " +
                        "terrain instability in the eastern valley corridor.",
                timestamp        = System.currentTimeMillis(),
                district         = "Chamoli",
                eventType        = "Flash Flood",
                alertEventType   = AlertEventType.FLOOD,
                confidence       = 0.86f,
                timeToEventHours = 36,
                status           = AlertStatus.CRITICAL
            ),
            Alert(
                id               = "demo_002",
                lat              = centerLat - 0.003,
                lon              = centerLon + 0.001,
                severity         = AlertSeverity.HIGH,
                message          = "Landslide risk detected. Historical pattern match with " +
                        "current moisture levels and slope gradient.",
                timestamp        = System.currentTimeMillis() - 3_600_000,
                district         = "Uttarakhand",
                eventType        = "Landslide",
                alertEventType   = AlertEventType.LANDSLIDE,
                confidence       = 0.82f,
                timeToEventHours = 48,
                status           = AlertStatus.WARNING
            ),
            Alert(
                id               = "demo_003",
                lat              = centerLat + 0.005,
                lon              = centerLon - 0.002,
                severity         = AlertSeverity.MEDIUM,
                message          = "Heat stress advisory. Temperatures projected above 44°C " +
                        "for the next 72 hours. Stay hydrated.",
                timestamp        = System.currentTimeMillis() - 7_200_000,
                district         = "Indore",
                eventType        = "Heatwave",
                alertEventType   = AlertEventType.HEATWAVE,
                confidence       = 0.74f,
                timeToEventHours = 72,
                status           = AlertStatus.WATCH
            ),
            Alert(
                id               = "demo_004",
                lat              = centerLat - 0.006,
                lon              = centerLon - 0.004,
                severity         = AlertSeverity.MEDIUM,
                message          = "Cyclonic circulation developing offshore. Monitor for " +
                        "escalation over the next 48 hours.",
                timestamp        = System.currentTimeMillis() - 10_800_000,
                district         = "Coastal Zone",
                eventType        = "Cyclone",
                alertEventType   = AlertEventType.CYCLONE,
                confidence       = 0.61f,
                timeToEventHours = 60,
                status           = AlertStatus.WATCH
            )
        )

        _alerts.addAll(seeds)
    }

    // ── CRUD helpers ───────────────────────────────────────────────────────

    fun pinAlert(id: String) {
        val idx = _alerts.indexOfFirst { it.id == id }
        if (idx >= 0) _alerts[idx] = _alerts[idx].copy(isPinned = true)
    }

    fun muteAlert(id: String) {
        val idx = _alerts.indexOfFirst { it.id == id }
        if (idx >= 0) _alerts[idx] = _alerts[idx].copy(isMuted = true)
    }

    fun saveOffline(id: String) {
        val idx = _alerts.indexOfFirst { it.id == id }
        if (idx >= 0) _alerts[idx] = _alerts[idx].copy(offlineSaved = true)
    }

    fun addAlert(alert: Alert) {
        _alerts.removeAll { it.id == alert.id }
        _alerts.add(alert)
    }
}