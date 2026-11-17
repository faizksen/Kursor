package dev.faizksen.kursor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.faizksen.kursor.domain.RefreshCatalogueUseCase
import dev.faizksen.kursor.domain.SyncScheduler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MainViewModel(
    private val syncScheduler: SyncScheduler,
    private val refreshCatalogue: RefreshCatalogueUseCase,
): ViewModel() {

    fun scheduleSync() {
        syncScheduler.schedulePeriodicSync()
    }

    fun refreshCatalogue() {
        viewModelScope.launch {
            try {
                refreshCatalogue.invoke()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Unit
            }
        }
    }
}
