package com.icompound.mainactivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigationevent.NavigationEvent
import com.icompound.core.presentation.utils.MessageTypeUi
import com.icompound.core.presentation.utils.getSnackbarColor
import com.icompound.designsystem.ICompoundTheme
import com.icompound.navigation.AppScaffold
import com.icompound.navigation.compose.LocalNavigator
import com.icompound.navigation.navigator.Navigator
import com.icompound.navigation.navigator.NavigatorImpl
import com.icompound.navigation.route.LoginNavKey
import com.icompound.navigation.route.SplashNavKey
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlin.getValue

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private var currentNavigator: Navigator? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ICompoundTheme {

                val isLoading by viewModel.isLoading.collectAsState()
                val snackbarHostState = remember {
                    SnackbarHostState()
                }

                var dialogMessage by remember {
                    mutableStateOf<String?>(null)
                }

                var snackbarColor by remember {
                    mutableStateOf(Color.Gray)
                }

                LaunchedEffect(viewModel, snackbarHostState) {
                    viewModel.uiMessages.collectLatest { message ->
                        when (message) {
                            is MessageTypeUi.Dialog -> {
                                dialogMessage = message.message ?: getString(message.messageResId!!)
                            }

                            is MessageTypeUi.Text -> {
                                snackbarColor =
                                    message.type.getSnackbarColor()

                                snackbarHostState.showSnackbar(
                                    message.message
                                )
                            }

                            is MessageTypeUi.Resource -> {
                                snackbarColor =
                                    message.type.getSnackbarColor()

                                snackbarHostState.showSnackbar(
                                    getString(message.messageResId)
                                )
                            }
                        }
                    }
                }

                // Create the Navigation 3 back stack and its navigator.
                val backStack = rememberNavBackStack(SplashNavKey)

                val navigator = remember(backStack) { NavigatorImpl(backStack) }

                // Make this exact navigator available to onNewIntent().
                SideEffect {
                    currentNavigator = navigator
                }

                // Process a notification deferred during the splash screen.
                LaunchedEffect(viewModel, navigator) {
                    snapshotFlow {
                        navigator.backStack.firstOrNull()
                    }.first { key ->
                        key != null && key !is SplashNavKey
                    }

                    val pendingEvent =
                        viewModel.consumePendingNavigationEvent()

                    if (
                        pendingEvent != null &&
                        navigator.backStack.firstOrNull() !is LoginNavKey
                    ) {
                        handleNavigationEvent(
                            event = pendingEvent,
                            navigator = navigator,
                        )
                    }
                }

                // Global dialogs
                dialogMessage?.let { dialog ->
//                    PriorityAlertDialog(
//                        title = dialog.title ?: stringResource(dialogز\ ?: R.string.alert),
//                        message = dialog.message ?: stringResource(
//                            dialog.messageResId ?: R.string.alert
//                        ),
//                        buttonText = dialog.positiveButtonText ?: stringResource(R.string.ok),
//                        onButtonClick = {
//                            dialog.onPositiveAction?.invoke()
//                            dialogMessage = null
//                        },
//                    )
                }

                // Provide the same navigator to the entire navigation UI.
                CompositionLocalProvider(
                    LocalNavigator provides navigator,
                ) {
                    AppScaffold(
                        navigator = navigator,
                        isLoading = isLoading,
                        snackbarHostState = snackbarHostState,
                        snackbarColor = snackbarColor,
                    )
                }
            }
        }

        // Preserve notification navigation during cold start.
//        if (savedInstanceState == null) {
//            parseIntent(intent)?.let { event ->
//                viewModel.setPendingNavigationEvent(event)
//                intent.action = null
//            }
//        }
    }

    private fun handleNavigationEvent(
        event: NavigationEvent,
        navigator: Navigator,
    ) {
        when (event) {
//            is NavigationEvent.NavigateToLogin -> {
//                navigator.navigateAndClearBackStack(LoginNavKey)
//            }
//
//            is NavigationEvent.NavigateToHome -> {
//                navigator.navigateAndClearBackStack(HomeNavKey)
//            }
        }
    }


}
