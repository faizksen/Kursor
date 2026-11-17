package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.SyncLogRepository
import dev.faizksen.kursor.model.ProviderAttempt
import dev.faizksen.kursor.model.SyncRun

class RecordSyncRunUseCase(
    private val syncLogRepository: SyncLogRepository,
) {
    suspend operator fun invoke(
        run: SyncRun,
        attempts: List<ProviderAttempt> = emptyList(),
    ): Long = syncLogRepository.record(run, attempts)
}
