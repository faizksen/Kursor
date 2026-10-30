package dev.faizksen.kursor.domain

import dev.faizksen.kursor.model.SyncQueue
import dev.faizksen.kursor.model.SyncTrigger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

fun interface ObserveSyncQueueUseCase {
    operator fun invoke(trigger: SyncTrigger): Flow<SyncQueue>
}

internal class ObserveSyncQueueUseCaseImpl @Inject constructor(
    private val syncScheduler: SyncScheduler,
): ObserveSyncQueueUseCase {
    override operator fun invoke(trigger: SyncTrigger): Flow<SyncQueue> =
        syncScheduler.observeSyncQueue(trigger)
}
