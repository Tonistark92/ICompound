package com.icompound.onboarding.presentation

import android.app.Activity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.airbnb.lottie.model.content.CircleShape
import com.icompound.core.presentation.utils.clickableNoRipple
import com.icompound.designsystem.ICompoundTheme
import com.icompound.onboarding.R
import com.icompound.onboarding.presentation.components.KenBurnsImage
import com.icompound.onboarding.presentation.components.Reveal
import kotlinx.coroutines.launch
@Composable
fun OnBoardingScreen(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {},
)
{

    val title1 = stringResource(R.string.onboarding_title1)
    val description1 = stringResource(R.string.onboarding_description)
    val title2 = stringResource(R.string.onboarding_title2)
    val requests = stringResource(R.string.requests)
    val qrPasses = stringResource(R.string.qr_passes)
    val bills = stringResource(R.string.bills)
    val facilities = stringResource(R.string.facilities)


    val pages = remember {
        listOf(
            OnboardingPageUi(
                image = R.drawable.img_onboarding1,
                title = title1,
                subtitle = description1,
            ),
            OnboardingPageUi(
                image = R.drawable.img_onboarding2,
                title = title2,
                tiles = listOf(
                    FeatureTile(requests, R.drawable.ic_wrench),
                    FeatureTile(qrPasses, R.drawable.ic_qrcode),
                    FeatureTile(bills, R.drawable.ic_bills),
                    FeatureTile(facilities, R.drawable.ic_city),
                ),
            ),
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.lastIndex

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        val previous = controller.isAppearanceLightStatusBars
        controller.isAppearanceLightStatusBars = false     // white icons
        onDispose { controller.isAppearanceLightStatusBars = previous }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        val position = pagerState.currentPage + pagerState.currentPageOffsetFraction
        pages.forEachIndexed { index, page ->
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val delta = index - position
                        translationX = delta.coerceAtLeast(0f) * size.width * 0.35f
                        alpha = if (delta > 0f) (1f - delta).coerceIn(0f, 1f) else 1f
                    }
            ) {
                KenBurnsImage(res = page.image)
                // placed it here to make it follow the image animation
                CustomGradientOverlay()
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

            PageContent(
                page = pages[page],
                visible = started && pagerState.currentPage == page,
                modifier = Modifier.graphicsLayer {
                    translationX = pageOffset * size.width * 0.1f
                    alpha = 1f - kotlin.math.abs(pageOffset).coerceIn(0f, 1f)
                },
            )
        }

        AnimatedVisibility(
            visible = !isLast,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.skip),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.clickableNoRipple(onFinished),
            )
        }

        AnimatedContent(
            targetState = isLast,
            label = "controls",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        )
        { last ->
            if (last) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    PageIndicator(pages.size, pagerState.currentPage)
                    Spacer(Modifier.height(16.dp))
                    OnboardingButton(
                        text = stringResource(R.string.get_started),
                        onClick = onFinished,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PageIndicator(pages.size, pagerState.currentPage)
                    OnboardingButton(
                        text = stringResource(R.string.next),
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        },
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Page content
// ---------------------------------------------------------------------------

@Composable
private fun PageContent(
    page: OnboardingPageUi,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .navigationBarsPadding()
            .padding(bottom = ControlsHeight),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Reveal(visible, index = 0) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
            )
        }

        page.subtitle?.let {
            Spacer(Modifier.height(8.dp))
            Reveal(visible, index = 1) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }
        }

        if (page.tiles.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            page.tiles.chunked(2).forEachIndexed { rowIndex, row ->
                if (rowIndex > 0) Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    row.forEachIndexed { colIndex, tile ->
                        Reveal(
                            visible = visible,
                            index = 1 + rowIndex * 2 + colIndex,
                            modifier = Modifier.weight(1f),
                        ) { FeatureTileCard(tile) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureTileCard(
    tile: FeatureTile,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(tile.icon!!),
                    contentDescription = null,
                    modifier = modifier.size(30.dp)
                )
            }
            Text(
                text = tile.label,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Controls
// ---------------------------------------------------------------------------

@Composable
private fun PageIndicator(pageCount: Int, current: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(pageCount) { i ->
            val selected = i == current
            val width: Dp by animateDpAsState(
                targetValue = if (selected) 28.dp else 16.dp,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "dotWidth",
            )
            val color by animateColorAsState(
                targetValue = if (selected) ICompoundTheme.colorScheme.tertiary else Color.White,
                label = "dotColor",
            )
            Box(
                Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun OnboardingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = ICompoundTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(8.dp))
            Icon(painter = painterResource(R.drawable.ic_right), contentDescription = "Right Icon")
        }
    }
}

// ---------------------------------------------------------------------------
// Gradient (alpha fixed to 1.0f)
// ---------------------------------------------------------------------------

@Composable
fun CustomGradientOverlay(modifier: Modifier = Modifier) {
    val brand = ICompoundTheme.colorScheme.primary

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to brand.copy(alpha = 0.55f),
                            0.18f to brand.copy(alpha = 0.20f),
                            0.35f to brand.copy(alpha = 0.10f),
                        )
                    )
                )
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.35f to brand.copy(alpha = 0.25f),
                            0.65f to brand.copy(alpha = 0.40f),
                            1.00f to brand.copy(alpha = 1.00f),
                        )
                    )
                )
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.45f to Color.Black.copy(alpha = 0f),
                            0.70f to Color.Black.copy(alpha = 0.30f),
                            0.88f to Color.Black.copy(alpha = 0.60f),
                            1.00f to Color.Black.copy(alpha = 0.65f),
                        )
                    )
                )
        )
    }
}

// ---------------------------------------------------------------------------
// Content model
// ---------------------------------------------------------------------------

private data class FeatureTile(
    val label: String,
    @DrawableRes val icon: Int? = null,
)

private data class OnboardingPageUi(
    @DrawableRes val image: Int,
    val title: String,
    val subtitle: String? = null,
    val tiles: List<FeatureTile> = emptyList(),
)

private val ControlsHeight = 120.dp   // space reserved for the indicator and button


//@Preview(name = "Page 1 - final state", showBackground = true, showSystemUi = true)
//@Composable
//private fun Page1Preview() {
//    ICompoundTheme {
//        Box(
//            Modifier
//                .fillMaxSize()
//                .background(Color.Black)
//        ) {
//            KenBurnsImage(res = R.drawable.img_onboarding1)
//            CustomGradientOverlay()
//            PageContent(
//                page = OnboardingPageUi(
//                    image = R.drawable.img_onboarding1,
//                    title = "A better way to live.",
//                    subtitle = "Gate passes, repairs and bills for your home, all in one calm place.",
//                ),
//                visible = true,
//            )
//        }
//    }
//}

@Preview(name = "Page 2 - final state", showBackground = true, showSystemUi = true)
@Composable
private fun Page2Preview() {
    ICompoundTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            KenBurnsImage(res = R.drawable.img_onboarding2)
            CustomGradientOverlay()
            PageContent(
                page = OnboardingPageUi(
                    image = R.drawable.img_onboarding2,
                    title = "Everything in one place.",
                    tiles = listOf(
                        FeatureTile("Requests"), FeatureTile("QR Passes"),
                        FeatureTile("Bills"), FeatureTile("Facilities"),
                    ),
                ),
                visible = true,
            )
        }
    }
}

//@Preview(name = "Small phone", device = "spec:width=360dp,height=640dp", showSystemUi = true)
//@Composable
//private fun SmallScreenPreview() {
//    ICompoundTheme { OnBoardingScreen() }
//}

//@Preview(name = "Full screen (use Interactive mode)", showSystemUi = true)
//@Composable
//private fun FullScreenPreview() {
//    ICompoundTheme { OnBoardingScreen() }
//}