package com.icompound.core.presentation.base.mvvm

import com.icompound.core.presentation.base.BaseViewModel
import com.icompound.core.presentation.base.DispatcherProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class MVVMViewModel<S : Any>(
    initialState: S,
    dispatcherProvider: DispatcherProvider
) : BaseViewModel(dispatcherProvider) {

    // ------------------------------------------------------------------------
    // State
    // ------------------------------------------------------------------------

    private val _uiState = MutableStateFlow(initialState)

    val uiState: StateFlow<S> = _uiState.asStateFlow()

    val currentState: S
        get() = _uiState.value

    // ------------------------------------------------------------------------
    // State Updates
    // ------------------------------------------------------------------------

    protected fun setState(newState: S) {
        _uiState.value = newState
    }

    protected fun updateState(reduce: S.() -> S) {
        _uiState.update { current ->
            current.reduce()
        }
    }
}