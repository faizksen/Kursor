package dev.faizksen.kursor.watchlist

import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.Rate
import dev.faizksen.kursor.model.WatchedPair
import dev.faizksen.kursor.model.WatchedPairWithRate

internal fun WatchedPair.displayedPair(): CurrencyPair = if (inverted) {
    CurrencyPair(base = pair.quote, quote = pair.base)
} else {
    pair
}

internal fun WatchedPairWithRate.displayedValue(): Rate? = rate?.value?.let { value ->
    if (watchedPair.inverted) value.inverted() else value
}
