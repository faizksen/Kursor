package dev.faizksen.kursor.watchlist

internal fun LastUpdate.Success.partialCount(): String? {
    val updated = updatedCount ?: return null
    val requested = requestedCount ?: return null

    return if (updated in 0 until requested) "$updated/$requested" else null
}
