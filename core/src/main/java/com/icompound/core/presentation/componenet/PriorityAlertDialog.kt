package com.icompound.core.presentation.componenet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.icompound.designsystem.CornerDimensions
import com.icompound.designsystem.ICompoundTheme
import com.icompound.designsystem.PaddingDimensions
import com.icompound.designsystem.colorText
import com.icompound.designsystem.colorTextHint


/**
 * Non-cancelable priority alert dialog
 * Used for critical alerts that require user acknowledgment
 */
@Composable
fun PriorityAlertDialog(
    title: String,
    message: String,
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* Non-cancelable - do nothing */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PaddingDimensions.high),
            shape = RoundedCornerShape(CornerDimensions.high)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(PaddingDimensions.high),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PaddingDimensions.medium)
            ) {
                // Warning Icon
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Warning",
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                // Title
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                // Message
                Text(text = message, style = MaterialTheme.typography.titleLarge)

                // Action Button
                Text(text = buttonText, style = MaterialTheme.typography.titleLarge)

            }
        }
    }
}
@Preview(showBackground = true, locale = "ar")
@Composable
fun PriorityAlertDialogPreview() {
    ICompoundTheme {
        PriorityAlertDialog(
            title = "انتهت الجلسة",
            message = "انتهت جلستك. يرجى المتابعة كزائر أو تسجيل الدخول مرة أخرى.",
            buttonText = "المتابعة كزائر",
            onButtonClick = {}
        )
    }
}