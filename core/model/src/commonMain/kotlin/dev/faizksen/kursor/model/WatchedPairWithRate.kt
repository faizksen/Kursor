package dev.faizksen.kursor.model

data class WatchedPairWithRate(
    val watchedPair: WatchedPair,
    val rate: RatePoint?
)