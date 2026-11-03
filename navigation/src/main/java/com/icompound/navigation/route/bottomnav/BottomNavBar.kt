package com.icompound.navigation.route.bottomnav

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icompound.navigation.route.NavScreen
import com.icompound.designsystem.ICompoundTheme



@Composable
fun BottomAppBar(
    currentKey: NavScreen?,
    items: List<BottomAppBarModel>,
    onItemSelected: (BottomAppBarModel) -> Unit,
) {
    val selectedIndex = remember(currentKey) {
        items.indexOfFirst { it.navScreen::class == currentKey?.let { k -> k::class } }
            .coerceAtLeast(0)
    }
    val centerIndex = items.size / 2
    val fabSize = 64.dp
    val shape = remember { NotchedBarShape(notchRadius = fabSize / 2 + 8.dp) }
    val isCenterSelected = selectedIndex == centerIndex

    val centerShadow by animateDpAsState(
        targetValue = if (isCenterSelected) 24.dp else 6.dp,
        animationSpec = tween(durationMillis = 300),
        label = "centerShadow"
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(top = fabSize / 2)               // room for the half of the circle above the bar
                .shadow(8.dp, shape)
                .background(MaterialTheme.colorScheme.background, shape)
                .windowInsetsPadding(NavigationBarDefaults.windowInsets)
                .height(72.dp)
                .fillMaxWidth(),
        ) {
            items.forEachIndexed { index, item ->
                if (index == centerIndex) {
                    Spacer(Modifier.weight(1f))           // the hole under the circle
                } else {
                    NavigationBarItem(
                        selected = index == selectedIndex,
                        onClick = { if (index != selectedIndex) onItemSelected(item) },
                        interactionSource = remember { NoRippleInteractionSource() },
                        icon = {
                            Icon(
                                painter = painterResource(
                                    if (index == selectedIndex) item.iconSelected else item.icon
                                ),
                                contentDescription = stringResource(item.title),
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(item.title),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (index == selectedIndex) MaterialTheme.colorScheme.primary
                                else Color(0xFF434654),
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer // the pill behind Home
                        ),
                    )
                }
            }
        }

        val center = items[centerIndex]
        Surface(
            onClick = {
                if (!isCenterSelected) onItemSelected(center)
            },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = centerShadow,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(fabSize),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(
                        if (isCenterSelected) center.iconSelected else center.icon
                    ),
                    contentDescription = stringResource(center.title),
                )
            }
        }
    }
}


@Preview(
    name = "Bottom App Bar",
    showBackground = true,
    backgroundColor = 0xFFF9F9FF,
)
@Composable
private fun BottomAppBarPreview() {
    ICompoundTheme {
        val items = listOf(
            BottomAppBarModel.Home(),
            BottomAppBarModel.Services(),
            BottomAppBarModel.Location(),   // center slot
            BottomAppBarModel.Bills(),
            BottomAppBarModel.Access(),
        )

        var selectedItem by remember {
            mutableStateOf<BottomAppBarModel>(BottomAppBarModel.Home())
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            BottomAppBar(
                currentKey = selectedItem.navScreen,
                items = items,
                onItemSelected = { selectedItem = it }
            )
        }
    }
}