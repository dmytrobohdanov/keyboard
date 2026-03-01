package dev.patrickgold.florisboard.ime.caching.usecases.location

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dev.patrickgold.florisboard.ime.caching.usecases.location.models.toLocationData
import dev.patrickgold.florisboard.ime.caching.usecases.savetofile.Cacher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.minutes

class LocationPoller(context: Context, private val cacher: Cacher) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fun startPolling(scope: CoroutineScope) {
        scope.launch {
            while (isActive) {
                fetchAndSave()
                delay(3.minutes)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchAndSave() {
        try {
            val location = suspendCancellableCoroutine { continuation ->
                val task = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                )
                task.addOnSuccessListener { loc ->
                    if (continuation.isActive) {
                        if (loc != null) {
                            continuation.resume(loc)
                        } else {
                            continuation.resumeWithException(
                                IllegalStateException("Location result was null")
                            )
                        }
                    }
                }
                task.addOnFailureListener { e ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(e)
                    }
                }
            }
            cacher.writeLocationToCache(location.toLocationData())
        } catch (e: Exception) {
            Log.w("LocationPoller", "Failed to fetch location", e)
        }
    }
}
