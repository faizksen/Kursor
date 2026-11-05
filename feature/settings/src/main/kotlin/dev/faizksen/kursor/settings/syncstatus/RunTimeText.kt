package dev.faizksen.kursor.settings.syncstatus

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant

internal fun formatRunTime(
    instant: Instant,
    zone: TimeZone,
    locale: Locale,
    today: LocalDate,
    timePattern: String,
    dateTimePattern: String,
): String {
    val dateTime = instant.toLocalDateTime(zone)
    val pattern = if (dateTime.date == today) timePattern else dateTimePattern

    return DateTimeFormatter.ofPattern(pattern, locale).format(dateTime.toJavaLocalDateTime())
}
