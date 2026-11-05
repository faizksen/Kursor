package dev.faizksen.kursor.watchlist

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import java.util.Locale
import kotlin.time.Instant

class FormatShortDateTimeTest {

    private val zone = TimeZone.of("Europe/Moscow")
    private val locale = Locale.of("ru")

    @Test
    fun `shows time when instant is today`() {
        val text = formatShortDateTime(
            instant = Instant.parse("2026-09-20T06:12:00Z"),
            zone = zone,
            locale = locale,
            today = LocalDate(2026, 9, 20),
            datePattern = "d MMM",
        )

        assertThat(text).isEqualTo("09:12")
    }

    @Test
    fun `shows date when instant is not today`() {
        val text = formatShortDateTime(
            instant = Instant.parse("2026-09-19T06:12:00Z"),
            zone = zone,
            locale = locale,
            today = LocalDate(2026, 9, 20),
            datePattern = "d MMM"
        )
        assertThat(text).isEqualTo("19 сент.")
    }

    @Test
    fun `uses user timezone when deciding what day it is`() {
        val text = formatShortDateTime(
            instant = Instant.parse("2026-09-19T22:30:00Z"),
            zone = TimeZone.of("Asia/Vladivostok"),
            locale = locale,
            today = LocalDate(2026, 9, 20),
            datePattern = "d MMM",
        )

        assertThat(text).isEqualTo("08:30")
    }
}