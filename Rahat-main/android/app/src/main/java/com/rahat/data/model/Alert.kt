package com.rahat.data.model

enum class AlertSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

// NEW: maps to spec event types for guidance logic
enum class AlertEventType {
    FLOOD, LANDSLIDE, HEATWAVE, CYCLONE, EARTHQUAKE, UNKNOWN
}

// NEW: maps to spec card status tags
enum class AlertStatus {
    WATCH, WARNING, CRITICAL, RESOLVED
}

data class Alert(
    val id: String,
    val lat: Double,
    val lon: Double,
    val severity: AlertSeverity,
    val message: String,
    val timestamp: Long,
    // ── NEW AI fields ────────────────────────────────────────────
    val district: String = "",
    val eventType: String = "",                     // raw string e.g. "Landslide"
    val alertEventType: AlertEventType = AlertEventType.UNKNOWN,
    val confidence: Float = 0f,                     // 0.0 – 1.0
    val timeToEventHours: Int? = null,              // null = unknown
    val status: AlertStatus = AlertStatus.WATCH,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val offlineSaved: Boolean = false
)

// Helper: convert severity → status
fun Alert.derivedStatus(): AlertStatus = when (severity) {
    AlertSeverity.CRITICAL -> AlertStatus.CRITICAL
    AlertSeverity.HIGH     -> AlertStatus.WARNING
    AlertSeverity.MEDIUM   -> AlertStatus.WATCH
    AlertSeverity.LOW      -> AlertStatus.WATCH
}

// Helper: human-readable time remaining
fun Alert.timeRemainingLabel(): String = when {
    timeToEventHours == null       -> "Time unknown"
    timeToEventHours <= 0          -> "Imminent"
    timeToEventHours < 24          -> "${timeToEventHours}h remaining"
    else                           -> "${timeToEventHours / 24}d remaining"
}

// Helper: confidence as percent string
fun Alert.confidencePercent(): String =
    if (confidence > 0f) "${(confidence * 100).toInt()}%" else "--"