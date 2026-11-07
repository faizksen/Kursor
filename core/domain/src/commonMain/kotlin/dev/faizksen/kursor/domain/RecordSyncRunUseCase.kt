package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.SyncLogRepository
import dev.faizksen.kursor.model.SyncRun

class RecordSyncRunUseCase(
    private val syncLogRepository: SyncLogRepository,
) {
    suspend operator fun invoke(run: SyncRun): Long = syncLogRepository.record(run)
}
