package dev.faizksen.kursor.watchlist.pairdetail

import androidx.lifecycle.viewModelScope
import dev.faizksen.kursor.domain.ObservePairDetailUseCase
import dev.faizksen.kursor.ui.BaseMviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PairDetailViewModel(
    pairId: Long,
    observePairDetail: ObservePairDetailUseCase,
): BaseMviViewModel<PairDetailUiState, PairDetailUiAction, PairDetailUiEffect>(
    PairDetailUiState.Loading
) {

    init {
        observePairDetail(pairId)
            .onEach { detail ->
                updateState {
                    if (detail == null) {
                        PairDetailUiState.Gone
                    } else {
                        PairDetailUiState.Watching(
                            watched = detail.watched,
                            provider = detail.provider,
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleActions(action: PairDetailUiAction) = Unit
}
