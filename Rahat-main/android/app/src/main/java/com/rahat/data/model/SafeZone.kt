package com.rahat.data.model

data class SafeZone(
    val id: String,
    val name: String,
    val type: SafeZoneType,
    val lat: Double,
    val lng: Double,
    val distanceMeters: Int,
    val capacity: Int = 0,
    val isVerified: Boolean = false
)

enum class SafeZoneType {
    SHELTER, HIGH_GROUND, ELEVATION_SAFE, RELIEF_CAMP
}