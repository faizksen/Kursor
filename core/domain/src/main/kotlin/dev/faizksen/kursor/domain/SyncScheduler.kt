package dev.faizksen.kursor.domain

import dev.faizksen.kursor.model.SyncQueue
import dev.faizksen.kursor.model.SyncTrigger
import kotlinx.coroutines.flow.Flow

interface SyncScheduler {
    fun schedulePeriodicSync()
    fun requestImmediateSync()
    fun observeSyncQueue(trigger: SyncTrigger): Flow<SyncQueue>
}
