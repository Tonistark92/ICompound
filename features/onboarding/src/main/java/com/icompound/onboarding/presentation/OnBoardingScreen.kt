package com.icompound.onboarding.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icompound.designsystem.ICompoundTheme
import com.icompound.onboarding.R

@Composable
fun OnBoardingScreenFirst(modifier: Modifier = Modifier) {

}

@Composable
fun BottomGradientOverlay(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to ICompoundTheme.colorScheme.primary.copy(alpha = 0.4f),
                        0.2f to ICompoundTheme.colorScheme.primary.copy(alpha = 0.15f),
                        0.1f to ICompoundTheme.colorScheme.primary.copy(alpha = 0.4f),
                        1.0f to ICompoundTheme.colorScheme.secondary.copy(alpha = 1.0f)
                    )
                )
            )
    )
}

@Preview(showBackground = true)
@Composable
private fun BottomGradientOverlayPreview() {
    ICompoundTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.img_onboarding1),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            BottomGradientOverlay(
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            Text(
                text = "A better way to live.",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            )
        }
    }
}