

package dev.patrickgold.florisboard.ime.caching.usecases.savetofile.s3.cacher

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.patrickgold.florisboard.ime.caching.usecases.savetofile.s3.S3Uploader
import java.io.File

class CacherUploadWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val filename = inputData.getString(KEY_FILENAME)
            ?: return Result.failure()

        Log.d("piing", "doWork: ")

        val file = File(applicationContext.cacheDir, filename)
        if (!file.exists()) {
            Log.w("CacherUploadWorker", "Cache file not found: $filename")
            return Result.failure()
        }

        return try {
            S3Uploader.upload(applicationContext, file)
            S3Uploader.markAsUploaded(applicationContext, filename)
            Result.success()
        } catch (e: Exception) {
            Log.w("CacherUploadWorker", "Upload failed for $filename, will retry", e)
            Result.retry()
        }
    }

    companion object {
        const val KEY_FILENAME = "filename"
    }
}

