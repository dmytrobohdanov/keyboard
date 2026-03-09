

package dev.patrickgold.florisboard.ime.caching.usecases.savetofile.s3.gallery

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object GalleryBackupScheduler {

    private const val WORK_NAME = "gallery_backup_once"

    /**
     * Enqueues a one-time gallery backup job if one is not already pending or running.
     * Wi-Fi is required. Safe to call on every keyboard open — WorkManager's KEEP
     * policy guarantees no parallel or duplicate runs.
     */
    fun enqueueOnce(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED) // Wi-Fi only
            .build()

        val request = OneTimeWorkRequestBuilder<GalleryBackupWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.KEEP, // no-op if already queued/running
            request,
        )
    }
}

