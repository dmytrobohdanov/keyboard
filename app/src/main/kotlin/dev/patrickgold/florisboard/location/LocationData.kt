package dev.patrickgold.florisboard.location

import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.TimeUnit

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun toString(): String {
        return "Lat: $latitude, Lon: $longitude, Acc: $accuracy m"
    }
}

// data/LocationService.kt
interface LocationService {
    /**
     * Emits the current location updates as a Kotlin Flow.
     * The location is emitted according to the Request interval (e.g., every 30 seconds).
     */
    fun getLocationUpdates(): Flow<LocationData>
}


private const val LOCATION_INTERVAL_SECONDS = 30L

class DefaultLocationService(
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationService {

    // Suppressing the missing permission check warning because the caller (ViewModel/UI)
    // is responsible for checking permissions before starting this flow.
    @Suppress("MissingPermission")
    override fun getLocationUpdates(): Flow<LocationData> = callbackFlow {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            TimeUnit.SECONDS.toMillis(LOCATION_INTERVAL_SECONDS)
        )
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(LOCATION_INTERVAL_SECONDS / 2))
            .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            .setWaitForAccurateLocation(true)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    // Attempt to send the location data to the Flow
                    trySend(location.toLocationData())
                    Log.d("piing", "onLocationResult: ${location.toLocationData()}")
                }
            }
        }

        // Start requesting location updates
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper() // Use main looper for callbacks
        )

        // The suspending block that executes when the Flow is cancelled or completes.
        // This is crucial for cleaning up resources.
        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}

// Extension function to map Android Location object to our domain model
fun Location.toLocationData(): LocationData = LocationData(
    latitude = latitude,
    longitude = longitude,
    accuracy = accuracy
)
