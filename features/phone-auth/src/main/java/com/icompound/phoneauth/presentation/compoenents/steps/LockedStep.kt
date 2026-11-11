package com.icompound.phoneauth.presentation.compoenents.steps

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.icompound.core.presentation.componenet.PrimaryButton
import com.icompound.core.presentation.componenet.SecondaryButton
import com.icompound.core.presentation.utils.StatusType
import com.icompound.phoneauth.R
import com.icompound.phoneauth.presentation.state.ManagerContactState
import com.icompound.phoneauth.presentation.state.PhoneAuthAction

@Composable
fun LockedStep(
    manager: ManagerContactState,
    onAction: (PhoneAuthAction) -> Unit,
) = StatusStep(
    type = StatusType.Error,
    icon = Icons.Outlined.Lock,
    title = stringResource(R.string.too_many_attempts),
    body = stringResource(R.string.attempts_left),
    primary = {
        PrimaryButton(
            stringResource(R.string.contact_compound_management),
            { onAction(PhoneAuthAction.ContactManagerClicked) },
        )
    },
    secondary = {
        SecondaryButton(
            stringResource(R.string.back_to_start),
            { onAction(PhoneAuthAction.BackClicked) },
        )
    },
)
