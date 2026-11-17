package dev.faizksen.kursor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.faizksen.kursor.domain.RefreshCatalogueUseCase
import dev.faizksen.kursor.domain.SyncOnFirstLaunchUseCase
import dev.faizksen.kursor.domain.SyncScheduler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MainViewModel(
    private val syncScheduler: SyncScheduler,
    private val refreshCatalogue: RefreshCatalogueUseCase,
    private val syncOnFirstLaunch: SyncOnFirstLaunchUseCase,
): ViewModel() {

    fun scheduleSync() {
        syncScheduler.schedulePeriodicSync()
        launchIgnoringFailure { syncOnFirstLaunch() }
    }

    fun refreshCatalogue() {
        launchIgnoringFailure { refreshCatalogue.invoke() }
    }

    private fun launchIgnoringFailure(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (ignored: Throwable) {
            }
        }
    }
}
