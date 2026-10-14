package dev.faizksen.kursor.domain

import dev.faizksen.kursor.model.SyncState
import dev.faizksen.kursor.model.SyncTrigger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveSyncStateUseCase @Inject constructor(
    private val syncScheduler: SyncScheduler,
) {
    operator fun invoke(trigger: SyncTrigger): Flow<SyncState> =
        syncScheduler.observeSyncQueue(trigger)
            .map { queue -> queue.state }
            .distinctUntilChanged()
}
