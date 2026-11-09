package com.icompound.phone_auth.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.icompound.core.presentation.base.utils.getViewModel
import com.icompound.core.presentation.componenet.AppTextField
import com.icompound.core.presentation.componenet.BodyText
import com.icompound.core.presentation.componenet.PrimaryButton
import com.icompound.core.presentation.componenet.SecondaryButton
import com.icompound.core.presentation.componenet.SubBodyText
import com.icompound.core.presentation.componenet.TitleText
import com.icompound.core.presentation.utils.StatusBanner
import com.icompound.core.presentation.utils.StatusType
import com.icompound.core.presentation.utils.colors
import com.icompound.core.util.dial
import com.icompound.designsystem.ColorLightTokens
import com.icompound.phone_auth.R
import com.icompound.phone_auth.domain.AuthStep
import com.icompound.phone_auth.domain.OtpStatus
import com.icompound.phone_auth.domain.ResidentUnit
import com.icompound.phone_auth.presentation.compoenents.OtpField
import com.icompound.phone_auth.presentation.compoenents.RollingNumberText
import com.icompound.phone_auth.presentation.compoenents.SoftCard
import com.icompound.phone_auth.presentation.state.ManagerContactState
import com.icompound.phone_auth.presentation.state.PhoneAuthAction
import com.icompound.phone_auth.presentation.state.PhoneAuthEvent
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import com.icompound.phone_auth.presentation.compoenents.steps.LockedStep
import com.icompound.phone_auth.presentation.compoenents.steps.NumberRejectedStep
import com.icompound.phone_auth.presentation.compoenents.steps.OtpStep
import com.icompound.phone_auth.presentation.compoenents.steps.PhoneStep
import com.icompound.phone_auth.presentation.compoenents.steps.PickUnitStep
import com.icompound.phone_auth.presentation.compoenents.steps.ReviewStep
import com.icompound.phone_auth.presentation.compoenents.steps.SendFailedStep

@Composable
fun PhoneAuthScreen( modifier: Modifier = Modifier,onFinished: () -> Unit) {
    val viewModel = getViewModel<PhoneAuthViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val onAction: (PhoneAuthAction) -> Unit = remember(viewModel) { viewModel::processIntent }

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

    AnimatedContent(
        targetState = state.step,
        modifier = modifier,
        contentKey = { it::class },
        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
        label = "authStep"
    ) { step ->
        when (step) {
            is AuthStep.Phone -> PhoneStep(step, onAction)
            AuthStep.CheckingNumber -> LoadingState(
                stringResource(R.string.checking_phone_number),
                stringResource(R.string.unit_privacy_multiline)
            )

            is AuthStep.SendingCode -> LoadingState(
                stringResource(R.string.sending_code),
                stringResource(R.string.texting, step.maskedNumber),
            )

            is AuthStep.NumberRejected -> NumberRejectedStep(state.manager, onAction)
            is AuthStep.SendFailed -> SendFailedStep(state.manager, onAction)
            AuthStep.Locked -> LockedStep(state.manager, onAction)
            is AuthStep.Otp -> OtpStep(step, onAction)
            is AuthStep.PickUnit -> PickUnitStep(step, onAction)
            is AuthStep.Review -> ReviewStep(step, onAction)
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


@Composable
fun StatusIconChip(type: StatusType, icon: ImageVector) {
    val c = type.colors()   // make StatusType.colors() `internal` instead of `private`
    Box(
        Modifier
            .size(40.dp)
            .background(c.container, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = c.content, modifier = Modifier.size(20.dp)) }
}

@Composable
fun UnitOptionCard(
    unit: ResidentUnit,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    val border by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(200),
        label = "unitBorder"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = border,
                shape = shape
            )
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            BodyText(unit.title)
            unit.subtitle?.let { SubBodyText(it) }
        }

        RadioButton(
            selected = selected,
            onClick = null
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        SubBodyText(label)
        BodyText(value)
    }
}

@Composable
fun LoadingState(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = ColorLightTokens.SurfaceVariant,
            strokeWidth = 6.dp
        )
        Spacer(Modifier.height(16.dp))
        TitleText(title, textAlign = TextAlign.Center)
        subtitle?.let { SubBodyText(it, textAlign = TextAlign.Center) }
    }
}

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
                resendsLeft == 0 -> SubBodyText(stringResource(R.string.no_resends_left))
                secondsLeft > 0 -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Outlined.Refresh,
                        null,
                        Modifier.size(16.dp),
                        tint = ColorLightTokens.TextHint
                    )

                    val template = stringResource(R.string.resend_in, 0)        // "Resend in 0 s"
                    val (prefix, suffix) = template.split("0", limit = 2)       // "Resend in ", " s"

                    RollingNumberText(secondsLeft, prefix = prefix, suffix = suffix)
                }

                else -> TextButton(onClick = onResend) {
                    Icon(Icons.Outlined.Refresh, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Resend Code", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}