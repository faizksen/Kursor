package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.WatchedPairsRepository
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.model.WatchedPair

fun interface WatchedPairUseCase {
    suspend operator fun invoke(id: Long): WatchedPair?
}

fun interface ChangeProviderUseCase {
    suspend operator fun invoke(id: Long, provider: ProviderKey)
}

internal class WatchedPairUseCaseImpl(
    private val watchedPairsRepository: WatchedPairsRepository,
): WatchedPairUseCase {
    override suspend fun invoke(id: Long): WatchedPair? = watchedPairsRepository.byId(id)
}

internal class ChangeProviderUseCaseImpl(
    private val watchedPairsRepository: WatchedPairsRepository,
    private val syncScheduler: SyncScheduler,
): ChangeProviderUseCase {

    override suspend fun invoke(id: Long, provider: ProviderKey) {
        val watched = watchedPairsRepository.byId(id) ?: return

        watchedPairsRepository.setProvider(id, provider)
        syncScheduler.requestPairSync(watched.pair)
    }
}
