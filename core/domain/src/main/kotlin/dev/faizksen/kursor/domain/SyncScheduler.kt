package dev.faizksen.kursor.domain

import dev.faizksen.kursor.model.SyncState
import dev.faizksen.kursor.model.SyncTrigger
import kotlinx.coroutines.flow.Flow

interface SyncScheduler {
    fun schedulePeriodicSync()
    fun requestImmediateSync()
    fun observeSyncState(trigger: SyncTrigger): Flow<SyncState>
}
