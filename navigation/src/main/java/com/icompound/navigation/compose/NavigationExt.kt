package com.icompound.navigation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.icompound.navigation.navigator.Navigator

/**
 * CompositionLocal for providing [Navigator] to child composables.
 *
 * Usage:
 * ```
 * CompositionLocalProvider(LocalNavigator provides navigator) {
 *     // Child composables can access navigator via LocalNavigator.current
 * }
 * ```
 */
val LocalNavigator =
    staticCompositionLocalOf<Navigator> {
        error("Navigator was not provided")
    }

/**
 * Generic display function for main navigation.
 *
 * This extension function wraps Navigation 3's NavDisplay with a generic API
 * that hides library-specific implementation details.
 *
 * @param modifier Modifier to apply to the navigation display
 * @param onBack Callback for back navigation (defaults to popBackStack)
 * @param content Entry provider scope for defining navigation entries
 */
@Composable
fun Navigator.Display(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { popBackStack() },
    content: EntryProviderScope<NavKey>.() -> Unit,
) {
    @Suppress("UNCHECKED_CAST")
    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        modifier = modifier,
        entryProvider = entryProvider(builder = content),
    )
}
