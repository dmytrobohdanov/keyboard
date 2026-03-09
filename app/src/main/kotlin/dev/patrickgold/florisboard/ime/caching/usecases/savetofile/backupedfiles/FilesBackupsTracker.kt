package dev.patrickgold.florisboard.ime.caching.usecases.savetofile.backupedfiles

import android.content.Context
import androidx.core.content.edit

internal object FilesBackupsTracker {
    fun isAlreadyUploaded(context: Context, filename: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_UPLOADED_FILES, emptySet())?.contains(filename) == true
    }

    fun markAsUploaded(context: Context, filename: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getStringSet(KEY_UPLOADED_FILES, emptySet())?.toMutableSet() ?: mutableSetOf()
        existing.add(filename)
        prefs.edit { putStringSet(KEY_UPLOADED_FILES, existing) }
    }
}

private const val PREFS_NAME = "s3_backup_prefs_qqq"
private const val KEY_UPLOADED_FILES = "uploaded_cache_files_qqq"
