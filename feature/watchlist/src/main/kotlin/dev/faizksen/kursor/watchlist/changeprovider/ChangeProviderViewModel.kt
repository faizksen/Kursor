package dev.faizksen.kursor.watchlist.changeprovider

import androidx.lifecycle.viewModelScope
import dev.faizksen.kursor.domain.ChangeProviderUseCase
import dev.faizksen.kursor.domain.ProviderCandidates
import dev.faizksen.kursor.domain.ProviderCandidatesUseCase
import dev.faizksen.kursor.domain.WatchedPairUseCase
import dev.faizksen.kursor.ui.BaseMviViewModel
import kotlinx.coroutines.launch

class ChangeProviderViewModel(
    private val pairId: Long,
    private val watchedPair: WatchedPairUseCase,
    private val providerCandidates: ProviderCandidatesUseCase,
    private val changeProvider: ChangeProviderUseCase,
): BaseMviViewModel<ChangeProviderUiState, ChangeProviderUiAction, ChangeProviderUiEffect>(
    ChangeProviderUiState.Loading
) {
    private var candidates: ProviderCandidates = ProviderCandidates(emptyList(), emptyList())

    init {
        viewModelScope.launch {
            val watched = watchedPair(pairId)

            if (watched == null) {
                updateState { ChangeProviderUiState.Gone }
                return@launch
            }

            candidates = providerCandidates(watched.pair)

            updateState {
                ChangeProviderUiState.Picking(
                    pair = watched.pair,
                    current = watched.provider,
                    selected = watched.provider,
                    providers = candidates.shortlist,
                    everyProviderCount = candidates.all.size,
                    showingEveryProvider = false,
                )
            }
        }
    }

    override fun handleActions(action: ChangeProviderUiAction) {
        when (action) {
            is ChangeProviderUiAction.ProviderPicked -> picking { copy(selected = action.key) }

            ChangeProviderUiAction.ShowEveryProviderClicked -> picking {
                copy(providers = candidates.all, showingEveryProvider = true)
            }

            ChangeProviderUiAction.ConfirmClicked -> confirm()
        }
    }

    private fun confirm() {
        val state = stateValue as? ChangeProviderUiState.Picking ?: return
        val provider = state.selected ?: return

        viewModelScope.launch {
            changeProvider(pairId, provider)
            sendEffect(ChangeProviderUiEffect.Changed)
        }
    }

    private fun picking(
        reducer: ChangeProviderUiState.Picking.() -> ChangeProviderUiState.Picking,
    ) {
        updateState { if (this is ChangeProviderUiState.Picking) reducer() else this }
    }
}
