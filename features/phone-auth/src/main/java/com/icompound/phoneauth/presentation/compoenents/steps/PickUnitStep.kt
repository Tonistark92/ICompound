package com.icompound.phoneauth.presentation.compoenents.steps

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icompound.core.presentation.componenet.BodyText
import com.icompound.core.presentation.componenet.PrimaryButton
import com.icompound.core.presentation.componenet.SubBodyText
import com.icompound.core.presentation.componenet.TitleText
import com.icompound.designsystem.ICompoundTheme
import com.icompound.phoneauth.R
import com.icompound.phoneauth.domain.model.AuthStep
import com.icompound.phoneauth.domain.model.ResidentUnit
import com.icompound.phoneauth.presentation.AuthStepLayout
import com.icompound.phoneauth.presentation.state.PhoneAuthAction

@Composable
fun PickUnitStep(
    step: AuthStep.PickUnit,
    onAction: (PhoneAuthAction) -> Unit,
) {
    AuthStepLayout(
        contentArrangement = Arrangement.Top,
        bottom = {
            SubBodyText(stringResource(R.string.only_units_registered_to_number))
            PrimaryButton(
                text = stringResource(R.string._continue),
                enabled = step.selectedId != null,
                onClick = { onAction(PhoneAuthAction.ContinueClicked) },
            )
        },
    ) {
        Spacer(Modifier.height(24.dp))
        TitleText(stringResource(R.string.select_unit_title))
        SubBodyText(stringResource(R.string.multiple_units_description))
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
fun UnitOptionCard(
    unit: ResidentUnit,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)

    val border by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        animationSpec = tween(200),
        label = "unitBorder",
    )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = border,
                    shape = shape,
                )
                .selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = onClick,
                )
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            BodyText(unit.title)
            unit.subtitle?.let { SubBodyText(it) }
        }

        RadioButton(
            selected = selected,
            onClick = null,
        )
    }
}

val previewUnits =
    listOf(
        ResidentUnit(
            id = "unit-1",
            title = "Villa 14",
            subtitle = "Palm Hills Compound",
        ),
        ResidentUnit(
            id = "unit-2",
            title = "Apartment 302",
            subtitle = "Building B",
        ),
        ResidentUnit(
            id = "unit-3",
            title = "Villa 21",
            subtitle = "Garden District",
        ),
    )

@Preview(showBackground = true, name = "Pick Unit")
@Composable
private fun PickUnitStepPreview() {
    ICompoundTheme {
        PickUnitStep(
            step =
                AuthStep.PickUnit(
                    units = previewUnits,
                ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "Pick Unit - Selected")
@Composable
private fun PickUnitStepSelectedPreview() {
    ICompoundTheme {
        PickUnitStep(
            step =
                AuthStep.PickUnit(
                    units = previewUnits,
                    selectedId = "unit-2",
                ),
            onAction = {},
        )
    }
}
