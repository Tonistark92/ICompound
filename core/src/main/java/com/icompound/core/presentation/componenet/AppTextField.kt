package com.icompound.core.presentation.componenet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icompound.designsystem.ICompoundTheme

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    error: String? = null,
    leading: (@Composable () -> Unit)? = null, // image, icon, flag + "+20", anything
    trailing: (@Composable () -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    autoFocus: Boolean = false, // focus when the screen opens
    focusRequester: FocusRequester? = null, // focus from outside, on demand
) {
    val requester = focusRequester ?: remember { FocusRequester() }

    if (autoFocus) LaunchedEffect(Unit) { requester.requestFocus() }

    Column(modifier) {
        label?.let {
            LabelText(it)
            Spacer(Modifier.height(6.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .focusRequester(requester),
            isError = error != null,
            placeholder = placeholder?.let { { SubBodyText(it) } },
            leadingIcon = leading,
            trailingIcon = trailing,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onAny = { onImeAction() }),
            shape = RoundedCornerShape(8.dp),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
        )
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            ErrorText(error)
        }
    }
}

@Preview(
    name = "App TextField",
    showBackground = true,
)
@Composable
private fun AppTextFieldPreview() {
    ICompoundTheme {
        var phoneNumber by remember {
            mutableStateOf("")
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            AppTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = "Mobile Number",
                placeholder = "Enter your mobile number",
                keyboardType = KeyboardType.Phone,
            )

            AppTextField(
                value = "12345",
                onValueChange = {},
                label = "Mobile Number",
                placeholder = "Enter your mobile number",
                error = "Please enter a valid phone number.",
                keyboardType = KeyboardType.Phone,
            )

            AppTextField(
                value = "",
                onValueChange = {},
                label = "Full Name",
                placeholder = "Enter your full name",
            )
        }
    }
}
