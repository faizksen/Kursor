package dev.faizksen.kursor.domain

import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.SyncQueue
import dev.faizksen.kursor.model.SyncTrigger
import kotlinx.coroutines.flow.Flow

interface SyncScheduler {
    fun schedulePeriodicSync()
    fun requestImmediateSync()
    fun requestPairSync(pair: CurrencyPair)
    fun observeSyncQueue(trigger: SyncTrigger): Flow<SyncQueue>
}
