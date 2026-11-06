package dev.faizksen.kursor.domain

import dev.faizksen.kursor.model.SyncState
import dev.faizksen.kursor.model.SyncTrigger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

fun interface ObserveSyncStateUseCase {
    operator fun invoke(trigger: SyncTrigger): Flow<SyncState>
}

internal class ObserveSyncStateUseCaseImpl(
    private val syncScheduler: SyncScheduler,
): ObserveSyncStateUseCase {
    override operator fun invoke(trigger: SyncTrigger): Flow<SyncState> =
        syncScheduler.observeSyncQueue(trigger)
            .map { queue -> queue.state }
            .distinctUntilChanged()
}
