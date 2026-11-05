package dev.faizksen.kursor.settings.syncstatus

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

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

    val changesAt = target - (minutesLeft - 1).minutes

    return (changesAt - now).coerceAtLeast(ShortestDelay)
}

private fun minutesLeft(target: Instant, now: Instant): Long {
    val left = target - now
    if (left <= Duration.ZERO) return 0

    return (left.inWholeSeconds + SecondsInMinute - 1) / SecondsInMinute
}

private const val MinutesInHour = 60L
private const val SecondsInMinute = 60L
private val ShortestDelay = 1.seconds
