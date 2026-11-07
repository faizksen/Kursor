package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.RatesRepository
import dev.faizksen.kursor.model.SourceId
import dev.faizksen.kursor.model.SyncOutcome

class RefreshRatesUseCase(
    private val ratesRepository: RatesRepository,
) {
    suspend operator fun invoke(): SyncOutcome = ratesRepository.refresh(SourceId.FRANKFURTER)
}
