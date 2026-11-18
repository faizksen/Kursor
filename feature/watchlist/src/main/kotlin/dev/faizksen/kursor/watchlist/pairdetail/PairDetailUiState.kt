package dev.faizksen.kursor.watchlist.pairdetail

import dev.faizksen.kursor.model.Provider
import dev.faizksen.kursor.model.WatchedPairWithRate
import dev.faizksen.kursor.ui.UiAction
import dev.faizksen.kursor.ui.UiEffect
import dev.faizksen.kursor.ui.UiState

sealed interface PairDetailUiState: UiState {
    data object Loading: PairDetailUiState

    data object Gone: PairDetailUiState

    data class Watching(
        val watched: WatchedPairWithRate,
        val provider: Provider?,
    ): PairDetailUiState
}

sealed interface PairDetailUiAction: UiAction {
    data object DeleteClicked: PairDetailUiAction
}

sealed interface PairDetailUiEffect: UiEffect {
    data object Deleted: PairDetailUiEffect
}
