package dev.faizksen.kursor.model

data class WatchedPair(
    val id: Long,
    val pair: CurrencyPair,
    val position: Int
)

