package com.icompound.navigation.route.bottomnav

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp


class NotchedBarShape(
    private val notchRadius: Dp,
    private val cornerRadius: Dp = 24.dp,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = with(density) { notchRadius.toPx() }
        val c = with(density) { cornerRadius.toPx() }
        val cx = size.width / 2f

        val path = Path().apply {
            moveTo(0f, c)
            arcTo(Rect(0f, 0f, 2 * c, 2 * c), 180f, 90f, false)               // top-left corner
            lineTo(cx - r, 0f)
            arcTo(Rect(cx - r, -r, cx + r, r), 180f, -180f, false)            // the notch
            lineTo(size.width - c, 0f)
            arcTo(Rect(size.width - 2 * c, 0f, size.width, 2 * c), 270f, 90f, false) // top-right corner
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}