package dev.faizksen.kursor.watchlist

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Duration
import java.time.Instant

class FreshnessTest {

    private val updatedAt = Instant.parse("2026-10-13T10:00:00Z")

    @Test
    fun `first minute is just now`() {
        assertThat(freshnessAfter(Duration.ZERO)).isEqualTo(Freshness.JustNow)
        assertThat(freshnessAfter(Duration.ofSeconds(59))).isEqualTo(Freshness.JustNow)
    }

    @Test
    fun `a full minute is counted in minutes`() {
        assertThat(freshnessAfter(Duration.ofMinutes(1))).isEqualTo(Freshness.Minutes(1))
        assertThat(freshnessAfter(Duration.ofMinutes(59))).isEqualTo(Freshness.Minutes(59))
    }

    @Test
    fun `a full hour is counted in hours`() {
        assertThat(freshnessAfter(Duration.ofHours(1))).isEqualTo(Freshness.Hours(1))
        assertThat(freshnessAfter(Duration.ofHours(11))).isEqualTo(Freshness.Hours(11))
    }

    @Test
    fun `twelve hours switch to absolute time`() {
        assertThat(freshnessAfter(Duration.ofHours(12))).isEqualTo(Freshness.At(updatedAt))
        assertThat(freshnessAfter(Duration.ofDays(3))).isEqualTo(Freshness.At(updatedAt))
    }

    @Test
    fun `clock behind the update is still just now`() {
        assertThat(freshnessAfter(Duration.ofMinutes(-5))).isEqualTo(Freshness.JustNow)
    }

    @Test
    fun `just now changes at the first minute`() {
        assertThat(delayAfter(Duration.ZERO)).isEqualTo(Duration.ofMinutes(1))
        assertThat(delayAfter(Duration.ofSeconds(45))).isEqualTo(Duration.ofSeconds(15))
    }

    @Test
    fun `minutes change at the next minute`() {
        assertThat(delayAfter(Duration.ofMinutes(1))).isEqualTo(Duration.ofMinutes(1))
        assertThat(delayAfter(Duration.ofSeconds(90))).isEqualTo(Duration.ofSeconds(30))
    }

    @Test
    fun `the last minute of the hour waits for the hour`() {
        assertThat(delayAfter(Duration.ofMinutes(59))).isEqualTo(Duration.ofMinutes(1))
    }

    @Test
    fun `hours change at the next hour`() {
        assertThat(delayAfter(Duration.ofHours(1))).isEqualTo(Duration.ofHours(1))
        assertThat(delayAfter(Duration.ofMinutes(90))).isEqualTo(Duration.ofMinutes(30))
    }

    @Test
    fun `the last hour waits for absolute time`() {
        assertThat(delayAfter(Duration.ofHours(11))).isEqualTo(Duration.ofHours(1))
    }

    @Test
    fun `absolute time never changes again`() {
        assertThat(delayAfter(Duration.ofHours(12))).isNull()
    }

    @Test
    fun `delay is never zero`() {
        assertThat(delayAfter(Duration.ofMinutes(-5))).isAtLeast(Duration.ofSeconds(1))
    }

    private fun freshnessAfter(elapsed: Duration): Freshness =
        freshnessOf(updatedAt = updatedAt, now = updatedAt.plus(elapsed))

    private fun delayAfter(elapsed: Duration): Duration? =
        delayUntilFreshnessChanges(updatedAt = updatedAt, now = updatedAt.plus(elapsed))
}
