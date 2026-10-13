package dev.faizksen.kursor.watchlist

import java.time.Duration
import java.time.Instant

internal sealed interface Freshness {
    data object JustNow: Freshness
    data class Minutes(val value: Int): Freshness
    data class Hours(val value: Int): Freshness
    data class At(val instant: Instant): Freshness
}

internal fun freshnessOf(updatedAt: Instant, now: Instant): Freshness {
    val elapsed = Duration.between(updatedAt, now).coerceAtLeast(Duration.ZERO)

    return when {
        elapsed < JustNowLimit -> Freshness.JustNow
        elapsed < MinutesLimit -> Freshness.Minutes(elapsed.toMinutes().toInt())
        elapsed < AbsoluteTimeLimit -> Freshness.Hours(elapsed.toHours().toInt())
        else -> Freshness.At(updatedAt)
    }
}

internal fun delayUntilFreshnessChanges(updatedAt: Instant, now: Instant): Duration? {
    val changesAt = when (val freshness = freshnessOf(updatedAt, now)) {
        Freshness.JustNow -> updatedAt.plus(JustNowLimit)
        is Freshness.Minutes -> updatedAt.plus(Duration.ofMinutes(freshness.value + 1L))
        is Freshness.Hours -> updatedAt.plus(Duration.ofHours(freshness.value + 1L))
        is Freshness.At -> return null
    }

    return Duration.between(now, changesAt).coerceAtLeast(ShortestDelay)
}

private val JustNowLimit = Duration.ofMinutes(1)
private val MinutesLimit = Duration.ofHours(1)
private val AbsoluteTimeLimit = Duration.ofHours(12)
private val ShortestDelay = Duration.ofSeconds(1)
