package com.icompound.phoneauth.presentation.compoenents.steps

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.icompound.core.presentation.componenet.PrimaryButton
import com.icompound.core.presentation.componenet.SecondaryButton
import com.icompound.core.presentation.utils.StatusType
import com.icompound.phoneauth.R
import com.icompound.phoneauth.presentation.state.ManagerContactState
import com.icompound.phoneauth.presentation.state.PhoneAuthAction

@Composable
fun NumberRejectedStep(
    manager: ManagerContactState,
    onAction: (PhoneAuthAction) -> Unit,
) = StatusStep(
    type = StatusType.Error,
    icon = Icons.Outlined.Apartment,
    title = stringResource(R.string.phone_verification_failed),
    body = stringResource(R.string.phone_verification_failed_description),
    details = { /* "What can you do" card with the two numbered steps */ },
    primary = {
        PrimaryButton(
            stringResource(R.string.retry_registered_phone),
            { onAction(PhoneAuthAction.RetrySend) },
        )
    },
    secondary = {
        SecondaryButton(
            stringResource(R.string.try_different_number),
            { onAction(PhoneAuthAction.TryDifferentNumber) },
        )
    },
)
