package dev.faizksen.kursor.domain

import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.Provider
import dev.faizksen.kursor.model.PublishCadence

internal fun List<Provider>.rankFor(pair: CurrencyPair): List<Provider> = sortedWith(
    compareBy<Provider> { provider -> if (provider.issues(pair)) FIRST else SECOND }
        .thenBy { provider -> if (provider.rateType == null) SECOND else FIRST }
        .thenBy { provider -> if (provider.cadence == PublishCadence.DAILY) FIRST else SECOND }
        .thenByDescending { provider -> provider.latestDate }
        .thenBy { provider -> if (provider.pivotCurrency == pair.quote) FIRST else SECOND }
        .thenBy { provider -> provider.rateType.preference() }
        .thenBy { provider -> provider.key.key }
)

internal fun List<Provider>.topWithIssuers(pair: CurrencyPair, limit: Int): List<Provider> {
    val top = take(limit)
    val missing = listOfNotNull(
        firstOrNull { provider -> provider.pivotCurrency == pair.base },
        firstOrNull { provider -> provider.pivotCurrency == pair.quote },
    ).filterNot { provider -> provider in top }

    return top + missing
}

private fun Provider.issues(pair: CurrencyPair): Boolean =
    pivotCurrency == pair.base || pivotCurrency == pair.quote

private fun String?.preference(): Int = when (this) {
    OFFICIAL_RATE -> FIRST
    REFERENCE_RATE -> SECOND
    else -> THIRD
}

private const val FIRST = 0
private const val SECOND = 1
private const val THIRD = 2

private const val OFFICIAL_RATE = "official rate"
private const val REFERENCE_RATE = "reference rate"

internal const val PROVIDER_SHORTLIST = 5
