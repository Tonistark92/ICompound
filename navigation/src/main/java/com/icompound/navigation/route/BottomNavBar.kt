package com.icompound.navigation.route

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaitech.core.presentation.utils.NoRippleInteractionSource
import com.kaitech.navigation.model.BottomAppBarModel
import com.kaitech.navigation.utils.calculateIndicatorOffset
import com.kaitech.navigation.utils.calculateIndicatorWidth

@Composable
fun BottomAppBar(
    currentKey: NavScreen?,
    items: List<BottomAppBarModel>,
    onItemSelected: (BottomAppBarModel) -> Unit
) {
    val selectedIndex = remember(currentKey) {
        items.indexOfFirst {
            it.navScreen::class == currentKey?.let { key -> key::class }
        }.coerceAtLeast(0)
    }

    NavigationBar(
        modifier = Modifier.padding(0.dp),
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
            ) {
                HorizontalDivider(
                    color = Color(0xFFE5E5E5),
                    thickness = 1.dp
                )

                // Primary indicator for selected tab
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = calculateIndicatorOffset(selectedIndex, items.size))
                        .width(calculateIndicatorWidth(items.size))
                        .height(3.dp)
                        .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Row {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = index == selectedIndex,
                        onClick = {
                            if (index != selectedIndex) {
                                onItemSelected(item)
                            }
                        },
                        interactionSource = remember { NoRippleInteractionSource() },
                        icon = {
                            Image(
                                painter = painterResource(
                                    if (selectedIndex == index) item.iconSelected else item.icon
                                ),
                                contentDescription = stringResource(item.title)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(item.title),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedIndex == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color(0xFF434654)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}
