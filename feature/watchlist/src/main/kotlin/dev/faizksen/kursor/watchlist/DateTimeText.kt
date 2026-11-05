package dev.faizksen.kursor.watchlist

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun rememberShortDateTimeText(
    instant: Instant,
    clock: Clock = Clock.System,
    zone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val configuration = LocalConfiguration.current

    return remember(instant, configuration, clock, zone) {
        val locale = configuration.locales[0]

        formatShortDateTime(
            instant = instant,
            zone = zone,
            locale = locale,
            today = clock.todayIn(zone),
            datePattern = DateFormat.getBestDateTimePattern(locale, "d MMM"),
        )
    }
}

internal fun formatShortDateTime(
    instant: Instant,
    zone: TimeZone,
    locale: Locale,
    today: LocalDate,
    datePattern: String,
): String {
    val dateTime = instant.toLocalDateTime(zone)

    val formatter = if (dateTime.date == today) {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
    } else {
        DateTimeFormatter.ofPattern(datePattern, locale)
    }

    return formatter.format(dateTime.toJavaLocalDateTime())
}
