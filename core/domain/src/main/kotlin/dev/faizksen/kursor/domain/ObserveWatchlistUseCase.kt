package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.RatesRepository
import dev.faizksen.kursor.model.WatchedPairWithRate
import kotlinx.coroutines.flow.Flow

fun interface ObserveWatchlistUseCase {
    operator fun invoke(): Flow<List<WatchedPairWithRate>>
}

internal class ObserveWatchlistUseCaseImpl(
    private val ratesRepository: RatesRepository,
): ObserveWatchlistUseCase {
    override operator fun invoke(): Flow<List<WatchedPairWithRate>> = ratesRepository.observeWatchlist()
}
