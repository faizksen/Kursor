package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.WatchedPairsRepository

fun interface FlipPairUseCase {
    suspend operator fun invoke(id: Long)
}

internal class FlipPairUseCaseImpl(
    private val watchedPairsRepository: WatchedPairsRepository,
): FlipPairUseCase {
    override suspend fun invoke(id: Long) = watchedPairsRepository.flipDirection(id)
}
