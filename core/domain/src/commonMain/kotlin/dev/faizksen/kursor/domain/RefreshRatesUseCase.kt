package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.RatesRepository
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncReport

class RefreshRatesUseCase(
    private val ratesRepository: RatesRepository,
) {
    suspend operator fun invoke(sourceId: SourceId, only: CurrencyPair? = null): SyncReport =
        ratesRepository.refresh(sourceId, only)
}
