package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.SyncLogRepository
import dev.faizksen.kursor.model.SyncRun
import javax.inject.Inject

class RecordSyncRunUseCase @Inject constructor(
    private val syncLogRepository: SyncLogRepository,
) {
    suspend operator fun invoke(run: SyncRun): Long = syncLogRepository.record(run)
}
