package com.icompound.core.presentation.componenet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icompound.designsystem.ColorLightTokens
import com.icompound.designsystem.ICompoundTheme

@Composable
fun TitleText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
) = Text(
    text,
    modifier,
    color = MaterialTheme.colorScheme.onBackground,
    style = MaterialTheme.typography.titleSmall,
    textAlign = textAlign,
)

@Composable
fun BodyText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
) = Text(
    text,
    modifier,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    style = MaterialTheme.typography.bodyMedium,
    textAlign = textAlign,
)

@Composable
fun SubBodyText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
) = Text(
    text,
    modifier,
    color = ColorLightTokens.TextHint,
    style = MaterialTheme.typography.bodySmall,
    textAlign = textAlign,
)

@Composable
fun LabelText(
    text: String,
    modifier: Modifier = Modifier,
) = Text(
    text.uppercase(),
    modifier,
    color = MaterialTheme.colorScheme.onSurface,
    style = MaterialTheme.typography.labelLarge,
) // uppercase handled once, here

@Composable
fun ErrorText(
    text: String,
    modifier: Modifier = Modifier,
) = Text(
    text,
    modifier,
    color = MaterialTheme.colorScheme.error,
    style = MaterialTheme.typography.bodySmall,
)

@Preview(
    name = "Typography Components",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun AppTextComponentsPreview() {
    ICompoundTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TitleText(
                text = "Enter your phone number",
            )

            BodyText(
                text = "We'll text you a one-time code to confirm it's you.",
            )

            SubBodyText(
                text = "Your unit details stay hidden until you verify the code.",
            )

            LabelText(
                text = "Mobile Number",
            )

            ErrorText(
                text = "That number doesn't look right.",
            )
        }
    }
}
