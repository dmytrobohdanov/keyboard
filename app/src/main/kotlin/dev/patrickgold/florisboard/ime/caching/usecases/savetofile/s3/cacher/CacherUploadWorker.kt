/*
 * Copyright (C) 2026 The FlorisBoard Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

