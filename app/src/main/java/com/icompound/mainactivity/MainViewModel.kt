package com.icompound.mainactivity

import androidx.lifecycle.ViewModel
import androidx.navigationevent.NavigationEvent
import com.icompound.core.presentation.utils.UiEventManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(
    private val uiEventManager: UiEventManager,
) : ViewModel() {

    // Observe general application UI state.
    val isLoading = uiEventManager.isLoading
    val uiMessages = uiEventManager.uiMessage

    // Events emitted by uiEventManager and handled by MainActivity.
    val navigationEvents = uiEventManager.navigationEvent

    // Store notification navigation while splash is active.
    private val _pendingNavigationEvent =
        MutableStateFlow<NavigationEvent?>(null)

    val pendingNavigationEvent =
        _pendingNavigationEvent.asStateFlow()


    fun setPendingNavigationEvent(event: NavigationEvent) {
        _pendingNavigationEvent.value = event
    }

    fun consumePendingNavigationEvent(): NavigationEvent? {
        val event = _pendingNavigationEvent.value
        _pendingNavigationEvent.value = null
        return event
    }

}