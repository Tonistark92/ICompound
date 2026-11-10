package com.icompound.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.icompound.core.presentation.componenet.DefaultAppBar
import com.icompound.core.presentation.componenet.ToolBarState
import com.icompound.navigation.navigator.Navigator
import com.icompound.navigation.route.NavScreen
import com.icompound.navigation.route.bottomBarItems
import com.icompound.navigation.route.bottomnav.BottomAppBar

@Composable
fun AppScaffold(
    navigator: Navigator,
    isLoading: Boolean,
    snackBarHostState: SnackbarHostState,
    snackBarColor: Color = Color.Gray,
) {
    val currentKey: NavKey? = navigator.backStack.lastOrNull()
    val currentScreen = currentKey as? NavScreen

    val showBottomBar by remember(currentKey) {
        derivedStateOf {
            currentKey != null &&
                bottomBarItems.any { item ->
                    item.navScreen::class == currentKey::class
                }
        }
    }

    val toolbar = currentScreen?.toolBarState
        ?: ToolBarState.Hidden

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                val updatedToolbar =
                    if (toolbar.onBackButtonClicked == null) {
                        toolbar.updateBackButtonClicked {
                            navigator.popBackStack()
                        }
                    } else {
                        toolbar
                    }

                DefaultAppBar(updatedToolbar)
            },
            bottomBar = {
                if (showBottomBar && currentScreen != null) {
                    BottomAppBar(
                        currentKey = currentScreen,
                        items = bottomBarItems,
                    ) { item ->
                        navigator.popToAndPush(
                            target = item.navScreen,
                            newKey = item.navScreen,
                        )
                    }
                }
            },
        ) { innerPadding ->
            val contentPadding = PaddingValues(
                top = if (currentScreen?.hasTopPadding == true) {
                    innerPadding.calculateTopPadding()
                } else {
                    0.dp
                },
                bottom = if (currentScreen?.hasBottomPadding == true) {
                    innerPadding.calculateBottomPadding()
                } else {
                    0.dp
                },
            )

            Navigation(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
                navigator = navigator,
            )
        }

        // Global loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent().changes.forEach { change ->
                                    change.consume()
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        // Global snackbar for messages
        SnackbarHost(
            hostState = snackBarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 26.dp),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = snackBarColor,
                shape = RoundedCornerShape(16.dp),
                contentColor = Color.White,
                modifier = Modifier.padding(14.dp),
            )
        }
    }
}
