package com.rahat.data.model

import com.rahat.ui.theme.RiskLevel

data class RiskPrediction(
    val id: String,
    val zone: String,
    val district: String,
    val eventType: AlertEventType,
    val riskLevel: RiskLevel,
    val confidence: Float,
    val timeToEventHours: Int?,
    val rainfallAnomaly: Float = 0f,
    val terrainInstability: Float = 0f,
    val historicalMatch: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val isOfflineSaved: Boolean = false
)