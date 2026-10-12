package dev.faizksen.kursor.watchlist

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun rememberShortDateTimeText(
    instant: Instant,
    clock: Clock = Clock.systemDefaultZone(),
): String {
    val configuration = LocalConfiguration.current

    return remember(instant, configuration, clock) {
        val zone = clock.zone
        val locale = configuration.locales[0]

        formatShortDateTime(
            instant = instant,
            zone = zone,
            locale = locale,
            today = LocalDate.now(clock),
            datePattern = DateFormat.getBestDateTimePattern(locale, "d MMM"),
        )
    }
}

internal fun formatShortDateTime(
    instant: Instant,
    zone: ZoneId,
    locale: Locale,
    today: LocalDate,
    datePattern: String,
): String {
    val dateTime = LocalDateTime.ofInstant(instant, zone)

    val formatter = if (dateTime.toLocalDate() == today) {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
    } else {
        DateTimeFormatter.ofPattern(datePattern, locale)
    }

    return formatter.format(dateTime)
}
