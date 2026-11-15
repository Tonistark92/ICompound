package com.icompound.phoneauth.presentation.compoenents.steps

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import com.icompound.core.presentation.componenet.BodyText
import com.icompound.core.presentation.componenet.LabelText
import com.icompound.core.presentation.componenet.PrimaryButton
import com.icompound.core.presentation.componenet.SecondaryButton
import com.icompound.core.presentation.componenet.SubBodyText
import com.icompound.core.presentation.componenet.TitleText
import com.icompound.core.presentation.utils.StatusType
import com.icompound.designsystem.ICompoundTheme
import com.icompound.phoneauth.R
import com.icompound.phoneauth.presentation.compoenents.SoftCard
import com.icompound.phoneauth.presentation.state.ManagerContactState
import com.icompound.phoneauth.presentation.state.PhoneAuthAction
import kotlinx.coroutines.delay

@Composable
fun LockedStep(

    lockSeconds: Int = 15 * 60, // later: pass this from the state / backend
    onAction: (PhoneAuthAction) -> Unit,
) = StatusStep(
    type = StatusType.Error,
    icon = painterResource(R.drawable.ic_lock_x),
    title = stringResource(R.string.too_many_attempts),
    body = stringResource(R.string.last_attempt_locked),
    details = {
        val secondsLeft = rememberCountdown(lockSeconds)
        SoftCard {
            BodyText(stringResource(R.string.request_new_code_in))
            LabelText("%02d:%02d".format(secondsLeft / 60, secondsLeft % 60))
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
            stringResource(R.string.back_to_start),
            { onAction(PhoneAuthAction.BackClicked) },
        )
    },
)

@Composable
private fun rememberCountdown(totalSeconds: Int): Int {
    var left by remember(totalSeconds) { mutableIntStateOf(totalSeconds) }
    LaunchedEffect(totalSeconds) {
        while (left > 0) {
            delay(1000)
            left--
        }
    }
    return left
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun LockedStepPreview(modifier: Modifier = Modifier) {
    ICompoundTheme {
        LockedStep() {

        }
    }

}
