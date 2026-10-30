package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.RatesRepository
import dev.faizksen.kursor.model.WatchedPairWithRate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

fun interface ObserveWatchlistUseCase {
    operator fun invoke(): Flow<List<WatchedPairWithRate>>
}

internal class ObserveWatchlistUseCaseImpl @Inject constructor(
    private val ratesRepository: RatesRepository,
): ObserveWatchlistUseCase {
    override operator fun invoke(): Flow<List<WatchedPairWithRate>> = ratesRepository.observeWatchlist()
}
