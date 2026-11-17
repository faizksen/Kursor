package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.RatesRepository
import kotlinx.coroutines.flow.first

fun interface SyncMissingRatesUseCase {
    suspend operator fun invoke()
}

internal class SyncMissingRatesUseCaseImpl(
    private val ratesRepository: RatesRepository,
    private val syncScheduler: SyncScheduler,
): SyncMissingRatesUseCase {

    override suspend fun invoke() {
        val watchlist = ratesRepository.observeWatchlist().first()

        if (watchlist.isNotEmpty() && watchlist.all { pair -> pair.rate == null }) {
            syncScheduler.requestImmediateSync()
        }
    }
}
