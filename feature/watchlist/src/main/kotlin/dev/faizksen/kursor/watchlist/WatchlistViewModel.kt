package dev.faizksen.kursor.watchlist

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.faizksen.kursor.domain.ObserveWatchlistUseCase
import dev.faizksen.kursor.ui.BaseMviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val watchlistUseCase: ObserveWatchlistUseCase,
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
                            lastUpdate = LastUpdate.Empty,
                            pairs = pairsWithRates
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleActions(action: WatchlistUiAction) {
        when(action) {
            WatchlistUiAction.AddNewPairClicked -> TODO()
            WatchlistUiAction.MenuClicked -> TODO()
            is WatchlistUiAction.PairClicked -> TODO()
            WatchlistUiAction.RefreshClicked -> TODO()
        }
    }

}