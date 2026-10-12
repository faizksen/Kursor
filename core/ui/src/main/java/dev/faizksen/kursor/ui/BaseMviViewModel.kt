package dev.faizksen.kursor.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

abstract class BaseMviViewModel<State : UiState, Action : UiAction, Effect : UiEffect>(
    initialState: State,
): ViewModel() {

    private val _stateFlow: MutableStateFlow<State> = MutableStateFlow(initialState)
    val screenState: StateFlow<State> = _stateFlow.asStateFlow()

    protected val stateValue: State
        get() = _stateFlow.value

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    protected abstract fun handleActions(action: Action)

    protected open fun onHandleActionsError(action: Action, e: Throwable) {
        System.err.println("${this::class.simpleName}: handleActions crash on action=$action")
        e.printStackTrace()
    }

    fun obtainAction(action: Action) {
        handleActions(action)
    }

    protected fun updateState(reducer: State.() -> State) {
        _stateFlow.update { it.reducer() }
    }

    protected fun sendEffect(effect: Effect) {
        _effect.trySend(effect)
    }
}
