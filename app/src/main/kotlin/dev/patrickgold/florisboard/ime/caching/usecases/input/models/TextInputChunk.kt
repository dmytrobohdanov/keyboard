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

package dev.patrickgold.florisboard.ime.caching.usecases.input.models

import android.view.inputmethod.EditorInfo
import dev.patrickgold.florisboard.ime.caching.usecases.input.utils.getInputTypeLabel
import java.time.LocalDateTime

class TextInputChunk(
    editorInfo: EditorInfo
) {
    val text: StringBuilder = StringBuilder()
    val timestamp: LocalDateTime = LocalDateTime.now()
    val packageName: String = editorInfo.packageName ?: "unknown package"
    val fieldType: String = editorInfo.inputType.getInputTypeLabel()
    val label: String? = editorInfo.label?.toString()
    val hint: String? = editorInfo.hintText?.toString()
}
