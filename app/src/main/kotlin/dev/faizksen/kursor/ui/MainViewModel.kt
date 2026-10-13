package dev.faizksen.kursor.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.faizksen.kursor.domain.SyncScheduler
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val syncScheduler: SyncScheduler
): ViewModel() {
    fun scheduleSync() {
        syncScheduler.schedulePeriodicSync()
    }
}