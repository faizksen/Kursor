package dev.faizksen.kursor.watchlist

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.faizksen.kursor.domain.ObserveSyncLogUseCase
import dev.faizksen.kursor.domain.ObserveSyncStateUseCase
import dev.faizksen.kursor.domain.ObserveWatchlistUseCase
import dev.faizksen.kursor.domain.RequestManualSyncUseCase
import dev.faizksen.kursor.model.SyncResult
import dev.faizksen.kursor.model.SyncRun
import dev.faizksen.kursor.model.SyncState
import dev.faizksen.kursor.model.SyncTrigger
import dev.faizksen.kursor.model.WatchedPairWithRate
import dev.faizksen.kursor.ui.BaseMviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val watchlistUseCase: ObserveWatchlistUseCase,
    private val requestManualSync: RequestManualSyncUseCase,
    observeSyncState: ObserveSyncStateUseCase,
    observeSyncLog: ObserveSyncLogUseCase,
): BaseMviViewModel<WatchlistUiState, WatchlistUiAction, WatchlistUiEffect>(
    WatchlistUiState.Initial
) {
    init {
        combine(
            watchlistUseCase(),
            observeSyncLog(LAST_RUN_ONLY).map { runs -> runs.firstOrNull() },
            observeSyncState(SyncTrigger.Manual),
        ) { pairsWithRates, lastRun, syncState ->
            pairsWithRates.toUiState(lastUpdate(lastRun, syncState))
        }
            .onEach { state -> updateState { state } }
            .launchIn(viewModelScope)
    }

    override fun handleActions(action: WatchlistUiAction) {
        when(action) {
            WatchlistUiAction.RefreshClicked -> requestManualSync()
        }
    }

    private fun List<WatchedPairWithRate>.toUiState(lastUpdate: LastUpdate): WatchlistUiState =
        if (isEmpty()) {
            WatchlistUiState.Empty
        } else {
            WatchlistUiState.Results(lastUpdate = lastUpdate, pairs = this)
        }

    private companion object {
        const val LAST_RUN_ONLY = 1
    }
}

internal fun lastUpdate(lastRun: SyncRun?, syncState: SyncState): LastUpdate = when {
    isSyncing(lastRun, syncState) -> LastUpdate.Progress(0)
    lastRun == null -> LastUpdate.Empty
    lastRun.result == SyncResult.Success -> LastUpdate.Success(lastRun.finishedAt)
    else -> LastUpdate.Failed(
        failedAttempts = lastRun.attempt,
        lastTryAt = lastRun.finishedAt,
        willRetry = lastRun.result == SyncResult.Retry,
    )
}

private fun isSyncing(lastRun: SyncRun?, syncState: SyncState): Boolean = when (syncState) {
    SyncState.Running -> true
    SyncState.Scheduled -> lastRun == null || lastRun.result == SyncResult.Success
    SyncState.Idle, SyncState.Succeeded, SyncState.Failed -> false
}
