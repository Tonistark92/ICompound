package com.icompound.phoneauth.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.icompound.core.presentation.base.utils.getViewModel
import com.icompound.core.presentation.componenet.BodyText
import com.icompound.core.presentation.componenet.SubBodyText
import com.icompound.core.presentation.componenet.TitleText
import com.icompound.core.presentation.utils.StatusType
import com.icompound.core.presentation.utils.colors
import com.icompound.core.util.dial
import com.icompound.designsystem.ColorLightTokens
import com.icompound.designsystem.ICompoundTheme
import com.icompound.phoneauth.R
import com.icompound.phoneauth.domain.model.AuthStep
import com.icompound.phoneauth.domain.model.ResidentUnit
import com.icompound.phoneauth.presentation.compoenents.LoadingState
import com.icompound.phoneauth.presentation.compoenents.RollingNumberText
import com.icompound.phoneauth.presentation.compoenents.SegmentedStepProgress
import com.icompound.phoneauth.presentation.compoenents.SoftCard
import com.icompound.phoneauth.presentation.compoenents.StepProgress
import com.icompound.phoneauth.presentation.compoenents.steps.LockedStep
import com.icompound.phoneauth.presentation.compoenents.steps.NumberRejectedStep
import com.icompound.phoneauth.presentation.compoenents.steps.OtpStep
import com.icompound.phoneauth.presentation.compoenents.steps.PhoneStep
import com.icompound.phoneauth.presentation.compoenents.steps.PickUnitStep
import com.icompound.phoneauth.presentation.compoenents.steps.ReviewStep
import com.icompound.phoneauth.presentation.compoenents.steps.SendFailedStep
import com.icompound.phoneauth.presentation.state.PhoneAuthAction
import com.icompound.phoneauth.presentation.state.PhoneAuthEvent
import com.icompound.phoneauth.presentation.state.TOTAL_STEPS
import com.icompound.phoneauth.presentation.state.progress

@Composable
fun PhoneAuthScreen(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit,
) {
    val viewModel = getViewModel<PhoneAuthViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val onAction: (PhoneAuthAction) -> Unit = remember(viewModel) { viewModel::processIntent }
    val progress = state.step.progress()

    var lastProgress by remember { mutableStateOf(StepProgress(1, TOTAL_STEPS)) }
    if (progress != null) lastProgress = progress


    LaunchedEffect(Unit) {
        viewModel.event.collect {
            when (it) {
                is PhoneAuthEvent.Dial -> context.dial(it.phone)
                PhoneAuthEvent.Done -> onFinished()
            }
        }
    }

    BackHandler(enabled = state.step !is AuthStep.Phone) {
        onAction(PhoneAuthAction.BackClicked)
    }

    Column(modifier
        .fillMaxSize()
        .background(color = ICompoundTheme.colorScheme.background)) {
        AnimatedVisibility(
            visible = progress != null,
            enter = fadeIn(tween(300)) + expandVertically(tween(300)),
            exit = fadeOut(tween(200)) + shrinkVertically(tween(200)),
        ) {
            SegmentedStepProgress(
                progress = lastProgress,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp),
            )
        }
        AnimatedContent(
            targetState = state.step,
            modifier = modifier,
            contentKey = { it::class },
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
            label = "authStep",
        ) { step ->
            when (step) {
                is AuthStep.Phone -> {
                    PhoneStep(step, onAction)
                }

                AuthStep.CheckingNumber -> {
                    LoadingState(
                        stringResource(R.string.checking_phone_number),
                        stringResource(R.string.unit_privacy_multiline),
                    )
                }

                is AuthStep.SendingCode -> {
                    LoadingState(
                        stringResource(R.string.sending_code),
                        stringResource(R.string.texting, step.maskedNumber),
                    )
                }

                is AuthStep.NumberRejected -> {
                    NumberRejectedStep(onAction)
                }

                is AuthStep.SendFailed -> {
                    SendFailedStep(onAction)
                }

                AuthStep.Locked -> {
                    LockedStep(onAction)
                }

                is AuthStep.Otp -> {
                    OtpStep(step, onAction)
                }

                is AuthStep.PickUnit -> {
                    PickUnitStep(step, onAction)
                }

                is AuthStep.Review -> {
                    ReviewStep(step, onAction)
                }
            }
        }

    }
}

@Composable
fun AuthStepLayout(
    modifier: Modifier = Modifier,
    contentArrangement: Arrangement.Vertical = Arrangement.Center,
    bottom: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .background(color = ICompoundTheme.colorScheme.background),
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = contentArrangement,
            content = content,
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = bottom)
    }
}




