package com.rahat.state

import com.rahat.ui.theme.RiskLevel

data class UiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // NEW: active risk prediction surfaced to all screens
    val activeRiskLevel: RiskLevel = RiskLevel.SAFE,
    val activeZone: String? = null,
    val offlineSynced: Boolean = false,
    val lastSyncTime: Long = 0L
)