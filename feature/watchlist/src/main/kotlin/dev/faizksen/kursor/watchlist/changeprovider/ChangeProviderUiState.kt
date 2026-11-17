package dev.faizksen.kursor.watchlist.changeprovider

import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.Provider
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.ui.UiAction
import dev.faizksen.kursor.ui.UiEffect
import dev.faizksen.kursor.ui.UiState

sealed interface ChangeProviderUiState: UiState {
    data object Loading: ChangeProviderUiState

    data object Gone: ChangeProviderUiState

    data class Picking(
        val pair: CurrencyPair,
        val current: ProviderKey?,
        val selected: ProviderKey?,
        val providers: List<Provider>,
        val everyProviderCount: Int,
        val showingEveryProvider: Boolean,
    ): ChangeProviderUiState
}

sealed interface ChangeProviderUiAction: UiAction {
    data class ProviderPicked(val key: ProviderKey): ChangeProviderUiAction
    data object ShowEveryProviderClicked: ChangeProviderUiAction
    data object ConfirmClicked: ChangeProviderUiAction
}

sealed interface ChangeProviderUiEffect: UiEffect {
    data object Changed: ChangeProviderUiEffect
}
