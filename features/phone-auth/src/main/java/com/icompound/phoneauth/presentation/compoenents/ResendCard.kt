package com.icompound.phoneauth.presentation.compoenents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.icompound.core.presentation.componenet.BodyText
import com.icompound.core.presentation.componenet.SubBodyText
import com.icompound.designsystem.ColorLightTokens
import com.icompound.phoneauth.R


@Composable
fun ResendCard(
    secondsLeft: Int,
    resendsLeft: Int,
    onResend: () -> Unit,
    modifier: Modifier = Modifier,
    maxResends: Int = 3,
) {
    SoftCard(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                BodyText(stringResource(R.string.didnt_receive_code))
                SubBodyText(stringResource(R.string.resends_left, resendsLeft, maxResends))
            }
            when {
                resendsLeft == 0 -> {
                    SubBodyText(stringResource(R.string.no_resends_left))
                }

                secondsLeft > 0 -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Refresh,
                            null,
                            Modifier.size(16.dp),
                            tint = ColorLightTokens.TextHint,
                        )

                        val template = stringResource(R.string.resend_in, 0) // "Resend in 0 s"
                        val (prefix, suffix) = template.split("0", limit = 2) // "Resend in ", " s"

                        RollingNumberText(secondsLeft, prefix = prefix, suffix = suffix)
                    }
                }

                else -> {
                    TextButton(onClick = onResend) {
                        Icon(Icons.Outlined.Refresh, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Resend Code", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
