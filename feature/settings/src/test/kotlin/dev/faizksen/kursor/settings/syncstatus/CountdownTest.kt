package dev.faizksen.kursor.settings.syncstatus

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Duration
import java.time.Instant

class CountdownTest {

    private val now = Instant.parse("2026-10-14T09:38:00Z")

    @Test
    fun `a passed moment is imminent`() {
        assertThat(countdownIn(Duration.ZERO)).isEqualTo(Countdown.Imminent)
        assertThat(countdownIn(Duration.ofMinutes(-7))).isEqualTo(Countdown.Imminent)
    }

    @Test
    fun `a started minute is shown as one minute`() {
        assertThat(countdownIn(Duration.ofSeconds(1))).isEqualTo(Countdown.Minutes(1))
        assertThat(countdownIn(Duration.ofSeconds(59))).isEqualTo(Countdown.Minutes(1))
    }

    @Test
    fun `an incomplete minute rounds up`() {
        assertThat(countdownIn(Duration.ofSeconds(61))).isEqualTo(Countdown.Minutes(2))
        assertThat(countdownIn(Duration.ofSeconds(120))).isEqualTo(Countdown.Minutes(2))
    }

    @Test
    fun `an hour is split into hours and minutes`() {
        assertThat(countdownIn(Duration.ofMinutes(60))).isEqualTo(Countdown.Hours(1, 0))
        assertThat(countdownIn(Duration.ofMinutes(252))).isEqualTo(Countdown.Hours(4, 12))
    }

    @Test
    fun `the next change is awaited until the displayed minute drops`() {
        assertThat(delayIn(Duration.ofSeconds(90))).isEqualTo(Duration.ofSeconds(30))
        assertThat(delayIn(Duration.ofMinutes(5))).isEqualTo(Duration.ofSeconds(60))
    }

    @Test
    fun `the shortest delay is a second`() {
        assertThat(delayIn(Duration.ofSeconds(1))).isEqualTo(Duration.ofSeconds(1))
    }

    @Test
    fun `a passed moment is never awaited`() {
        assertThat(delayIn(Duration.ZERO)).isNull()
        assertThat(delayIn(Duration.ofMinutes(-3))).isNull()
    }

    private fun countdownIn(left: Duration): Countdown = countdownTo(now.plus(left), now)

    private fun delayIn(left: Duration): Duration? =
        delayUntilCountdownChanges(now.plus(left), now)
}
