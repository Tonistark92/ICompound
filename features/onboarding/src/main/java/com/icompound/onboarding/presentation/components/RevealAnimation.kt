package com.icompound.onboarding.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Reveal(
    visible: Boolean,
    index: Int, // 0, 1, 2... controls the delay
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val delay = index * 120
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter =
            fadeIn(tween(600, delayMillis = delay)) +
                slideInVertically(
                    animationSpec = tween(600, delayMillis = delay, easing = FastOutSlowInEasing),
                    initialOffsetY = { 60 },
                ),
        exit = fadeOut(tween(150)),
    ) { content() }
}
