package dev.patrickgold.florisboard.ime.caching.usecases.copied

import dev.patrickgold.florisboard.ime.clipboard.provider.ClipboardItem
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC)

fun ClipboardItem.formatToOutput(): String {
    val formattedTime = isoFormatter.format(Instant.ofEpochMilli(creationTimestampMs))
    return "$formattedTime\n${text ?: "<no text>"}"
}
