package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.SyncLogRepository
import dev.faizksen.kursor.model.SyncRun
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

fun interface ObserveSyncLogUseCase {
    operator fun invoke(limit: Int): Flow<List<SyncRun>>
}

internal class ObserveSyncLogUseCaseImpl @Inject constructor(
    private val syncLogRepository: SyncLogRepository,
): ObserveSyncLogUseCase {
    override operator fun invoke(limit: Int): Flow<List<SyncRun>> = syncLogRepository.observeRecent(limit)
}
