package com.icompound.designsystem

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class KaiTechAppRadius(
    val none: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp,
    val full: Dp,
)

val KaiTechRadius = KaiTechAppRadius(
    none = 0.dp,
    small = 4.dp,
    medium = 8.dp,
    large = 12.dp,
    extraLarge = 20.dp,
    full = 999.dp,
)

internal val LocalKaiTechAppRadius= staticCompositionLocalOf { KaiTechRadius }