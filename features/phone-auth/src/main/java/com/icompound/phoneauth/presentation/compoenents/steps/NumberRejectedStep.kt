package com.icompound.phoneauth.presentation.compoenents.steps

import android.R.id.primary
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_NO
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icompound.core.presentation.componenet.BodyText
import com.icompound.core.presentation.componenet.LabelText
import com.icompound.core.presentation.componenet.PrimaryButton
import com.icompound.core.presentation.componenet.SecondaryButton
import com.icompound.core.presentation.componenet.SubBodyText
import com.icompound.core.presentation.componenet.TitleText
import com.icompound.core.presentation.utils.StatusType
import com.icompound.designsystem.ICompoundTheme
import com.icompound.phoneauth.R
import com.icompound.phoneauth.presentation.compoenents.NumberedStep
import com.icompound.phoneauth.presentation.compoenents.SoftCard
import com.icompound.phoneauth.presentation.state.ManagerContactState
import com.icompound.phoneauth.presentation.state.PhoneAuthAction

@Composable
fun NumberRejectedStep(onAction: (PhoneAuthAction) -> Unit) =
    StatusStep(
        type = StatusType.Error,
        icon = painterResource(R.drawable.ic_apartment_blue),
        title = stringResource(R.string.phone_verification_failed),
        body = stringResource(R.string.phone_verification_failed_description),
        details = {
            SoftCard {
                LabelText(stringResource(R.string.what_can_you_do))
                NumberedStep(1, stringResource(R.string.register_phone_with_management))
                NumberedStep(2, stringResource(R.string.retry_registered_phone))
            }
        },
        primary = {
            PrimaryButton(
                stringResource(R.string.contact_compound_management),
                { onAction(PhoneAuthAction.ContactManagerClicked) },
            )
        },
        secondary = {
            SecondaryButton(
                stringResource(R.string.try_different_number),
                { onAction(PhoneAuthAction.TryDifferentNumber) },
            )
        },
    )

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun NumberRejectedStepPreview(modifier: Modifier = Modifier) {
    ICompoundTheme {
        NumberRejectedStep {
        }
    }
}
