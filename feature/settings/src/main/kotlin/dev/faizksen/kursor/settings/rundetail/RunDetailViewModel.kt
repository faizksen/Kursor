package dev.faizksen.kursor.settings.rundetail

import androidx.lifecycle.viewModelScope
import dev.faizksen.kursor.domain.ObserveRunAttemptsUseCase
import dev.faizksen.kursor.domain.ObserveRunUseCase
import dev.faizksen.kursor.model.ProviderAttempt
import dev.faizksen.kursor.model.SyncRun
import dev.faizksen.kursor.ui.BaseMviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class RunDetailViewModel(
    runId: Long,
    observeRun: ObserveRunUseCase,
    observeRunAttempts: ObserveRunAttemptsUseCase,
): BaseMviViewModel<RunDetailUiState, RunDetailUiAction, RunDetailUiEffect>(
    RunDetailUiState.Loading
) {
    init {
        combine(
            observeRun(runId),
            observeRunAttempts(runId),
        ) { run, attempts -> runDetail(run, attempts) }
            .onEach { state -> updateState { state } }
            .launchIn(viewModelScope)
    }

    override fun handleActions(action: RunDetailUiAction) = Unit
}

internal fun runDetail(run: SyncRun?, attempts: List<ProviderAttempt>): RunDetailUiState =
    if (run == null) {
        RunDetailUiState.Gone
    } else {
        RunDetailUiState.Content(run = run, attempts = attempts)
    }
