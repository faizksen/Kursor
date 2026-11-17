package dev.faizksen.kursor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.faizksen.kursor.domain.RefreshCatalogueUseCase
import dev.faizksen.kursor.domain.ResolveMissingProvidersUseCase
import dev.faizksen.kursor.domain.SyncMissingRatesUseCase
import dev.faizksen.kursor.domain.SyncScheduler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MainViewModel(
    private val syncScheduler: SyncScheduler,
    private val refreshCatalogue: RefreshCatalogueUseCase,
    private val resolveMissingProviders: ResolveMissingProvidersUseCase,
    private val syncMissingRates: SyncMissingRatesUseCase,
): ViewModel() {

    fun scheduleSync() {
        syncScheduler.schedulePeriodicSync()
        launchIgnoringFailure { syncMissingRates() }
    }

    fun refreshCatalogue() {
        launchIgnoringFailure {
            refreshCatalogue.invoke()
            resolveMissingProviders()
        }
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
