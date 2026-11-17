package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.WatchedPairsRepository
import dev.faizksen.kursor.data.model.AddPairResult
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.ProviderKey

enum class AddPairOutcome {
    ADDED,
    ALREADY_WATCHED,
}

fun interface AddWatchedPairUseCase {
    suspend operator fun invoke(pair: CurrencyPair, provider: ProviderKey): AddPairOutcome
}

internal class AddWatchedPairUseCaseImpl(
    private val watchedPairsRepository: WatchedPairsRepository,
    private val syncScheduler: SyncScheduler,
): AddWatchedPairUseCase {

    override suspend fun invoke(pair: CurrencyPair, provider: ProviderKey): AddPairOutcome =
        when (watchedPairsRepository.add(pair, provider)) {
            is AddPairResult.Added -> {
                syncScheduler.requestPairSync(pair)
                AddPairOutcome.ADDED
            }
            is AddPairResult.AlreadyWatched -> AddPairOutcome.ALREADY_WATCHED
        }
}
