package com.icompound.navigation.navigator

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KClass

class NavigatorImpl(
    override val backStack: NavBackStack<NavKey>,
) : Navigator {

    override fun navigate(route: NavKey) {
        backStack.add(route)
    }

    override fun navigateAndClearBackStack(route: NavKey) {
        backStack.clear()
        backStack.add(route)
    }

    override fun popUpTo(route: NavKey, inclusive: Boolean) {
        val targetIndex = backStack.indexOfLast { it == route }
        if (targetIndex == -1) return

        val elementsToRemove =
            backStack.size - targetIndex - if (inclusive) 0 else 1

        repeat(elementsToRemove.coerceAtLeast(0)) {
            backStack.removeLastOrNull()
        }
    }

    override fun dropAllOfType(
        routeClass: KClass<out NavKey>,
    ) {
        backStack.removeAll { route ->
            route::class == routeClass
        }
    }

    override fun replaceTop(route: NavKey) {
        if (backStack.isNotEmpty()) {
            backStack.removeLastOrNull()
        }
        backStack.add(route)
    }

    override fun popBackStack() {
        if (backStack.isNotEmpty()) {
            backStack.removeLastOrNull()
        }
    }

    override fun navigateUp() {
        if (canNavigateBack()) {
            backStack.removeLastOrNull()
        }
    }

    override fun canNavigateBack(): Boolean {
        return backStack.size > 1
    }
    override fun popToAndPush(
        target: NavKey,
        newKey: NavKey,
        inclusive: Boolean,
    ) {
        val targetIndex = backStack.indexOfLast { it == target }

        if (targetIndex == -1) {
            // Target is not in the stack: add it, then navigate.
            backStack.add(target)

            if (newKey != target) {
                backStack.add(newKey)
            }

            return
        }

        val destinationIndex = if (inclusive) {
            targetIndex - 1
        } else {
            targetIndex
        }

        while (backStack.lastIndex > destinationIndex) {
            backStack.removeAt(backStack.lastIndex)
        }

        if (inclusive && backStack.lastOrNull() == target) {
            backStack.removeAt(backStack.lastIndex)
        }

        if (backStack.lastOrNull() != newKey) {
            backStack.add(newKey)
        }
    }


}