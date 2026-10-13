package dev.faizksen.kursor.watchlist

import dev.faizksen.kursor.model.WatchedPairWithRate
import dev.faizksen.kursor.ui.UiAction
import dev.faizksen.kursor.ui.UiEffect
import dev.faizksen.kursor.ui.UiState
import java.time.Instant

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
    data class Success(val updatedAt: Instant): LastUpdate
    data class Failed(
        val failedAttempts: Int,
        val lastTryAt: Instant,
        val willRetry: Boolean,
    ): LastUpdate
    data class Progress(
        val percents: Int
    ): LastUpdate
}

sealed interface WatchlistUiAction: UiAction {
    data object RefreshClicked: WatchlistUiAction
    data object MenuClicked: WatchlistUiAction
}

sealed interface WatchlistUiEffect: UiEffect {
}