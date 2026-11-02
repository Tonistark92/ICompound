package com.icompound.core.presentation.base.mvi

import com.icompound.core.presentation.base.BaseViewModel
import com.icompound.core.presentation.base.DispatcherProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class MVIViewModel<
        A : ViewAction,
        E : ViewEvent,
        S : ViewState
        >(
    initialState: S,
    dispatcherProvider: DispatcherProvider
) : BaseViewModel(dispatcherProvider) {

    // ------------------------------------------------------------------------
    // State
    // ------------------------------------------------------------------------

    private val _viewState = MutableStateFlow(initialState)

    val viewState: StateFlow<S> = _viewState.asStateFlow()

    val currentViewState: S
        get() = _viewState.value

    // ------------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------------

    private val actionChannel = Channel<A>(Channel.UNLIMITED)

    private val actionFlow: Flow<A> = actionChannel.receiveAsFlow()

    /**
     * Sends an action from the UI to the ViewModel.
     */
    fun processIntent(action: A) {
        check(actionChannel.trySend(action).isSuccess) {
            "Failed to emit action: $action"
        }
    }

    // ------------------------------------------------------------------------
    // Events
    // ------------------------------------------------------------------------

    private val eventChannel = Channel<E>(Channel.UNLIMITED)

    val singleEvent: Flow<E> = eventChannel.receiveAsFlow()

    /**
     * Sends a one-time event to the UI.
     */
    protected fun sendEvent(event: E) {
        check(eventChannel.trySend(event).isSuccess) {
            "Failed to emit event: $event"
        }
    }

    // ------------------------------------------------------------------------
    // State Updates
    // ------------------------------------------------------------------------

    protected fun setState(newState: S) {
        _viewState.value = newState
    }

    protected fun updateState(transform: S.() -> S) {
        _viewState.update { current ->
            current.transform()
        }
    }

    // ------------------------------------------------------------------------
    // Action Observation
    // ------------------------------------------------------------------------

    init {
        viewModelScopeWithHandler.launch {
            actionFlow.collect { action ->
                onActionTrigger(action)
            }
        }
    }

    protected abstract fun onActionTrigger(action: A)

    // ------------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------------

    override fun onCleared() {
        actionChannel.close()
        eventChannel.close()
        super.onCleared()
    }
}