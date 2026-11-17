package dev.faizksen.kursor.settings.syncstatus

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.datetime.todayIn
import kotlin.time.Clock
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

@Composable
internal fun rememberRunTimeText(
    instant: Instant,
    clock: Clock = Clock.System,
): String {
    val configuration = LocalConfiguration.current
    val zone = TimeZone.currentSystemDefault()

    return remember(instant, configuration, clock, zone) {
        val locale = configuration.locales[0]

        formatRunTime(
            instant = instant,
            zone = zone,
            locale = locale,
            today = clock.todayIn(zone),
            timePattern = DateFormat.getBestDateTimePattern(locale, TimeSkeleton),
            dateTimePattern = DateFormat.getBestDateTimePattern(locale, DateTimeSkeleton),
        )
    }
}
