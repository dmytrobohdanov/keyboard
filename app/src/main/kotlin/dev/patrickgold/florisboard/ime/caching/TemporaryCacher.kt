package dev.patrickgold.florisboard.ime.caching

import android.content.Context
import android.util.Log
import dev.patrickgold.florisboard.appContext
import kotlin.getValue

class TemporaryCacher(context: Context) {
    private val appContext by context.appContext()

    fun writeToCache(text: String, inputType: InputType) {
        Log.d("piing", "---->> writeToCache: type: $inputType" +
            "\n$text")
    }
}
