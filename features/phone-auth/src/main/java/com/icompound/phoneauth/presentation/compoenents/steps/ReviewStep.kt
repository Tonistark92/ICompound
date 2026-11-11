package com.icompound.phoneauth.presentation.compoenents.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icompound.core.presentation.componenet.BodyText
import com.icompound.core.presentation.componenet.PrimaryButton
import com.icompound.core.presentation.componenet.SubBodyText
import com.icompound.core.presentation.componenet.TitleText
import com.icompound.designsystem.ICompoundTheme
import com.icompound.phoneauth.R
import com.icompound.phoneauth.domain.model.AuthStep
import com.icompound.phoneauth.domain.model.Details
import com.icompound.phoneauth.presentation.AuthStepLayout
import com.icompound.phoneauth.presentation.DetailRow
import com.icompound.phoneauth.presentation.compoenents.SoftCard
import com.icompound.phoneauth.presentation.state.PhoneAuthAction

@Composable
fun ReviewStep(
    step: AuthStep.Review,
    onAction: (PhoneAuthAction) -> Unit,
) {
    val d = step.details
    AuthStepLayout(
        contentArrangement = Arrangement.Top,
        bottom = {
            PrimaryButton(
                stringResource(R.string.finish_setup),
                onClick = { onAction(PhoneAuthAction.FinishClicked) },
            )
        },
    ) {
        Spacer(Modifier.height(24.dp))
        TitleText(stringResource(R.string.welcome_review_details))
        SubBodyText(stringResource(R.string.profile_visibility_description))
        Spacer(Modifier.height(16.dp))
        SoftCard {
            DetailRow(stringResource(R.string.full_name), d.fullName)
            DetailRow(stringResource(R.string.mobile), d.maskedMobile)
            DetailRow(stringResource(R.string.unit), d.unit.title)
        }
        Spacer(Modifier.height(12.dp))
        SoftCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    BodyText(stringResource(R.string.visitor_notification_title))
                    SubBodyText(stringResource(R.string.get_notified_about_visitors))
                }
                Switch(
                    checked = d.allowNotifications,
                    onCheckedChange = { onAction(PhoneAuthAction.NotificationsToggled(it)) },
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Review Details")
@Composable
private fun ReviewStepPreview() {
    ICompoundTheme {
        ReviewStep(
            step =
                AuthStep.Review(
                    details =
                        Details(
                            fullName = "Islam Hany Riyad",
                            maskedMobile = "+20 • • • • 4567",
                            unit = previewUnits[0],
                            allowNotifications = true,
                        ),
                ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "Review - Notifications Disabled")
@Composable
private fun ReviewStepNotificationsDisabledPreview() {
    ICompoundTheme {
        ReviewStep(
            step =
                AuthStep.Review(
                    details =
                        Details(
                            fullName = "Ahmed Mohamed",
                            maskedMobile = "+20 • • • • 1234",
                            unit = previewUnits[1],
                            allowNotifications = false,
                        ),
                ),
            onAction = {},
        )
    }
}
