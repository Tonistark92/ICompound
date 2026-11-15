package com.icompound.phoneauth.presentation.compoenents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.icompound.core.presentation.componenet.SubBodyText
import com.icompound.core.presentation.componenet.TitleText
import com.icompound.designsystem.ColorLightTokens


@Composable
fun LoadingState(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = ColorLightTokens.SurfaceVariant,
            strokeWidth = 6.dp,
        )
        Spacer(Modifier.height(16.dp))
        TitleText(title, textAlign = TextAlign.Center)
        subtitle?.let { SubBodyText(it, textAlign = TextAlign.Center) }
    }
}

