package com.icompound.designsystem

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class KaiTechAppSpacing(
    val extraSmall: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp,
)

val KaiTechSpacing =
    KaiTechAppSpacing(
        extraSmall = 4.dp,
        small = 8.dp,
        medium = 16.dp,
        large = 24.dp,
        extraLarge = 32.dp,
    )

internal val LocalKaiTechAppSpacing = staticCompositionLocalOf { KaiTechSpacing }
