package dev.faizksen.kursor.model

import kotlin.time.Instant

data class WatchedPair(
    val id: Long,
    val pair: CurrencyPair,
    val provider: ProviderKey?,
    val position: Int,
    val addedAt: Instant,
    val inverted: Boolean = false,
)
