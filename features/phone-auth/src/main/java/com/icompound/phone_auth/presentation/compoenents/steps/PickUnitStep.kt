package com.icompound.phone_auth.presentation.compoenents.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icompound.core.presentation.componenet.PrimaryButton
import com.icompound.core.presentation.componenet.SubBodyText
import com.icompound.core.presentation.componenet.TitleText
import com.icompound.designsystem.ICompoundTheme
import com.icompound.phone_auth.R
import com.icompound.phone_auth.domain.AuthStep
import com.icompound.phone_auth.presentation.AuthStepLayout
import com.icompound.phone_auth.presentation.UnitOptionCard
import com.icompound.phone_auth.presentation.state.PhoneAuthAction


@Composable
fun PickUnitStep(step: AuthStep.PickUnit, onAction: (PhoneAuthAction) -> Unit) {
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

@Preview(showBackground = true, name = "Pick Unit")
@Composable
private fun PickUnitStepPreview() {
    ICompoundTheme {
        PickUnitStep(
            step = AuthStep.PickUnit(
                units = previewUnits
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "Pick Unit - Selected")
@Composable
private fun PickUnitStepSelectedPreview() {
    ICompoundTheme {
        PickUnitStep(
            step = AuthStep.PickUnit(
                units = previewUnits,
                selectedId = "unit-2"
            ),
            onAction = {}
        )
    }
}