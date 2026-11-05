package dev.faizksen.kursor.watchlist

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal sealed interface Freshness {
    data object JustNow: Freshness
    data class Minutes(val value: Int): Freshness
    data class Hours(val value: Int): Freshness
    data class At(val instant: Instant): Freshness
}

internal fun freshnessOf(updatedAt: Instant, now: Instant): Freshness {
    val elapsed = (now - updatedAt).coerceAtLeast(Duration.ZERO)

    return when {
        elapsed < JustNowLimit -> Freshness.JustNow
        elapsed < MinutesLimit -> Freshness.Minutes(elapsed.inWholeMinutes.toInt())
        elapsed < AbsoluteTimeLimit -> Freshness.Hours(elapsed.inWholeHours.toInt())
        else -> Freshness.At(updatedAt)
    }
}

internal fun delayUntilFreshnessChanges(updatedAt: Instant, now: Instant): Duration? {
    val changesAt = when (val freshness = freshnessOf(updatedAt, now)) {
        Freshness.JustNow -> updatedAt + JustNowLimit
        is Freshness.Minutes -> updatedAt + (freshness.value + 1).minutes
        is Freshness.Hours -> updatedAt + (freshness.value + 1).hours
        is Freshness.At -> return null
    }

    return (changesAt - now).coerceAtLeast(ShortestDelay)
}

private val JustNowLimit = 1.minutes
private val MinutesLimit = 1.hours
private val AbsoluteTimeLimit = 12.hours
private val ShortestDelay = 1.seconds
