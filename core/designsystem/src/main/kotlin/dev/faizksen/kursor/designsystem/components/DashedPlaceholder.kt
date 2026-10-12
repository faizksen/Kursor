package dev.faizksen.kursor.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DashedPlaceholder(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    cornerRadius: Dp = 16.dp,
    strokeWidth: Dp = 2.dp,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    Canvas(modifier.size(size)) {
        val stroke = strokeWidth.toPx()
        val inset = stroke / 2f
        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(
                        left = inset,
                        top = inset,
                        right = this@Canvas.size.width - inset,
                        bottom = this@Canvas.size.height - inset
                    ),
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            )
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = stroke,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))
            )
        )
    }
}