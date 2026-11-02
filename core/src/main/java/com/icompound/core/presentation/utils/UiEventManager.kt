package com.icompound.core.presentation.utils

import androidx.navigationevent.NavigationEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class UiEventManager{

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _navigationEvent = Channel<NavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    // One-time UI messages (snack bar/toast)
    private val _uiMessage = MutableSharedFlow<MessageTypeUi>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val uiMessage: SharedFlow<MessageTypeUi> = _uiMessage.asSharedFlow()

    fun showLoading() {
        _isLoading.value = true
    }

    fun hideLoading() {
        _isLoading.value = false
    }

    suspend fun showMsg(message: String, type: UIMessageState = UIMessageState.NORMAL) {
        _uiMessage.emit(MessageTypeUi.Text(message, type = type))
    }

    suspend fun showMsg(messageResId: Int, type: UIMessageState = UIMessageState.NORMAL) {
        _uiMessage.emit(MessageTypeUi.Resource(messageResId, type = type))
    }

    suspend fun showDialog(
        title: String? = null,
        titleResId: Int? = null,
        message: String? = null,
        messageResId: Int? = null,
        positiveButtonText: String? = null,
        positiveButtonResId: Int? = null,
        onPositiveAction: (() -> Unit)? = null
    ) {
        _uiMessage.emit(
            MessageTypeUi.Dialog(
                title,
                titleResId,
                message,
                messageResId,
                positiveButtonText,
                positiveButtonResId,
                onPositiveAction
            )
        )
    }

}