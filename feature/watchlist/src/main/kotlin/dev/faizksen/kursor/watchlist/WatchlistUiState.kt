package dev.faizksen.kursor.watchlist

import dev.faizksen.kursor.model.WatchedPair
import dev.faizksen.kursor.model.WatchedPairWithRate
import dev.faizksen.kursor.ui.UiAction
import dev.faizksen.kursor.ui.UiEffect
import dev.faizksen.kursor.ui.UiState
import java.time.LocalDate

sealed interface WatchlistUiState: UiState {
    data object Initial: WatchlistUiState
    data object Empty: WatchlistUiState
    data class Results(
        val lastUpdate: LastUpdate,
        val pairs: List<WatchedPairWithRate>
    ): WatchlistUiState
}

sealed interface LastUpdate {
    data object Empty: LastUpdate
    data class Success(val updatedAt: LocalDate): LastUpdate
    data class Failed(
        val failedAttempts: Int,
        val lastTryAt: LocalDate
    ): LastUpdate
    data class Progress(
        val percents: Int
    ): LastUpdate
}

sealed interface WatchlistUiAction: UiAction {
    data object AddNewPairClicked: WatchlistUiAction
    data object RefreshClicked: WatchlistUiAction
    data object MenuClicked: WatchlistUiAction
    // ToDo надо сделать так чтобы можно было выбрать из какого источника брать курс: Дефолт и список доступных, в которых есть эта пара
    data class PairClicked(val pair: WatchedPair): WatchlistUiAction
}

sealed interface WatchlistUiEffect: UiEffect {

}