package dev.faizksen.kursor.watchlist.addpair

import dev.faizksen.kursor.model.Currency
import dev.faizksen.kursor.model.CurrencyCode

internal fun List<Currency>.matching(
    query: String,
    excluding: CurrencyCode?,
    nameOf: CurrencyNamer,
): List<Currency> {
    val trimmed = query.trim()
    val available = filter { currency -> currency.code != excluding }

    if (trimmed.isEmpty()) return available

    return available.filter { currency ->
        currency.code.code.contains(trimmed, ignoreCase = true) ||
            nameOf(currency).contains(trimmed, ignoreCase = true)
    }
}
