package com.example.snail.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun PaletteIcon(tint: Color = Color.White) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val unit = size.width / 24f
        val outline = Path().apply {
            moveTo(12f * unit, 2f * unit)
            cubicTo(6f * unit, 2f * unit, 2f * unit, 6f * unit, 2f * unit, 12f * unit)
            cubicTo(2f * unit, 18f * unit, 6f * unit, 22f * unit, 12f * unit, 22f * unit)
            cubicTo(15f * unit, 22f * unit, 16f * unit, 20f * unit, 14f * unit, 18f * unit)
            cubicTo(12f * unit, 16f * unit, 14f * unit, 14f * unit, 17f * unit, 14f * unit)
            lineTo(19f * unit, 14f * unit)
            cubicTo(24f * unit, 14f * unit, 22f * unit, 2f * unit, 12f * unit, 2f * unit)
            close()
        }
        drawPath(outline, tint, style = Stroke(width = 2.5f * unit))
        listOf(7f to 9f, 11f to 6f, 16f to 7f, 6f to 14f).forEach { (x, y) ->
            drawCircle(tint, radius = 1.3f * unit, center = Offset(x * unit, y * unit))
        }
    }
}
