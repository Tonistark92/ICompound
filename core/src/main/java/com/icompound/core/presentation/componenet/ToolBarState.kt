package com.icompound.core.presentation.componenet


import androidx.compose.ui.graphics.Color
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.icompound.core.R
import com.icompound.designsystem.ICompoundTheme


@Preview(showBackground = true)
@Composable
fun DefaultToolBarPreview() {
    ICompoundTheme {
        DefaultAppBar(
            state = ToolBarState.AuthTitleWithBack(
                title = "Title",
                onBackButtonClicked = {}
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultAppBar(
    state: ToolBarState,
    modifier: Modifier = Modifier
) {
    if (state is ToolBarState.Hidden) return

    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
//            DefaultText(
//                text = state.resolvedTitle(),
//                textAlign = TextAlign.Center,
//                textStyle = MaterialTheme.typography.titleLarge.copy(
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 16.sp
//                ),
//                textColor = if (state is ToolBarState.TitleWithNotification)
//                    MaterialTheme.colorScheme.primary
//                else
//                    MaterialTheme.colorScheme.onSurface
//            )
        },
        navigationIcon = {
            if (state.hasBackButton) {
                IconAction(
                    resId = R.drawable.ic_left_arrow,
                    contentDescription = "Back",
                    onClick = { state.onBackButtonClicked?.invoke() }
                )
            }
        },
        actions = {
            when (state) {
                is ToolBarState.TitleWithNotification -> {
//                    IconAction(
//                        resId = R.drawable.ic_notification,
//                        tint = MaterialTheme.colorScheme.primary,
//                        contentDescription = "Notifications",
//                        onClick = { state.onNotificationButtonClicked?.invoke() }
//                    )
                }

                is ToolBarState.TitleWithBackAndActions -> {
                    state.actions.forEach { action -> action() }
                }

                else -> Unit
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (state is ToolBarState.AuthTitleWithBack)
                MaterialTheme.colorScheme.background
            else
                MaterialTheme.colorScheme.surface,
            scrolledContainerColor = Color.Unspecified,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )

}

@Composable
private fun ToolBarState.resolvedTitle(): String {
    return when (this) {
        is ToolBarState.TitleWithBack ->
            title ?: titleResId?.let { stringResource(it) }.orEmpty()

        is ToolBarState.TitleWithNotification ->
            title ?: titleResId?.let { stringResource(it) }.orEmpty()

        is ToolBarState.AuthTitleWithBack ->
            title ?: titleResId?.let { stringResource(it) }.orEmpty()

        is ToolBarState.Title ->
            title ?: titleResId?.let { stringResource(it) }.orEmpty()

        else -> title.orEmpty()
    }
}

private val ToolBarState.hasBackButton: Boolean
    get() = this is ToolBarState.TitleWithBack ||
            this is ToolBarState.TitleWithNotification ||
            this is ToolBarState.AuthTitleWithBack ||
            this is ToolBarState.TitleWithBackAndActions

@Composable
private fun IconAction(
    @DrawableRes resId: Int,
    contentDescription: String,
    tint: Color? = null,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(id = resId),
            tint = null,
            contentDescription = contentDescription
        )
    }
}

sealed interface ToolbarCommonState {
    val title: String?
    val onBackButtonClicked: (() -> Unit)?
    val onNotificationButtonClicked: (() -> Unit)?
}

@Immutable
sealed class ToolBarState(
    override val title: String? = null,
    val _onBackButtonClicked: (() -> Unit)? = null,
    override val onNotificationButtonClicked: (() -> Unit)? = null,
) : ToolbarCommonState {
    override val onBackButtonClicked: (() -> Unit)? = _onBackButtonClicked

    fun updateBackButtonClicked(newOnBackButtonClicked: () -> Unit): ToolBarState {
        return when (this) {
            is TitleWithBack -> copy(onBackButtonClicked = newOnBackButtonClicked)
            is AuthTitleWithBack -> copy(onBackButtonClicked = newOnBackButtonClicked)
            is TitleWithBackAndActions -> copy(onBackButtonClicked = newOnBackButtonClicked)
            else -> this // Hidden state doesn't need back button updates
        }
    }

    data object Hidden : ToolBarState()

    data class Title(
        @field:StringRes val titleResId: Int? = null
    ) : ToolBarState()

    data class TitleWithBack(
        override val title: String? = null,
        @field:StringRes val titleResId: Int? = null,
        override val onBackButtonClicked: (() -> Unit)? = null,
    ) : ToolBarState()

    data class AuthTitleWithBack(
        override val title: String? = null,
        @field:StringRes val titleResId: Int? = null,
        override val onBackButtonClicked: (() -> Unit)? = null,
    ) : ToolBarState()

    data class TitleWithNotification(
        override val title: String? = null,
        @field:StringRes val titleResId: Int? = null,
        override val onNotificationButtonClicked: (() -> Unit)? = null,
        override val onBackButtonClicked: (() -> Unit)? = null,
    ) : ToolBarState()

    data class TitleWithBackAndActions(
        override val title: String,
        override val onBackButtonClicked: (() -> Unit)? = null,
        val actions: List<@Composable () -> Unit>,
    ) : ToolBarState()
}