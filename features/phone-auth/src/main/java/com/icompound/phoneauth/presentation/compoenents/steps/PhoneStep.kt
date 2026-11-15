package com.icompound.phoneauth.presentation.compoenents.steps

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icompound.core.presentation.componenet.AppTextField
import com.icompound.core.presentation.componenet.PrimaryButton
import com.icompound.core.presentation.componenet.SubBodyText
import com.icompound.core.presentation.componenet.TitleText
import com.icompound.designsystem.ICompoundExtendedColors
import com.icompound.designsystem.ICompoundTheme
import com.icompound.phoneauth.R
import com.icompound.phoneauth.domain.model.AuthStep
import com.icompound.phoneauth.domain.model.ResidentUnit
import com.icompound.phoneauth.presentation.AuthStepLayout
import com.icompound.phoneauth.presentation.state.PhoneAuthAction

@Composable
fun PhoneStep(
    step: AuthStep.Phone,
    onAction: (PhoneAuthAction) -> Unit,
) {
    AuthStepLayout(bottom = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.VerifiedUser, null, Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            SubBodyText(stringResource(R.string.unit_privacy_notice))
        }
        PrimaryButton(
            stringResource(R.string.send_code),
            enabled = step.number.isNotBlank() && step.error == null,
            onClick = { onAction(PhoneAuthAction.SendCodeClicked) },
        )
    }) {
        TitleText(stringResource(R.string.enter_phone_number))
        SubBodyText(stringResource(R.string.phone_verification_description))
        Spacer(Modifier.height(16.dp))
        AppTextField(
            value = step.number,
            onValueChange = { onAction(PhoneAuthAction.PhoneChanged(it)) },
            label = stringResource(R.string.mobile_number),
            placeholder = "1xxx xxx xxx",
            keyboardType = KeyboardType.Phone,
            autoFocus = true,
            error = step.error,
            leading = {
                Surface(
                    modifier = Modifier.size(25.dp),
                    shadowElevation = 50.dp,
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_eg),
                        contentDescription = "",
                        modifier = Modifier.padding(1.dp),
                    )
                }
            },
        )
    }
}

@Preview(showBackground = true, name = "Phone - Empty")
@Composable
private fun PhoneStepEmptyPreview() {
    ICompoundTheme {
        PhoneStep(
            step = AuthStep.Phone(),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "Phone - With Error")
@Composable
private fun PhoneStepErrorPreview() {
    ICompoundTheme {
        PhoneStep(
            step =
                AuthStep.Phone(
                    number = "01012345678",
                    error = stringResource(R.string.invalid_phone_number),
                ),
            onAction = {},
        )
    }
}
