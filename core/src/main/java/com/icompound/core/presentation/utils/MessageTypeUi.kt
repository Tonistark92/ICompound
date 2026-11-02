package com.icompound.core.presentation.utils

import androidx.compose.ui.graphics.Color

sealed class MessageTypeUi {
    data class Text(val message: String, val type: UIMessageState = UIMessageState.NORMAL) :
        MessageTypeUi()

    data class Resource(val messageResId: Int, val type: UIMessageState = UIMessageState.NORMAL) :
        MessageTypeUi()

    data class Dialog(
        val title: String? = null,
        val titleResId: Int? = null,
        val message: String? = null,
        val messageResId: Int? = null,
        val positiveButtonText: String? = null,
        val positiveButtonResId: Int? = null,
        val onPositiveAction: (() -> Unit)? = null
    ) : MessageTypeUi()
}

enum class UIMessageState {
    SUCCESS, ERROR, NORMAL
}

fun UIMessageState.getSnackbarColor(): Color = when (this) {
    UIMessageState.SUCCESS -> Color.Green
    UIMessageState.ERROR -> Color.Red
    UIMessageState.NORMAL -> Color.Gray
}