package dev.faizksen.kursor.ui

import androidx.lifecycle.ViewModel
import dev.faizksen.kursor.domain.SyncScheduler

class MainViewModel(
    private val syncScheduler: SyncScheduler
): ViewModel() {
    fun scheduleSync() {
        syncScheduler.schedulePeriodicSync()
    }
}