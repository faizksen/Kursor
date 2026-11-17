package dev.faizksen.kursor.watchlist.addpair

import dev.faizksen.kursor.model.Currency
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.Provider
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.ui.UiAction
import dev.faizksen.kursor.ui.UiEffect
import dev.faizksen.kursor.ui.UiState

enum class AddPairStep {
    BASE,
    QUOTE,
    PROVIDER,
}

sealed interface AddPairUiState: UiState {
    data object Loading: AddPairUiState

    data object CatalogueTooOld: AddPairUiState

    data class Picking(
        val step: AddPairStep,
        val query: String,
        val currencies: List<Currency>,
        val base: CurrencyCode?,
        val quote: CurrencyCode?,
        val providers: List<Provider>,
        val everyProviderCount: Int,
        val showingEveryProvider: Boolean,
        val provider: ProviderKey?,
    ): AddPairUiState
}

sealed interface AddPairUiAction: UiAction {
    data class QueryChanged(val query: String): AddPairUiAction
    data class CurrencyPicked(val code: CurrencyCode): AddPairUiAction
    data class ProviderPicked(val key: ProviderKey): AddPairUiAction
    data object ShowEveryProviderClicked: AddPairUiAction
    data object BackClicked: AddPairUiAction
    data object ConfirmClicked: AddPairUiAction
}

sealed interface AddPairUiEffect: UiEffect {
    data object Added: AddPairUiEffect
    data object AlreadyWatched: AddPairUiEffect
    data object Dismissed: AddPairUiEffect
}
