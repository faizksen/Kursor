package dev.faizksen.kursor.model

data class WatchedPair(
    val id: Long,
    val pair: CurrencyPair,
    val provider: ProviderKey?,
    val position: Int
)
