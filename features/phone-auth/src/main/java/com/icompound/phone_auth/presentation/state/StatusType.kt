package com.icompound.phone_auth.presentation.state

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.icompound.designsystem.ICompoundTheme
import com.icompound.phone_auth.R

enum class StatusType { Error, Warning, Success, Info }
@Immutable
data class StatusColors(val container: Color, val border: Color, val content: Color)


@Composable
@ReadOnlyComposable
fun StatusType.colors(): StatusColors {
    val scheme = MaterialTheme.colorScheme
    val ext = ICompoundTheme.colors
    return when (this) {
        StatusType.Error -> StatusColors(
            container = scheme.errorContainer,
            border = scheme.error.copy(alpha = .4f),
            content = scheme.error,
        )
        StatusType.Warning -> StatusColors(
            container = ext.warning.copy(alpha = .15f),
            border = ext.warning,
            content = ext.onWarning,
        )
        StatusType.Success -> StatusColors(
            container = ext.success.copy(alpha = .35f),
            border = ext.onSuccess.copy(alpha = .4f),
            content = ext.onSuccess,
        )
        StatusType.Info -> StatusColors(
            container = scheme.primaryContainer,
            border = ext.info,
            content = scheme.primary,
        )
    }
}
@Composable
fun StatusType.defaultIcon(): Painter = when (this) {
    StatusType.Error -> painterResource(R.drawable.ic_info_red)
    StatusType.Warning -> painterResource(R.drawable.ic_info_brown)
    StatusType.Success -> painterResource(R.drawable.ic_info_brown)
    StatusType.Info -> painterResource(R.drawable.ic_info_brown)
}

@Composable
fun StatusBanner(
    text: String,
    type: StatusType,
    modifier: Modifier = Modifier,
    icon: Painter = type.defaultIcon(),
) {
    val c = type.colors()
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier
            .fillMaxWidth()
            .background(c.container, shape)
            .border(1.dp, c.border, shape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, null, tint = c.content, modifier = Modifier.size(18.dp))
        Text(text, color = c.content, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun StatusIconChip(type: StatusType, icon: ImageVector, modifier: Modifier = Modifier) {
    val c = type.colors()
    Box(
        modifier.size(40.dp).background(c.container, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = c.content, modifier = Modifier.size(20.dp)) }
}
