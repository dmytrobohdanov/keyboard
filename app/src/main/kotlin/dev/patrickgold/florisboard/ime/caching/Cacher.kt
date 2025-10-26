package dev.patrickgold.florisboard.ime.caching

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException

object Cacher {
    private const val TAG = "Cacher"

    // Application context stored to avoid leaking Activity/Service contexts.
    private var appContext: Context? = null

    /**
     * Initialize the Cacher with an Android Context (preferably applicationContext).
     * Call this once (for example from your Application.onCreate) before using writeToCache().
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Writes [text] to a file in the app cache directory.
     * Uses the default filename `cached_text.txt`.
     * Throws IllegalStateException if Cacher wasn't initialized.
     */
    fun writeToCache(text: String) {
        writeToCache(text, "cached_text.txt")
    }

    /**
     * Writes [text] to a file named [filename] in the app cache directory and returns the written File.
     */
    fun writeToCache(text: String, filename: String): File {
        val ctx = appContext ?: throw IllegalStateException(
            "Cacher not initialized. Call Cacher.init(context) before using writeToCache()."
        )

        val file = File(ctx.cacheDir, filename)
        try {
            // Ensure parent directory exists (cacheDir should exist, but be defensive)
            file.parentFile?.let { parent -> if (!parent.exists()) parent.mkdirs() }
            file.writeText(text)
            return file
        } catch (e: IOException) {
            Log.e(TAG, "Failed to write to cache file=$file: ${e.message}", e)
            throw e
        }
    }
}
