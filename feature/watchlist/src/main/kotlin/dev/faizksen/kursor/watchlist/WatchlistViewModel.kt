package dev.faizksen.kursor.watchlist

import androidx.lifecycle.viewModelScope
import dev.faizksen.kursor.domain.ForgetDeletedPairUseCase
import dev.faizksen.kursor.domain.ObserveDefaultsSeededUseCase
import dev.faizksen.kursor.domain.ObserveDeletedPairUseCase
import dev.faizksen.kursor.domain.ObserveSyncLogUseCase
import dev.faizksen.kursor.domain.ObserveSyncStateUseCase
import dev.faizksen.kursor.domain.ObserveWatchlistUseCase
import dev.faizksen.kursor.domain.RequestManualSyncUseCase
import dev.faizksen.kursor.domain.UndoDeletedPairUseCase
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.SyncResult
import dev.faizksen.kursor.model.SyncRun
import dev.faizksen.kursor.model.SyncState
import dev.faizksen.kursor.model.SyncTrigger
import dev.faizksen.kursor.model.WatchedPairWithRate
import dev.faizksen.kursor.ui.BaseMviViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WatchlistViewModel(
    private val watchlistUseCase: ObserveWatchlistUseCase,
    private val requestManualSync: RequestManualSyncUseCase,
    private val undoDeletedPair: UndoDeletedPairUseCase,
    private val forgetDeletedPair: ForgetDeletedPairUseCase,
    observeDeletedPair: ObserveDeletedPairUseCase,
    observeSyncState: ObserveSyncStateUseCase,
    observeSyncLog: ObserveSyncLogUseCase,
    observeDefaultsSeeded: ObserveDefaultsSeededUseCase,
): BaseMviViewModel<WatchlistUiState, WatchlistUiAction, WatchlistUiEffect>(
    WatchlistUiState.Loading
) {
    val deletedPair: StateFlow<CurrencyPair?> = observeDeletedPair()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE_MILLIS), null)

    init {
        combine(
            watchlistUseCase(),
            observeSyncLog(LAST_RUN_ONLY).map { runs -> runs.firstOrNull() },
            observeSyncState(SyncTrigger.Manual),
            observeDefaultsSeeded(),
        ) { pairsWithRates, lastRun, syncState, seeded ->
            pairsWithRates.toUiState(lastUpdate(lastRun, syncState), seeded)
        }
            .onEach { state -> updateState { state } }
            .launchIn(viewModelScope)
    }

    override fun handleActions(action: WatchlistUiAction) {
        when(action) {
            WatchlistUiAction.RefreshClicked -> requestManualSync()
            WatchlistUiAction.UndoDeleteClicked -> viewModelScope.launch { undoDeletedPair() }
            WatchlistUiAction.DeleteNoticeDismissed -> forgetDeletedPair()
        }
    }

    private fun List<WatchedPairWithRate>.toUiState(
        lastUpdate: LastUpdate,
        seeded: Boolean,
    ): WatchlistUiState = when {
        isNotEmpty() -> WatchlistUiState.Results(lastUpdate = lastUpdate, pairs = this)
        seeded -> WatchlistUiState.Empty
        else -> WatchlistUiState.Loading
    }

    private companion object {
        const val LAST_RUN_ONLY = 1
        const val SUBSCRIPTION_GRACE_MILLIS = 5_000L
    }
}

internal fun lastUpdate(lastRun: SyncRun?, syncState: SyncState): LastUpdate = when {
    isSyncing(lastRun, syncState) -> LastUpdate.Progress(0)
    lastRun == null -> LastUpdate.Empty
    lastRun.result == SyncResult.Success -> LastUpdate.Success(
        updatedAt = lastRun.finishedAt,
        updatedCount = lastRun.updatedCount,
        requestedCount = lastRun.requestedCount,
        runId = lastRun.id,
    )
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
