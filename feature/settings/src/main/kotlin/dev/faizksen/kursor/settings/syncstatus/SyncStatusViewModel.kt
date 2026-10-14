package dev.faizksen.kursor.settings.syncstatus

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.faizksen.kursor.domain.ObserveSyncLogUseCase
import dev.faizksen.kursor.domain.ObserveSyncQueueUseCase
import dev.faizksen.kursor.domain.RequestManualSyncUseCase
import dev.faizksen.kursor.model.SyncTrigger
import dev.faizksen.kursor.ui.BaseMviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SyncStatusViewModel @Inject constructor(
    private val requestManualSync: RequestManualSyncUseCase,
    observeSyncLog: ObserveSyncLogUseCase,
    observeSyncQueue: ObserveSyncQueueUseCase,
): BaseMviViewModel<SyncStatusUiState, SyncStatusUiAction, SyncStatusUiEffect>(
    SyncStatusUiState.Loading
) {
    init {
        combine(
            observeSyncLog(HistoryLimit),
            observeSyncQueue(SyncTrigger.Periodic),
            observeSyncQueue(SyncTrigger.Manual),
        ) { runs, periodic, manual ->
            SyncStatusUiState.Content(
                periodic = periodic,
                manual = manual,
                runs = runs,
            )
        }
            .onEach { state -> updateState { state } }
            .launchIn(viewModelScope)
    }

    override fun handleActions(action: SyncStatusUiAction) {
        when (action) {
            SyncStatusUiAction.RunSyncClicked -> requestManualSync()
        }
    }

    private companion object {
        const val HistoryLimit = 50
    }
}
