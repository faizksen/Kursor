package dev.faizksen.kursor.settings.syncstatus

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class CountdownTest {

    private val now = Instant.parse("2026-10-14T09:38:00Z")

    @Test
    fun `a passed moment is imminent`() {
        assertThat(countdownIn(Duration.ZERO)).isEqualTo(Countdown.Imminent)
        assertThat(countdownIn((-7).minutes)).isEqualTo(Countdown.Imminent)
    }

    @Test
    fun `a started minute is shown as one minute`() {
        assertThat(countdownIn(1.seconds)).isEqualTo(Countdown.Minutes(1))
        assertThat(countdownIn(59.seconds)).isEqualTo(Countdown.Minutes(1))
    }

    @Test
    fun `an incomplete minute rounds up`() {
        assertThat(countdownIn(61.seconds)).isEqualTo(Countdown.Minutes(2))
        assertThat(countdownIn(120.seconds)).isEqualTo(Countdown.Minutes(2))
    }

    @Test
    fun `an hour is split into hours and minutes`() {
        assertThat(countdownIn(60.minutes)).isEqualTo(Countdown.Hours(1, 0))
        assertThat(countdownIn(252.minutes)).isEqualTo(Countdown.Hours(4, 12))
    }

    @Test
    fun `the next change is awaited until the displayed minute drops`() {
        assertThat(delayIn(90.seconds)).isEqualTo(30.seconds)
        assertThat(delayIn(5.minutes)).isEqualTo(60.seconds)
    }

    @Test
    fun `the shortest delay is a second`() {
        assertThat(delayIn(1.seconds)).isEqualTo(1.seconds)
    }

    @Test
    fun `a passed moment is never awaited`() {
        assertThat(delayIn(Duration.ZERO)).isNull()
        assertThat(delayIn((-3).minutes)).isNull()
    }

    private fun countdownIn(left: Duration): Countdown = countdownTo(now.plus(left), now)

    private fun delayIn(left: Duration): Duration? =
        delayUntilCountdownChanges(now.plus(left), now)
}
