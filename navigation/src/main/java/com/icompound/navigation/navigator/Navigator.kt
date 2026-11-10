package com.icompound.navigation.navigator

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KClass

interface Navigator {
    val backStack: NavBackStack<NavKey>

    fun dropAllOfType(routeClass: KClass<out NavKey>)

    fun replaceTop(route: NavKey)

    fun popToAndPush(
        target: NavKey,
        newKey: NavKey,
        inclusive: Boolean = false,
    )

    fun navigate(route: NavKey)

    fun navigateAndClearBackStack(route: NavKey)

    fun popBackStack()

    fun navigateUp()

    fun canNavigateBack(): Boolean

    fun popUpTo(
        route: NavKey,
        inclusive: Boolean = false,
    )
}
