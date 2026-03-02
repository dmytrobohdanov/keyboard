package dev.patrickgold.florisboard.ime.caching.usecases.location

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dev.patrickgold.florisboard.ime.caching.usecases.location.models.toLocationData
import dev.patrickgold.florisboard.temporaryCacher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
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
                            continuation.resumeWithException(IllegalStateException("Location was null"))
                        }
                    }
                }
                task.addOnFailureListener { e ->
                    if (continuation.isActive) continuation.resumeWithException(e)
                }
            }
            val cacher by applicationContext.temporaryCacher()
            cacher.writeLocationToCache(location.toLocationData())
            Result.success()
        } catch (e: Exception) {
            Log.w("LocationWorker", "Failed to fetch location", e)
            Result.failure()
        }
    }
}
