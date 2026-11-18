package dev.faizksen.kursor.watchlist

import dev.faizksen.kursor.model.WatchedPairWithRate

internal fun <T> List<T>.moved(from: Int, to: Int): List<T> {
    if (from !in indices || to !in indices || from == to) return this

    return toMutableList().apply { add(to, removeAt(from)) }
}

internal fun List<WatchedPairWithRate>.inOrderOf(ids: List<Long>?): List<WatchedPairWithRate> {
    if (ids == null) return this

    val places = ids.withIndex().associate { (place, id) -> id to place }

    return sortedBy { watched -> places[watched.watchedPair.id] ?: places.size }
}
