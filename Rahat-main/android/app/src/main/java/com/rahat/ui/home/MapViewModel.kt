package com.rahat.ui.home

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rahat.data.model.Alert
import com.rahat.data.repo.AlertRepository
import com.rahat.data.firebase.FirestoreUserRepository
import com.rahat.data.repo.MeshRepository
import com.rahat.data.model.PeerState
import com.rahat.ui.theme.RiskLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class MapViewModel(
    private val repository: AlertRepository,
    private val userRepo: FirestoreUserRepository
) : ViewModel() {

    // ── Location ────────────────────────────────────────────────────────────
    private val _userLocation = MutableStateFlow<GeoPoint?>(null)
    val userLocation: StateFlow<GeoPoint?> = _userLocation.asStateFlow()

    // ── Alerts ───────────────────────────────────────────────────────────────
    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()

    // ── Nearby Peers ─────────────────────────────────────────────────────────
    val nearbyPeers: StateFlow<List<PeerState>> = MeshRepository.nearbyPeers

    // ── NEW: Active Risk Prediction ──────────────────────────────────────────
    // Holds the highest risk level detected for the user's area.
    // Null zone = no active prediction.
    private val _activeRiskLevel = MutableStateFlow(RiskLevel.SAFE)
    val activeRiskLevel: StateFlow<RiskLevel> = _activeRiskLevel.asStateFlow()

    private val _activeWarningZone = MutableStateFlow<String?>(null)
    val activeWarningZone: StateFlow<String?> = _activeWarningZone.asStateFlow()

    private val _activeEventType = MutableStateFlow<String?>(null)
    val activeEventType: StateFlow<String?> = _activeEventType.asStateFlow()

    private val _confidence = MutableStateFlow<Float>(0f)
    val confidence: StateFlow<Float> = _confidence.asStateFlow()

    private val _timeToEventHours = MutableStateFlow<Int?>(null)
    val timeToEventHours: StateFlow<Int?> = _timeToEventHours.asStateFlow()

    private val _offlineSynced = MutableStateFlow(false)
    val offlineSynced: StateFlow<Boolean> = _offlineSynced.asStateFlow()

    // ── Location Updates ─────────────────────────────────────────────────────
    fun onLocationUpdated(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)

        if (_userLocation.value == null) {
            viewModelScope.launch {
                repository.generateDemoSeeds(location.latitude, location.longitude)
                val loaded = repository.getAlerts()
                _alerts.value = loaded
                // NEW: derive risk from loaded alerts
                deriveRiskFromAlerts(loaded)
            }
        }

        _userLocation.value = geoPoint
    }

    // ── NEW: Derive risk level from alert list ────────────────────────────────
    private fun deriveRiskFromAlerts(alerts: List<Alert>) {
        if (alerts.isEmpty()) {
            _activeRiskLevel.value  = RiskLevel.SAFE
            _activeWarningZone.value = null
            return
        }
        // Pick highest severity alert
        val worst = alerts.maxByOrNull { it.severity.ordinal } ?: return
        _activeRiskLevel.value = when (worst.severity.name.uppercase()) {
            "CRITICAL" -> RiskLevel.CRITICAL
            "HIGH"     -> RiskLevel.WARNING
            "MEDIUM"   -> RiskLevel.WATCH
            else       -> RiskLevel.SAFE
        }
        _activeWarningZone.value  = worst.district.ifBlank { "Your Area" }
        _activeEventType.value    = worst.eventType.ifBlank { "Disaster" }
        _confidence.value         = worst.confidence
        _timeToEventHours.value   = worst.timeToEventHours
    }

    // ── NEW: Manual risk override (from AI feed) ──────────────────────────────
    fun setActiveRisk(
        level: RiskLevel,
        zone: String,
        eventType: String,
        confidence: Float,
        timeToEventHours: Int?
    ) {
        _activeRiskLevel.value   = level
        _activeWarningZone.value = zone
        _activeEventType.value   = eventType
        _confidence.value        = confidence
        _timeToEventHours.value  = timeToEventHours
    }

    fun clearRisk() {
        _activeRiskLevel.value   = RiskLevel.SAFE
        _activeWarningZone.value = null
        _activeEventType.value   = null
        _confidence.value        = 0f
        _timeToEventHours.value  = null
    }
}