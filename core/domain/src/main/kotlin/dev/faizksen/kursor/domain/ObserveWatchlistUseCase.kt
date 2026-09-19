package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.RatesRepository
import dev.faizksen.kursor.model.WatchedPairWithRate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveWatchlistUseCase @Inject constructor(
    private val ratesRepository: RatesRepository,
) {
    operator fun invoke(): Flow<List<WatchedPairWithRate>> = ratesRepository.observeWatchlist()
}
