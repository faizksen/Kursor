package dev.faizksen.kursor.watchlist.addpair

import androidx.lifecycle.viewModelScope
import dev.faizksen.kursor.domain.AddPairOutcome
import dev.faizksen.kursor.domain.AddWatchedPairUseCase
import dev.faizksen.kursor.domain.PickableCurrenciesUseCase
import dev.faizksen.kursor.domain.ProviderCandidates
import dev.faizksen.kursor.domain.ProviderCandidatesUseCase
import dev.faizksen.kursor.model.CurrencyCode
import dev.faizksen.kursor.model.CurrencyPair
import dev.faizksen.kursor.model.ProviderKey
import dev.faizksen.kursor.ui.BaseMviViewModel
import kotlinx.coroutines.launch

class AddPairViewModel(
    private val pickableCurrencies: PickableCurrenciesUseCase,
    private val providerCandidates: ProviderCandidatesUseCase,
    private val addWatchedPair: AddWatchedPairUseCase,
): BaseMviViewModel<AddPairUiState, AddPairUiAction, AddPairUiEffect>(AddPairUiState.Loading) {

    private var candidates: ProviderCandidates = ProviderCandidates(emptyList(), emptyList())

    init {
        viewModelScope.launch {
            val pickable = pickableCurrencies()

            updateState {
                if (pickable.tooOldToPick) {
                    AddPairUiState.CatalogueTooOld
                } else {
                    AddPairUiState.Picking(
                        step = AddPairStep.BASE,
                        query = "",
                        currencies = pickable.currencies,
                        base = null,
                        quote = null,
                        providers = emptyList(),
                        everyProviderCount = 0,
                        showingEveryProvider = false,
                        provider = null,
                    )
                }
            }
        }
    }

    override fun handleActions(action: AddPairUiAction) {
        when (action) {
            is AddPairUiAction.QueryChanged -> picking { copy(query = action.query) }
            is AddPairUiAction.CurrencyPicked -> pickCurrency(action.code)
            is AddPairUiAction.ProviderPicked -> picking { copy(provider = action.key) }
            AddPairUiAction.ShowEveryProviderClicked -> showEveryProvider()
            AddPairUiAction.BackClicked -> goBack()
            AddPairUiAction.ConfirmClicked -> confirm()
        }
    }

    private fun pickCurrency(code: CurrencyCode) {
        val state = stateValue as? AddPairUiState.Picking ?: return

        when (state.step) {
            AddPairStep.BASE -> picking {
                copy(step = AddPairStep.QUOTE, query = "", base = code, quote = null)
            }
            AddPairStep.QUOTE -> loadProviders(base = state.base ?: return, quote = code)
            AddPairStep.PROVIDER -> Unit
        }
    }

    private fun loadProviders(base: CurrencyCode, quote: CurrencyCode) {
        viewModelScope.launch {
            candidates = providerCandidates(CurrencyPair(base = base, quote = quote))

            picking {
                copy(
                    step = AddPairStep.PROVIDER,
                    query = "",
                    quote = quote,
                    providers = candidates.shortlist,
                    everyProviderCount = candidates.all.size,
                    showingEveryProvider = false,
                    provider = candidates.shortlist.firstOrNull()?.key,
                )
            }
        }
    }

    private fun showEveryProvider() {
        picking { copy(providers = candidates.all, showingEveryProvider = true) }
    }

    private fun goBack() {
        val state = stateValue as? AddPairUiState.Picking ?: run {
            sendEffect(AddPairUiEffect.Dismissed)
            return
        }

        when (state.step) {
            AddPairStep.BASE -> sendEffect(AddPairUiEffect.Dismissed)
            AddPairStep.QUOTE -> picking {
                copy(step = AddPairStep.BASE, query = "", base = null)
            }
            AddPairStep.PROVIDER -> picking {
                copy(
                    step = AddPairStep.QUOTE,
                    query = "",
                    quote = null,
                    providers = emptyList(),
                    everyProviderCount = 0,
                    showingEveryProvider = false,
                    provider = null,
                )
            }
        }
    }

    private fun confirm() {
        val state = stateValue as? AddPairUiState.Picking ?: return
        val base = state.base ?: return
        val quote = state.quote ?: return
        val provider = state.provider ?: return

        viewModelScope.launch {
            val outcome = addWatchedPair(CurrencyPair(base = base, quote = quote), provider)

            sendEffect(
                when (outcome) {
                    AddPairOutcome.ADDED -> AddPairUiEffect.Added
                    AddPairOutcome.ALREADY_WATCHED -> AddPairUiEffect.AlreadyWatched
                }
            )
        }
    }

    private fun picking(reducer: AddPairUiState.Picking.() -> AddPairUiState.Picking) {
        updateState { if (this is AddPairUiState.Picking) reducer() else this }
    }
}
