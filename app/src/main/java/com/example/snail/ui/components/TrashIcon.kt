package com.example.snail.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun TrashIcon(
    tint: Color = Color.White,
    contentDescription: String = "Eliminar ejercicio"
) {
    Canvas(
        modifier = Modifier
            .size(24.dp)
            .semantics { this.contentDescription = contentDescription }
    ) {
        val strokeWidth = 2.dp.toPx()
        drawLine(
            color = tint,
            start = Offset(size.width * 0.22f, size.height * 0.28f),
            end = Offset(size.width * 0.78f, size.height * 0.28f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.40f, size.height * 0.18f),
            end = Offset(size.width * 0.60f, size.height * 0.18f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.30f, size.height * 0.36f),
            size = Size(size.width * 0.40f, size.height * 0.48f),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )
    }
}
