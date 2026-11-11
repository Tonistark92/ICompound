package com.icompound.phoneauth.presentation.compoenents.steps

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icompound.core.presentation.componenet.PrimaryButton
import com.icompound.core.presentation.componenet.SubBodyText
import com.icompound.core.presentation.componenet.TitleText
import com.icompound.core.presentation.utils.StatusBanner
import com.icompound.core.presentation.utils.StatusType
import com.icompound.designsystem.ICompoundTheme
import com.icompound.phoneauth.R
import com.icompound.phoneauth.domain.model.AuthStep
import com.icompound.phoneauth.domain.model.OtpStatus
import com.icompound.phoneauth.domain.model.toUi
import com.icompound.phoneauth.presentation.AuthStepLayout
import com.icompound.phoneauth.presentation.ResendCard
import com.icompound.phoneauth.presentation.compoenents.OtpField
import com.icompound.phoneauth.presentation.state.PhoneAuthAction

@Composable
fun OtpStep(
    step: AuthStep.Otp,
    onAction: (PhoneAuthAction) -> Unit,
) {
    AuthStepLayout(bottom = {
        PrimaryButton(
            stringResource(R.string.verify),
            enabled = step.code.length == 6 && step.status == OtpStatus.Idle,
            loading = step.status == OtpStatus.Verifying,
            onClick = { onAction(PhoneAuthAction.CodeComplete(step.code)) },
        )
    }) {
        TitleText(stringResource(R.string.enter_verification_code))
        SubBodyText(stringResource(R.string.sent_by_sms_to) + " ${step.maskedNumber}")
        Spacer(Modifier.height(20.dp))
        OtpField(
            code = step.code,
            status = step.status.toUi(),
            onCodeChange = { onAction(PhoneAuthAction.CodeChanged(it)) },
            onComplete = { onAction(PhoneAuthAction.CodeComplete(it)) },
        )
        step.errorMessage?.let {
            Spacer(Modifier.height(12.dp))
            StatusBanner(
                it,
                StatusType.Error,
            )
        }
        Spacer(Modifier.height(12.dp))
        ResendCard(
            step.secondsLeft,
            step.resendsLeft,
            onResend = { onAction(PhoneAuthAction.ResendClicked) },
        )
    }
}

@Preview(showBackground = true, name = "OTP - Idle")
@Composable
private fun OtpStepIdlePreview() {
    ICompoundTheme {
        OtpStep(
            step =
                AuthStep.Otp(
                    maskedNumber = "+20 • • • • 4567",
                    code = "",
                    status = OtpStatus.Idle,
                    secondsLeft = 30,
                    resendsLeft = 3,
                ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "OTP - Entered Code")
@Composable
private fun OtpStepEnteredCodePreview() {
    ICompoundTheme {
        OtpStep(
            step =
                AuthStep.Otp(
                    maskedNumber = "+20 • • • • 4567",
                    code = "123456",
                    status = OtpStatus.Idle,
                    secondsLeft = 0,
                    resendsLeft = 2,
                ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "OTP - Verifying")
@Composable
private fun OtpStepVerifyingPreview() {
    ICompoundTheme {
        OtpStep(
            step =
                AuthStep.Otp(
                    maskedNumber = "+20 • • • • 4567",
                    code = "123456",
                    status = OtpStatus.Verifying,
                    secondsLeft = 0,
                    resendsLeft = 2,
                ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "OTP - Error")
@Composable
private fun OtpStepErrorPreview() {
    ICompoundTheme {
        OtpStep(
            step =
                AuthStep.Otp(
                    maskedNumber = "+20 • • • • 4567",
                    code = "123456",
                    status = OtpStatus.Idle,
                    errorMessage = "The verification code is incorrect",
                    secondsLeft = 0,
                    resendsLeft = 1,
                ),
            onAction = {},
        )
    }
}
