package com.rahat.state

import com.rahat.ui.theme.RiskLevel

data class RiskState(
    val level: RiskLevel = RiskLevel.SAFE,
    val zone: String? = null,
    val eventType: String? = null,
    val confidence: Float = 0f,
    val timeToEventHours: Int? = null,
    val isOffline: Boolean = false,
    val lastSyncTime: Long = 0L
) {
    val isActive: Boolean get() = level != RiskLevel.SAFE && level != RiskLevel.OFFLINE
    val bannerVisible: Boolean get() = isActive
}