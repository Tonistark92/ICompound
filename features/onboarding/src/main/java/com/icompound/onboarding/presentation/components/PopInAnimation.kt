package com.icompound.onboarding.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PopIn(
    visible: Boolean,
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val delay = 300 + index * 90 // after the title
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter =
            fadeIn(tween(400, delayMillis = delay)) +
                scaleIn(
                    initialScale = 0.9f,
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                ),
    ) { content() }
}
