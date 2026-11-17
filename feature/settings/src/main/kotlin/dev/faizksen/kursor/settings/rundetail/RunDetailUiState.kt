package dev.faizksen.kursor.settings.rundetail

import dev.faizksen.kursor.model.ProviderAttempt
import dev.faizksen.kursor.model.SyncRun
import dev.faizksen.kursor.ui.UiAction
import dev.faizksen.kursor.ui.UiEffect
import dev.faizksen.kursor.ui.UiState

sealed interface RunDetailUiState: UiState {
    data object Loading: RunDetailUiState

    data object Gone: RunDetailUiState

    data class Content(
        val run: SyncRun,
        val attempts: List<ProviderAttempt>,
    ): RunDetailUiState
}

sealed interface RunDetailUiAction: UiAction

sealed interface RunDetailUiEffect: UiEffect
