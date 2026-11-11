package com.icompound.core.presentation.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icompound.designsystem.ColorLightTokens
import com.icompound.designsystem.ICompoundTheme

enum class StatusType { Error, Warning, Success, Info }

data class StatusColors(
    val container: Color,
    val border: Color,
    val content: Color,
)

fun StatusType.colors() =
    when (this) {
        StatusType.Error -> {
            StatusColors(ColorLightTokens.ErrorContainer, ColorLightTokens.Error.copy(alpha = .4f), ColorLightTokens.Error)
        }

        StatusType.Warning -> {
            StatusColors(ColorLightTokens.Warning.copy(alpha = .15f), ColorLightTokens.Warning, ColorLightTokens.OrangeDark)
        }

        StatusType.Success -> {
            StatusColors(ColorLightTokens.Success, ColorLightTokens.OnSuccess.copy(alpha = .4f), ColorLightTokens.OnSuccess)
        }

        StatusType.Info -> {
            StatusColors(ColorLightTokens.PrimaryContainer, ColorLightTokens.Info, ColorLightTokens.Primary)
        }
    }

@Composable
fun StatusBanner(
    text: String,
    type: StatusType,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Info,
) {
    val c = type.colors()
    Row(
        modifier
            .fillMaxWidth()
            .background(c.container, RoundedCornerShape(10.dp))
            .border(1.dp, c.border, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, null, tint = c.content, modifier = Modifier.size(18.dp))
        Text(text, color = c.content, style = MaterialTheme.typography.bodySmall)
    }
}

@Preview(
    name = "Status Banners",
    showBackground = true,
)
@Composable
private fun StatusBannerPreview() {
    ICompoundTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatusBanner(
                text = "Something went wrong. Please try again.",
                type = StatusType.Error,
            )

            StatusBanner(
                text = "Your verification code will expire soon.",
                type = StatusType.Warning,
            )

            StatusBanner(
                text = "Your phone number has been verified.",
                type = StatusType.Success,
            )

            StatusBanner(
                text = "A verification code has been sent to your phone.",
                type = StatusType.Info,
            )
        }
    }
}
