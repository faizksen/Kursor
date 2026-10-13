package dev.faizksen.kursor.domain

import dev.faizksen.kursor.model.SyncState
import dev.faizksen.kursor.model.SyncTrigger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSyncStateUseCase @Inject constructor(
    private val syncScheduler: SyncScheduler,
) {
    operator fun invoke(trigger: SyncTrigger): Flow<SyncState> =
        syncScheduler.observeSyncState(trigger)
}
