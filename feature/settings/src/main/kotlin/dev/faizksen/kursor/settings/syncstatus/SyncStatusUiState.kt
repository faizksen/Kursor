package dev.faizksen.kursor.settings.syncstatus

import dev.faizksen.kursor.model.SyncQueue
import dev.faizksen.kursor.model.SyncRun
import dev.faizksen.kursor.ui.UiAction
import dev.faizksen.kursor.ui.UiEffect
import dev.faizksen.kursor.ui.UiState

sealed interface SyncStatusUiState: UiState {
    data object Loading: SyncStatusUiState
    data class Content(
        val periodic: SyncQueue,
        val manual: SyncQueue,
        val runs: List<SyncRun>,
    ): SyncStatusUiState
}

sealed interface SyncStatusUiAction: UiAction {
    data object RunSyncClicked: SyncStatusUiAction
}

sealed interface SyncStatusUiEffect: UiEffect
