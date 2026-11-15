package com.icompound.phoneauth.presentation.compoenents.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icompound.core.presentation.componenet.SubBodyText
import com.icompound.core.presentation.componenet.TitleText
import com.icompound.core.presentation.utils.StatusType
import com.icompound.core.presentation.utils.colors
import com.icompound.phoneauth.presentation.AuthStepLayout

@Composable
fun StatusStep(
    type: StatusType,
    icon: Painter,
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
        Spacer(Modifier.height(6.dp))
        details?.let {
            Spacer(Modifier.height(16.dp))
            it()
        }
    }
}

@Composable
fun StatusIconChip(
    type: StatusType,
    icon: Painter,
) {
    val c = type.colors() // make StatusType.colors() `internal` instead of `private`
    Box(
        Modifier
            .size(40.dp)
            .background(c.container, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = c.content, modifier = Modifier.size(20.dp)) }
}
