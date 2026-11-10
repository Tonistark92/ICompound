package com.icompound.navigation.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun calculateIndicatorOffset(
    selectedIndex: Int,
    totalItems: Int,
): Dp {
    val density = LocalDensity.current
    val screenWidth = with(density) { LocalConfiguration.current.screenWidthDp.dp }
    val itemWidth = screenWidth / totalItems
    return itemWidth * selectedIndex
}

@Composable
internal fun calculateIndicatorWidth(totalItems: Int): Dp {
    val density = LocalDensity.current
    val screenWidth = with(density) { LocalConfiguration.current.screenWidthDp.dp }
    return (screenWidth / totalItems)
}
