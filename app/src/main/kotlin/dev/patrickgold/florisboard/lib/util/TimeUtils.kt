

package dev.patrickgold.florisboard.lib.util

import android.icu.text.SimpleDateFormat
import dev.patrickgold.florisboard.lib.FlorisLocale
import dev.patrickgold.jetpref.datastore.model.LocalTime
import java.time.Instant
import java.time.format.DateTimeFormatter

object TimeUtils {
    private val ISO_INSTANT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", FlorisLocale.ENGLISH.base)

    fun currentUtcTimestamp(): CharSequence {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now())
    }

    val LocalTime.javaLocalTime: java.time.LocalTime
        get() = java.time.LocalTime.of(hour, minute)
}
