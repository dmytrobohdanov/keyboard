package dev.patrickgold.florisboard.ime.caching.usecases.location.models

import android.location.Location

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long = System.currentTimeMillis()
)

// Extension function to map Android Location object to our domain model
fun Location.toLocationData(): LocationData = LocationData(
    latitude = latitude,
    longitude = longitude,
    accuracy = accuracy
)
