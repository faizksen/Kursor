package dev.faizksen.kursor.watchlist

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class FreshnessTest {

    private val updatedAt = Instant.parse("2026-10-13T10:00:00Z")

    @Test
    fun `first minute is just now`() {
        assertThat(freshnessAfter(Duration.ZERO)).isEqualTo(Freshness.JustNow)
        assertThat(freshnessAfter(59.seconds)).isEqualTo(Freshness.JustNow)
    }

    @Test
    fun `a full minute is counted in minutes`() {
        assertThat(freshnessAfter(1.minutes)).isEqualTo(Freshness.Minutes(1))
        assertThat(freshnessAfter(59.minutes)).isEqualTo(Freshness.Minutes(59))
    }

    @Test
    fun `a full hour is counted in hours`() {
        assertThat(freshnessAfter(1.hours)).isEqualTo(Freshness.Hours(1))
        assertThat(freshnessAfter(11.hours)).isEqualTo(Freshness.Hours(11))
    }

    @Test
    fun `twelve hours switch to absolute time`() {
        assertThat(freshnessAfter(12.hours)).isEqualTo(Freshness.At(updatedAt))
        assertThat(freshnessAfter(3.days)).isEqualTo(Freshness.At(updatedAt))
    }

    @Test
    fun `clock behind the update is still just now`() {
        assertThat(freshnessAfter((-5).minutes)).isEqualTo(Freshness.JustNow)
    }

    @Test
    fun `just now changes at the first minute`() {
        assertThat(delayAfter(Duration.ZERO)).isEqualTo(1.minutes)
        assertThat(delayAfter(45.seconds)).isEqualTo(15.seconds)
    }

    @Test
    fun `minutes change at the next minute`() {
        assertThat(delayAfter(1.minutes)).isEqualTo(1.minutes)
        assertThat(delayAfter(90.seconds)).isEqualTo(30.seconds)
    }

    @Test
    fun `the last minute of the hour waits for the hour`() {
        assertThat(delayAfter(59.minutes)).isEqualTo(1.minutes)
    }

    @Test
    fun `hours change at the next hour`() {
        assertThat(delayAfter(1.hours)).isEqualTo(1.hours)
        assertThat(delayAfter(90.minutes)).isEqualTo(30.minutes)
    }

    @Test
    fun `the last hour waits for absolute time`() {
        assertThat(delayAfter(11.hours)).isEqualTo(1.hours)
    }

    @Test
    fun `absolute time never changes again`() {
        assertThat(delayAfter(12.hours)).isNull()
    }

    @Test
    fun `delay is never zero`() {
        assertThat(delayAfter((-5).minutes)).isAtLeast(1.seconds)
    }

    private fun freshnessAfter(elapsed: Duration): Freshness =
        freshnessOf(updatedAt = updatedAt, now = updatedAt.plus(elapsed))

    private fun delayAfter(elapsed: Duration): Duration? =
        delayUntilFreshnessChanges(updatedAt = updatedAt, now = updatedAt.plus(elapsed))
}
