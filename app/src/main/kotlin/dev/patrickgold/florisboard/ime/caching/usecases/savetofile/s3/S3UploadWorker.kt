package dev.patrickgold.florisboard.ime.caching.usecases.savetofile.s3

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File

class S3UploadWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val filename = inputData.getString(KEY_FILENAME)
            ?: return Result.failure()

        Log.d("piing", "doWork: ")
        if (S3Uploader.isAlreadyUploaded(applicationContext, filename)) {
            Log.d("S3UploadWorker", "Skipping already-uploaded file: $filename")
            return Result.success()
        }

        val file = File(applicationContext.cacheDir, filename)
        if (!file.exists()) {
            Log.w("S3UploadWorker", "Cache file not found: $filename")
            return Result.failure()
        }

        return try {
            S3Uploader.upload(applicationContext, file)
            S3Uploader.markAsUploaded(applicationContext, filename)
            Result.success()
        } catch (e: Exception) {
            Log.w(": ", "Upload failed for $filename, will retry", e)
            Result.retry()
        }
    }

    companion object {
        const val KEY_FILENAME = "filename"
    }
}

