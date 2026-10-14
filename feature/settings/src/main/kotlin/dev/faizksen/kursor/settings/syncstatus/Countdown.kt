package dev.faizksen.kursor.settings.syncstatus

import java.time.Duration
import java.time.Instant

internal sealed interface Countdown {
    data object Imminent: Countdown
    data class Minutes(val value: Int): Countdown
    data class Hours(val value: Int, val minutes: Int): Countdown
}

internal fun countdownTo(target: Instant, now: Instant): Countdown {
    val minutesLeft = minutesLeft(target, now)

    return when {
        minutesLeft < 1 -> Countdown.Imminent
        minutesLeft < MinutesInHour -> Countdown.Minutes(minutesLeft.toInt())
        else -> Countdown.Hours(
            value = (minutesLeft / MinutesInHour).toInt(),
            minutes = (minutesLeft % MinutesInHour).toInt(),
        )
    }
}

internal fun delayUntilCountdownChanges(target: Instant, now: Instant): Duration? {
    val minutesLeft = minutesLeft(target, now)
    if (minutesLeft < 1) return null

    val changesAt = target.minus(Duration.ofMinutes(minutesLeft - 1))

    return Duration.between(now, changesAt).coerceAtLeast(ShortestDelay)
}

private fun minutesLeft(target: Instant, now: Instant): Long {
    val left = Duration.between(now, target)
    if (left <= Duration.ZERO) return 0

    return (left.seconds + SecondsInMinute - 1) / SecondsInMinute
}

private const val MinutesInHour = 60L
private const val SecondsInMinute = 60L
private val ShortestDelay = Duration.ofSeconds(1)
