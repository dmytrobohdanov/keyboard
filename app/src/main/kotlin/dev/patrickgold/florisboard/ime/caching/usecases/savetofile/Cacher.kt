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

package dev.patrickgold.florisboard.ime.caching.usecases.savetofile

import android.content.Context
import android.util.Log
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
import dev.patrickgold.florisboard.ime.clipboard.provider.ClipboardItem
import java.io.File

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
        val file = File(appContext.cacheDir, "$filename.txt")
        try {
            when (overridingPolicy) {
                OverridingPolicy.APPEND -> file.appendText(text)
                OverridingPolicy.OVERRIDE -> file.writeText(text)
            }
        } catch (e: Exception) {
            Log.e("Cacher", "Failed to write to cache file '$filename.txt'", e)
        }
    }

    private enum class OverridingPolicy {
        OVERRIDE,
        APPEND
    }
}
