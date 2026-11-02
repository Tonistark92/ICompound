package com.icompound.component.toolbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.icompound.designsystem.ICompoundTheme

@Composable
fun DefaultTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    centerContent: (@Composable () -> Unit)? = null,
) {
    Column {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            when {
                centerContent != null -> centerContent()

                title != null -> Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                else -> {
//                    Image(
//                        modifier = Modifier
//                            .width(140.dp)
//                            .height(32.dp),
//                        painter = painterResource(R.drawable.kaitech_logo),
//                        contentDescription = null
//                    )
                }
            }

//            NotificationBell(
//                unreadCount = unreadCount,
//                onClick = onNotificationClick,
//            )
        }
        HorizontalDivider(
            color = Color(0xFFC0C7D2),
            thickness = 0.5.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDefaultTopBar() {
    ICompoundTheme {
        DefaultTopBar()
    }
}