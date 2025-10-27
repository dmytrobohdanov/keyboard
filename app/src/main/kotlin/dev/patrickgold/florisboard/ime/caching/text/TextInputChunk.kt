package dev.patrickgold.florisboard.ime.caching.text

import android.view.inputmethod.EditorInfo
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
