package dev.faizksen.kursor.watchlist

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.faizksen.kursor.domain.ObserveWatchlistUseCase
import dev.faizksen.kursor.domain.RefreshRatesUseCase
import dev.faizksen.kursor.model.SyncOutcome
import dev.faizksen.kursor.ui.BaseMviViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val watchlistUseCase: ObserveWatchlistUseCase,
    private val refreshRatesUseCase: RefreshRatesUseCase,
    private val clock: Clock,
): BaseMviViewModel<WatchlistUiState, WatchlistUiAction, WatchlistUiEffect>(
    WatchlistUiState.Initial
) {
    private var refreshJob: Job? = null

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
    }

    override fun handleActions(action: WatchlistUiAction) {
        when(action) {
            WatchlistUiAction.RefreshClicked -> refresh()
            WatchlistUiAction.MenuClicked -> showMenu()
        }
    }

    private fun refresh() {
        if (refreshJob?.isActive == true) return

        refreshJob = viewModelScope.launch {
            updateLastUpdate { LastUpdate.Progress(0) }

            val outcome = refreshRatesUseCase()

            updateLastUpdate { previous ->
                when (outcome) {
                    is SyncOutcome.Success -> LastUpdate.Success(clock.instant())

                    is SyncOutcome.Transient,
                    is SyncOutcome.Permanent,
                    -> LastUpdate.Failed(
                        failedAttempts = previous.failedAttempts() + 1,
                        lastTryAt = clock.instant(),
                    )
                }
            }
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

    private fun WatchlistUiState.currentLastUpdate(): LastUpdate =
        (this as? WatchlistUiState.Results)?.lastUpdate ?: LastUpdate.Empty

    private fun LastUpdate.failedAttempts(): Int =
        (this as? LastUpdate.Failed)?.failedAttempts ?: 0
}
