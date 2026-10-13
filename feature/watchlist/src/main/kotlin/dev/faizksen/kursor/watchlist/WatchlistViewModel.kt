package dev.faizksen.kursor.watchlist

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.faizksen.kursor.domain.ObserveSyncStateUseCase
import dev.faizksen.kursor.domain.ObserveWatchlistUseCase
import dev.faizksen.kursor.domain.RequestManualSyncUseCase
import dev.faizksen.kursor.model.SyncState
import dev.faizksen.kursor.model.SyncTrigger
import dev.faizksen.kursor.ui.BaseMviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.Clock
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val watchlistUseCase: ObserveWatchlistUseCase,
    private val requestManualSync: RequestManualSyncUseCase,
    private val observeSyncState: ObserveSyncStateUseCase,
    private val clock: Clock,
): BaseMviViewModel<WatchlistUiState, WatchlistUiAction, WatchlistUiEffect>(
    WatchlistUiState.Initial
) {
    init {
        watchlistUseCase()
            .onEach { pairsWithRates ->
                updateState {
                    if (pairsWithRates.isEmpty()) {
                        WatchlistUiState.Empty
                    } else {
                        WatchlistUiState.Results(
                            lastUpdate = currentLastUpdate(),
                            pairs = pairsWithRates
                        )
                    }
                }
            }
            .launchIn(viewModelScope)

        observeSyncState(SyncTrigger.Manual)
            .onEach { syncState ->
                updateLastUpdate { previous -> previous.reduce(syncState) }
            }
            .launchIn(viewModelScope)
    }

    override fun handleActions(action: WatchlistUiAction) {
        when(action) {
            WatchlistUiAction.RefreshClicked -> requestManualSync()
            WatchlistUiAction.MenuClicked -> showMenu()
        }
    }

    private fun showMenu() {

    }

    private fun updateLastUpdate(reducer: (LastUpdate) -> LastUpdate) {
        updateState {
            when (this) {
                is WatchlistUiState.Results -> copy(lastUpdate = reducer(lastUpdate))
                WatchlistUiState.Initial, WatchlistUiState.Empty -> this
            }
        }
    }

    private fun LastUpdate.reduce(syncState: SyncState): LastUpdate = when (syncState) {
        SyncState.Scheduled, SyncState.Running -> LastUpdate.Progress(0)
        SyncState.Succeeded -> LastUpdate.Success(clock.instant())
        SyncState.Failed -> LastUpdate.Failed(
            failedAttempts = failedAttempts() + 1,
            lastTryAt = clock.instant(),
        )
        SyncState.Idle -> this
    }

    private fun WatchlistUiState.currentLastUpdate(): LastUpdate =
        (this as? WatchlistUiState.Results)?.lastUpdate ?: LastUpdate.Empty

    private fun LastUpdate.failedAttempts(): Int =
        (this as? LastUpdate.Failed)?.failedAttempts ?: 0
}
