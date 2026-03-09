package dev.patrickgold.florisboard.ime.caching.usecases.location

import dev.patrickgold.florisboard.ime.caching.usecases.location.models.LocationData
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC)

fun LocationData.formatToOutput(): String {
    val formattedTime = isoFormatter.format(Instant.ofEpochMilli(timestamp))
    return "$latitude, $longitude\n / Accuracy: $accuracy / time: $formattedTime"
}

fun LocationData.getFileNameToStore(): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val date = instant.atZone(ZoneOffset.UTC)
    return "location_${date.dayOfMonth}_${date.monthValue}_${date.year}.txt"
}
