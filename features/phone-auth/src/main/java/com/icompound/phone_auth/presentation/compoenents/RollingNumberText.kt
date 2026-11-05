package com.icompound.phone_auth.presentation.compoenents

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.icompound.designsystem.ColorLightTokens

@Composable
fun RollingNumberText(
    value: Int,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    style: TextStyle = MaterialTheme.typography.bodySmall,
    color: Color = ColorLightTokens.TextHint,
) {
    val digits = value.coerceAtLeast(0).toString()

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (prefix.isNotEmpty()) Text(prefix, style = style, color = color)

        digits.forEachIndexed { i, _ ->
            // Key by position from the right, so the units digit keeps its own
            // animation state even when the number gets shorter (10 -> 9)
            key(digits.length - i) {
                AnimatedContent(
                    targetState = digits[i],
                    transitionSpec = {
                        // Value going up rolls up, value going down rolls down
                        val up = targetState > initialState
                        val enter = slideInVertically(tween(220)) { if (up) it else -it } +
                                fadeIn(tween(220))
                        val exit = slideOutVertically(tween(220)) { if (up) -it else it } +
                                fadeOut(tween(120))
                        enter togetherWith exit using SizeTransform(clip = false)
                    },
                    label = "digit"
                ) { c ->
                    Text(c.toString(), style = style, color = color)
                }
            }
        }

        if (suffix.isNotEmpty()) Text(suffix, style = style, color = color)
    }
}


// Resend timer
//RollingNumberText(secondsLeft, prefix = "Resend in ", suffix = "s")
//
//// Attempts left, in the error color
//RollingNumberText(
//attemptsLeft,
//prefix = "Incorrect code, ",
//suffix = " attempts left",
//color = MaterialTheme.colorScheme.error
//)
//
//// A different style, e.g. a big number on a dashboard card
//RollingNumberText(unreadCount, style = MaterialTheme.typography.titleLarge,
//color = MaterialTheme.colorScheme.onBackground)