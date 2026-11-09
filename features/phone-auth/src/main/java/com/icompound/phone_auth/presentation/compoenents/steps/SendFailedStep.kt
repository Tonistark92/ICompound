package com.icompound.phone_auth.presentation.compoenents.steps

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.icompound.core.presentation.componenet.PrimaryButton
import com.icompound.core.presentation.componenet.SecondaryButton
import com.icompound.core.presentation.utils.StatusBanner
import com.icompound.core.presentation.utils.StatusType
import com.icompound.phone_auth.R
import com.icompound.phone_auth.presentation.state.ManagerContactState
import com.icompound.phone_auth.presentation.state.PhoneAuthAction


@Composable
fun SendFailedStep(manager: ManagerContactState, onAction: (PhoneAuthAction) -> Unit) =
    StatusStep(
        type = StatusType.Warning, icon = Icons.Outlined.WarningAmber,
        title = stringResource(R.string.phone_verification_failed),
        body = stringResource(R.string.sms_sending_failed_description),
        details = {
            StatusBanner(
                stringResource(R.string.sms_retry_hint),
                StatusType.Warning
            )
        },
        primary = {
            PrimaryButton(
                stringResource(R.string.try_again),
                { onAction(PhoneAuthAction.RetrySend) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_retry_white),
                        contentDescription = null
                    )
                }
            )
        },
        secondary = {
            SecondaryButton(
                text = stringResource(R.string.contact_support),
                onClick = { onAction(PhoneAuthAction.ContactSupportClicked) },
            )
        }, // swap for SecondaryButton "Contact Support" if different
    )
