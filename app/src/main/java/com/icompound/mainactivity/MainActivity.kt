package com.icompound.mainactivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.icompound.navigation.route.OnBoardingNavKey
import com.icompound.navigation.route.SplashNavKey
import kotlin.getValue
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ICompoundTheme {
                val isLoading by viewModel.isLoading.collectAsState()
                val snackBarHostState = remember {
                    SnackbarHostState()
                }

                var dialogMessage by remember {
                    mutableStateOf<String?>(null)
                }

                var snackBarColor by remember {
                    mutableStateOf(Color.Gray)
                }

                LaunchedEffect(viewModel, snackBarHostState) {
                    viewModel.uiMessages.collectLatest { message ->
                        when (message) {
                            is MessageTypeUi.Dialog -> {
                                dialogMessage = message.message ?: getString(message.messageResId!!)
                            }

                            is MessageTypeUi.Text -> {
                                snackBarColor =
                                    message.type.getSnackbarColor()

                                snackBarHostState.showSnackbar(
                                    message.message,
                                )
                            }

                            is MessageTypeUi.Resource -> {
                                snackBarColor =
                                    message.type.getSnackbarColor()

                                snackBarHostState.showSnackbar(
                                    getString(message.messageResId),
                                )
                            }
                        }
                    }
                }

                // Create the Navigation 3 back stack and its navigator.
                val backStack = rememberNavBackStack(OnBoardingNavKey)

                val navigator = remember(backStack) { NavigatorImpl(backStack) }

                // Process a notification deferred during the splash screen.
//                LaunchedEffect(viewModel, navigator) {
//                    snapshotFlow {
//                        navigator.backStack.firstOrNull()
//                    }.first { key ->
//                        key != null && key !is SplashNavKey
//                    }
//
//                    val pendingEvent =
//                        viewModel.consumePendingNavigationEvent()
//
//                    if (
//                        pendingEvent != null &&
//                        navigator.backStack.firstOrNull() !is LoginNavKey
//                    ) {
//                        handleNavigationEvent(
//                            event = pendingEvent,
//                            navigator = navigator,
//                        )
//                    }
//                }

                // Global dialogs
                dialogMessage?.let { dialog ->
//                    PriorityAlertDialog(
//                        title = dialog.title ?: stringResource(dialog\ ?: R.string.alert),
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
                        snackBarHostState = snackBarHostState,
                        snackBarColor = snackBarColor,
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

    private fun handleNavigationEvent(event: NavigationEvent, navigator: Navigator) {
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
