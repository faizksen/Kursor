package dev.faizksen.kursor.settings.syncstatus

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Test
import java.util.Locale
import kotlin.time.Instant

class RunTimeTextTest {

    private val zone = TimeZone.of("Europe/Moscow")
    private val locale = Locale.of("ru")

    @Test
    fun `shows only time when the run finished today`() {
        val text = formatRunTime(
            instant = Instant.parse("2026-10-14T06:12:00Z"),
            zone = zone,
            locale = locale,
            today = LocalDate(2026, 10, 14),
            timePattern = TimePattern,
            dateTimePattern = DateTimePattern,
        )

        assertThat(text).isEqualTo("09:12")
    }

    @Test
    fun `shows the date when the run finished on another day`() {
        val text = formatRunTime(
            instant = Instant.parse("2026-10-13T06:12:00Z"),
            zone = zone,
            locale = locale,
            today = LocalDate(2026, 10, 14),
            timePattern = TimePattern,
            dateTimePattern = DateTimePattern,
        )

        assertThat(text).isEqualTo("13 окт. 09:12")
    }

    @Test
    fun `uses user timezone when deciding what day it is`() {
        val text = formatRunTime(
            instant = Instant.parse("2026-10-13T22:30:00Z"),
            zone = TimeZone.of("Asia/Vladivostok"),
            locale = locale,
            today = LocalDate(2026, 10, 14),
            timePattern = TimePattern,
            dateTimePattern = DateTimePattern,
        )

        assertThat(text).isEqualTo("08:30")
    }

    @Test
    fun `renders time in the user timezone and not in UTC`() {
        val text = formatRunTime(
            instant = Instant.parse("2026-10-14T06:12:00Z"),
            zone = TimeZone.of("Asia/Vladivostok"),
            locale = locale,
            today = LocalDate(2026, 10, 14),
            timePattern = TimePattern,
            dateTimePattern = DateTimePattern,
        )

        assertThat(text).isEqualTo("16:12")
    }

    private companion object {
        const val TimePattern = "HH:mm"
        const val DateTimePattern = "d MMM HH:mm"
    }
}
