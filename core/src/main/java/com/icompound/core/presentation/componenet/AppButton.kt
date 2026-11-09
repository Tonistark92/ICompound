package com.icompound.core.presentation.componenet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.dp
import com.icompound.designsystem.ICompoundTheme


@Composable
@ReadOnlyComposable
fun contentColorForContainer(container: Color): Color {
    val ext = ICompoundTheme.colors
    val scheme = MaterialTheme.colorScheme
    return when (container) {
        ext.warning -> ext.onWarning
        ext.success -> ext.onSuccess
        ext.info -> ext.onInfo
        else -> scheme.contentColorFor(container).takeOrElse {
            if (container.luminance() < 0.5f) Color.White else scheme.onSurface
        }
    }
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = contentColorForContainer(containerColor),  // override only if needed
    borderColor: Color? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    // Disabled look from your image: tinted container, muted text of the same hue
    val darkContainer = containerColor.luminance() < 0.5f
    val disabledContainer = containerColor.copy(alpha = .12f)
        .compositeOver(MaterialTheme.colorScheme.background)
    val disabledContent = (if (darkContainer) containerColor else contentColor).copy(alpha = .45f)

    Button(
        onClick = { if (!loading) onClick() },   // loading keeps normal colors
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        border = borderColor?.let { BorderStroke(1.dp, if (enabled) it else it.copy(alpha = .4f)) },
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainer,
            disabledContentColor = disabledContent,
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                Modifier.alpha(if (loading) 0f else 1f),   // keeps width while loading
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                leadingIcon?.invoke()
                Text(text, style = MaterialTheme.typography.labelLarge)
            }
            if (loading) CircularProgressIndicator(
                Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = contentColor
            )
        }
    }
}

@Composable
fun PrimaryButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true, loading: Boolean = false,
) = AppButton(
    text,
    onClick,
    modifier,
    leadingIcon = leadingIcon,
    enabled = enabled,
    loading = loading
)

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) = AppButton(
    text = text,
    onClick = onClick,
    modifier = modifier,
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.primary,
    borderColor = MaterialTheme.colorScheme.outlineVariant,
    leadingIcon = leadingIcon,
    enabled = enabled,
)