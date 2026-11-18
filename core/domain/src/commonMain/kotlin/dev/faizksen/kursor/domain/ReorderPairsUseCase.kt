package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.WatchedPairsRepository

fun interface ReorderPairsUseCase {
    suspend operator fun invoke(ids: List<Long>)
}

internal class ReorderPairsUseCaseImpl(
    private val watchedPairsRepository: WatchedPairsRepository,
): ReorderPairsUseCase {
    override suspend fun invoke(ids: List<Long>) = watchedPairsRepository.reorder(ids)
}
