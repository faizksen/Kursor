package dev.faizksen.kursor.domain

import dev.faizksen.kursor.model.SyncQueue
import dev.faizksen.kursor.model.SyncTrigger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSyncQueueUseCase @Inject constructor(
    private val syncScheduler: SyncScheduler,
) {
    operator fun invoke(trigger: SyncTrigger): Flow<SyncQueue> =
        syncScheduler.observeSyncQueue(trigger)
}
