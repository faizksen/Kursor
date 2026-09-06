package dev.faizksen.kursor.data.model

sealed interface AddPairResult {
    val id: Long

    data class Added(override val id: Long) : AddPairResult
    data class AlreadyWatched(override val id: Long) : AddPairResult
}
