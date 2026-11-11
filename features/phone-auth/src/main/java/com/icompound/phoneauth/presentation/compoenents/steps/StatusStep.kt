package com.icompound.phoneauth.presentation.compoenents.steps

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.icompound.core.presentation.componenet.SubBodyText
import com.icompound.core.presentation.componenet.TitleText
import com.icompound.core.presentation.utils.StatusType
import com.icompound.phoneauth.presentation.AuthStepLayout
import com.icompound.phoneauth.presentation.StatusIconChip

@Composable
fun StatusStep(
    type: StatusType,
    icon: ImageVector,
    title: String,
    body: String,
    primary: @Composable ColumnScope.() -> Unit,
    secondary: (@Composable ColumnScope.() -> Unit)? = null,
    details: (@Composable ColumnScope.() -> Unit)? = null,
) {
    AuthStepLayout(bottom = {
        primary()
        secondary?.invoke(this)
    }) {
        StatusIconChip(type, icon) // small rounded chip, colors from type
        Spacer(Modifier.height(12.dp))
        TitleText(title)
        Spacer(Modifier.height(6.dp))
        SubBodyText(body)
        details?.let {
            Spacer(Modifier.height(16.dp))
            it()
        }
    }
}
