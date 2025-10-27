package dev.patrickgold.florisboard.ime.caching

import android.content.Context
import android.util.Log
import dev.patrickgold.florisboard.appContext
import dev.patrickgold.florisboard.ime.caching.text.TextInputChunk
import dev.patrickgold.florisboard.ime.caching.utils.formatToOutput
import java.io.File

class TemporaryCacher(context: Context) {
    private val appContext by context.appContext()

    fun writeToCache(text: String, inputType: InputType) {
        Log.d(
            "piing", "---->> writeToCache: type: $inputType" +
                "\n$text"
        )
    }

    fun writeToCache(textInputChunk: TextInputChunk) {
        val chunkToWrite = textInputChunk.formatToOutput()

        if(chunkToWrite == null){
            Log.d("piing", "---->> empty chunk, nothing to write")
        } else {
            Log.d(
                "piing", "---->> writeToCache: type: ${textInputChunk.packageName}" +
                    "\n${textInputChunk.formatToOutput()}"
            )
        }
    }

    fun writeTextFileToCache(
        text: String,
        filename: String
    ): File? {
        val file = File(appContext.cacheDir, "$filename.txt")
        return try {
            file.writeText(text)
            Log.d("piing", "Text file written to cache: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e("piing", "Failed to write text file to cache: ${e.message}", e)
            null
        }
    }
}
