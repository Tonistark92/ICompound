package com.icompound.phone_auth.presentation.compoenents

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.icompound.designsystem.ColorLightTokens
import com.icompound.designsystem.ICompoundTheme
import com.icompound.phone_auth.domain.OtpStatus
import kotlinx.coroutines.delay


enum class OtpUiStatus { Idle, Error, Success }

@Composable
fun OtpField(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    status: OtpUiStatus = OtpUiStatus.Idle,
    locked: Boolean = false,
    autoFocus: Boolean = true,
    onComplete: (String) -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    var hasFocus by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val shakeX = remember { Animatable(0f) }
    val amp = with(LocalDensity.current) { 10.dp.toPx() }

    if (autoFocus) LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // Shake + haptic when the status becomes Error
    LaunchedEffect(status) {
        when (status) {
            OtpUiStatus.Error -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                shakeX.animateTo(0f, keyframes {
                    durationMillis = 420
                    (-amp) at 60
                    amp at 120
                    (-amp * .7f) at 180
                    (amp * .7f) at 240
                    (-amp * .4f) at 300
                    (amp * .4f) at 360
                    0f at 420
                })
            }
            OtpUiStatus.Success ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            else -> Unit
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BasicTextField(
            value = code,
            onValueChange = { input ->
                val digits = input.filter(Char::isDigit).take(length)
                if (digits != code) {
                    onCodeChange(digits)
                    if (digits.length == length) onComplete(digits)
                }
            },
            readOnly = locked || status != OtpUiStatus.Idle,
            modifier = modifier
                .focusRequester(focusRequester)
                .onFocusChanged { hasFocus = it.isFocused },
            // Lock input while the result animation plays
            enabled = status == OtpUiStatus.Idle,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = TextStyle(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            decorationBox = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { translationX = shakeX.value },
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    repeat(length) { index ->
                        OtpBox(
                            index = index,
                            char = code.getOrNull(index),
                            isActive = hasFocus && status == OtpUiStatus.Idle &&
                                    index == code.length.coerceAtMost(length - 1),
                            status = status,
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun OtpBox(
    index: Int,
    char: Char?,
    isActive: Boolean,
    status: OtpUiStatus,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)

    val lift = remember { Animatable(0f) }      // success wave
    val pop = remember { Animatable(1f) }       // digit pop-in
    val appear = remember { Animatable(0f) }    // entrance
    val liftPx = with(LocalDensity.current) { 8.dp.toPx() }

    // 4. Staggered entrance: each box starts 50 ms after the previous one
    LaunchedEffect(Unit) {
        delay(index * 50L)
        appear.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
    }

    // 1. Digit pop-in: runs every time the digit in this box changes
    LaunchedEffect(char) {
        if (char != null) {
            pop.snapTo(0.6f)
            pop.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
        }
    }

    // Success wave (unchanged)
    LaunchedEffect(status) {
        if (status == OtpUiStatus.Success) {
            delay(index * 70L)
            lift.animateTo(-liftPx, tween(140, easing = FastOutSlowInEasing))
            lift.animateTo(0f, spring(Spring.DampingRatioMediumBouncy))
        }
    }

    val targetBorder = when {
        status == OtpUiStatus.Error -> colors.error
        status == OtpUiStatus.Success -> ColorLightTokens.CompletedGreen
        isActive -> colors.primary
        char != null -> colors.primary.copy(alpha = .6f)
        else -> colors.outlineVariant
    }
    val borderColor by animateColorAsState(targetBorder, tween(200), label = "otpBorder")

    val container = when {
        status == OtpUiStatus.Error -> colors.errorContainer.copy(alpha = .35f)
        status == OtpUiStatus.Success -> ColorLightTokens.Success.copy(alpha = .35f)
        char != null -> colors.surface
        else -> ColorLightTokens.SurfaceContainer
    }
    val textColor = when (status) {
        OtpUiStatus.Error -> colors.error
        OtpUiStatus.Success -> ColorLightTokens.OnSuccess
        else -> colors.onBackground
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                alpha = appear.value
                translationY = lift.value + (1f - appear.value) * 24.dp.toPx()
            }
            .size(width = 46.dp, height = 56.dp)
            .background(container, shape)
            .border(if (isActive) 2.dp else 1.dp, borderColor, shape),
        contentAlignment = Alignment.Center
    ) {
        when {
            char != null -> Text(
                text = char.toString(),
                modifier = Modifier.graphicsLayer { scaleX = pop.value; scaleY = pop.value },
                color = textColor,
                style = MaterialTheme.typography.titleMedium
            )
            // 2. Blinking caret in the box waiting for the next digit
            isActive -> BlinkingCaret(colors.primary)
            else -> Text(
                text = "0",
                color = colors.onSurface.copy(alpha = .15f),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun BlinkingCaret(color: Color) {
    val alpha by rememberInfiniteTransition(label = "caret").animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(530), RepeatMode.Reverse),
        label = "caretAlpha"
    )
    Box(
        Modifier
            .size(2.dp, 24.dp)
            .graphicsLayer { this.alpha = alpha }
            .background(color, RoundedCornerShape(1.dp))
    )
}


@Preview(
    name = "OTP - All States",
    showBackground = true,
    widthDp = 380,
    heightDp = 480
)
@Composable
private fun OtpFieldAllStatesPreview() {
    ICompoundTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            OtpPreviewItem(
                title = "Idle - Empty",
                code = "",
                status = OtpUiStatus.Idle
            )

            OtpPreviewItem(
                title = "Idle - Partially Filled",
                code = "123",
                status = OtpUiStatus.Idle
            )

            OtpPreviewItem(
                title = "Idle - Completed",
                code = "123456",
                status = OtpUiStatus.Idle
            )

            OtpPreviewItem(
                title = "Error",
                code = "123456",
                status = OtpUiStatus.Error
            )

            OtpPreviewItem(
                title = "Success",
                code = "123456",
                status = OtpUiStatus.Success
            )
        }
    }
}

@Composable
private fun OtpPreviewItem(
    title: String,
    code: String,
    status: OtpUiStatus
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge
        )

        OtpField(
            code = code,
            onCodeChange = {},
            autoFocus = false,
            status = status
        )
    }
}