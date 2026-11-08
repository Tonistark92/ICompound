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
import androidx.compose.remote.creation.compose.modifier.SemanticsProperties.Role
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

@Composable
fun PhoneAuthScreen(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel = getViewModel<PhoneAuthViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()      // `by`, not `=`
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
                "Checking Your Number…",
                "Nothing about your unit is shown until you verify the code"
            )

            is AuthStep.SendingCode -> LoadingState(
                "Sending your code…",
                "Texting ${step.maskedNumber}"
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
    Column(modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 16.dp)) {
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
private fun StatusStep(
    type: StatusType,
    icon: ImageVector,
    title: String,
    body: String,
    primary: @Composable ColumnScope.() -> Unit,
    secondary: (@Composable ColumnScope.() -> Unit)? = null,
    details: (@Composable ColumnScope.() -> Unit)? = null,
) {
    AuthStepLayout(bottom = { primary(); secondary?.invoke(this) }) {
        StatusIconChip(type, icon)            // small rounded chip, colors from type
        Spacer(Modifier.height(12.dp))
        TitleText(title)
        Spacer(Modifier.height(6.dp))
        SubBodyText(body)
        details?.let { Spacer(Modifier.height(16.dp)); it() }
    }
}

@Composable
private fun StatusIconChip(type: StatusType, icon: ImageVector) {
    val c = type.colors()   // make StatusType.colors() `internal` instead of `private`
    Box(
        Modifier
            .size(40.dp)
            .background(c.container, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = c.content, modifier = Modifier.size(20.dp)) }
}

@Composable
fun PhoneStep(step: AuthStep.Phone, onAction: (PhoneAuthAction) -> Unit) {
    AuthStepLayout(bottom = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.VerifiedUser, null, Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            SubBodyText("Your unit details stay hidden until you verify the code.")
        }
        PrimaryButton(
            stringResource(R.string.send_code),
            enabled = step.number.isNotBlank() && step.error == null,
            onClick = { onAction(PhoneAuthAction.SendCodeClicked) })
    }) {
        TitleText("Enter your phone number")
        SubBodyText("We'll text you a one-time code to confirm it's you. Use the number registered with management.")
        Spacer(Modifier.height(16.dp))
        AppTextField(
            value = step.number,
            onValueChange = { onAction(PhoneAuthAction.PhoneChanged(it)) },
            label = "Mobile Number", placeholder = "1xxx xxx xxx",
            keyboardType = KeyboardType.Phone, autoFocus = true, error = step.error,
            leading = { /* flag + "+20" */ },
        )
    }
}

@Composable
fun NumberRejectedStep(manager: ManagerContactState, onAction: (PhoneAuthAction) -> Unit) =
    StatusStep(
        type = StatusType.Error, icon = Icons.Outlined.Apartment,
        title = "We couldn't continue with this number.",
        body = "Check the number and try again, or contact compound management.",
        details = { /* "What can you do" card with the two numbered steps */ },
        primary = { PrimaryButton(stringResource(R.string.retry_registered_phone), { onAction(PhoneAuthAction.RetrySend) }) },
        secondary = {
            SecondaryButton(
                stringResource(R.string.try_different_number),
                { onAction(PhoneAuthAction.TryDifferentNumber) })
        },
    )

@Composable
fun SendFailedStep(manager: ManagerContactState, onAction: (PhoneAuthAction) -> Unit) =
    StatusStep(
        type = StatusType.Warning, icon = Icons.Outlined.WarningAmber,
        title = "We couldn't send the code",
        body = "Something interrupted the SMS, or your connection dropped. Your number is fine, so you can try again.",
        details = {
            StatusBanner(
                "Check that you're online, then retry. If it keeps failing, support can help.",
                StatusType.Warning
            )
        },
        primary = {
            PrimaryButton(
                "Try again",
                { onAction(PhoneAuthAction.RetrySend) },
                leadingIcon = {Icon(painter = painterResource(R.drawable.ic_retry_white), contentDescription = null)}
            )
        },
        secondary = {
            SecondaryButton(
                stringResource(R.string.contact_support),
                PhoneAuthAction.ContactSupportClicked
            )
        }, // swap for SecondaryButton "Contact Support" if different
    )

@Composable
fun LockedStep(manager: ManagerContactState, onAction: (PhoneAuthAction) -> Unit) =
    StatusStep(
        type = StatusType.Error, icon = Icons.Outlined.Lock,
        title = "Too many attempts",
        body = "Entry is locked for now. Contact compound management to get back in.",
        primary = {
            PrimaryButton(
                stringResource(R.string.contact_compound_management),
                { onAction(PhoneAuthAction.ContactManagerClicked) })
        },
        secondary = { SecondaryButton(stringResource(R.string.back_to_start), { onAction(PhoneAuthAction.BackClicked) }) },
    )

@Composable
fun OtpStep(step: AuthStep.Otp, onAction: (PhoneAuthAction) -> Unit) {
    AuthStepLayout(bottom = {
        PrimaryButton(
            stringResource(R.string.verify), enabled = step.code.length == 6 && step.status == OtpStatus.Idle,
            loading = step.status == OtpStatus.Verifying,
            onClick = { onAction(PhoneAuthAction.CodeComplete(step.code)) })
    }) {
        TitleText(stringResource(R.string.enter_verification_code))
        SubBodyText(stringResource(R.string.sent_by_sms_to)+" ${step.maskedNumber}")
        Spacer(Modifier.height(20.dp))
        OtpField(
            code = step.code, status = step.status,
            onCodeChange = { onAction(PhoneAuthAction.CodeChanged(it)) },
            onComplete = { onAction(PhoneAuthAction.CodeComplete(it)) },
        )
        step.errorMessage?.let {
            Spacer(Modifier.height(12.dp)); StatusBanner(
            it,
            StatusType.Error
        )
        }
        Spacer(Modifier.height(12.dp))
        ResendCard(step.secondsLeft, step.resendsLeft, onResend ={ onAction(PhoneAuthAction.ResendClicked)})
    }
}

@Composable
fun ReviewStep(step: AuthStep.Review, onAction: (PhoneAuthAction) -> Unit) {
    val d = step.details
    AuthStepLayout(
        contentArrangement = Arrangement.Top,
        bottom = {
            PrimaryButton(
                stringResource(R.string.finish_setup),
                onClick = { onAction(PhoneAuthAction.FinishClicked) })
        },
    ) {
        Spacer(Modifier.height(24.dp))
        TitleText(stringResource(R.string.welcome_review_details))
        SubBodyText("Confirm how we've saved your profile. You can change this later.")
        Spacer(Modifier.height(16.dp))
        SoftCard {
            DetailRow("Full name", d.fullName)
            DetailRow("Mobile", d.maskedMobile)
            DetailRow("Unit", d.unit.title)
        }
        Spacer(Modifier.height(12.dp))
        SoftCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    BodyText("Know when a visitor arrives")
                    SubBodyText("Get notified about visitors, deliveries and compound updates.")
                }
                Switch(
                    checked = d.allowNotifications,
                    onCheckedChange = { onAction(PhoneAuthAction.NotificationsToggled(it)) },
                )
            }
        }
    }
}

@Composable
fun PickUnitStep(step: AuthStep.PickUnit, onAction: (PhoneAuthAction) -> Unit) {
    AuthStepLayout(
        contentArrangement = Arrangement.Top,
        bottom = {
            SubBodyText("Only units registered to your number are listed.")
            PrimaryButton(
                text = "Continue",
                enabled = step.selectedId != null,
                onClick = { onAction(PhoneAuthAction.ContinueClicked) },
            )
        },
    ) {
        Spacer(Modifier.height(24.dp))
        TitleText("Which unit would you like to access?")
        SubBodyText("Your number is linked to more than one unit. Pick one to continue.")
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            step.units.forEach { unit ->
                UnitOptionCard(
                    unit = unit,
                    selected = unit.id == step.selectedId,
                    onClick = { onAction(PhoneAuthAction.UnitSelected(unit.id)) },
                )
            }
        }
    }
}

@Composable
private fun UnitOptionCard(
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
private fun DetailRow(label: String, value: String) {
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
                BodyText("Didn't get it?")
                SubBodyText("$resendsLeft of $maxResends resends left")
            }
            when {
                resendsLeft == 0 -> SubBodyText("No resends left")
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
                    RollingNumberText(secondsLeft, prefix = "Resend in ", suffix = "s")
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