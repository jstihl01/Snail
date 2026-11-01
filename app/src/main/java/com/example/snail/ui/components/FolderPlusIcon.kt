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
fun FolderPlusIcon() {
    Canvas(Modifier.size(24.dp)) {
        val u = size.width / 24
        val path = Path().apply { moveTo(2*u, 6*u); lineTo(9*u, 6*u); lineTo(11*u, 8*u); lineTo(22*u, 8*u)
            lineTo(22*u, 21*u); lineTo(2*u, 21*u); close() }
        drawPath(path, Color.White, style = Stroke(1.6f*u))
        drawLine(Color.White, Offset(8*u, 14*u), Offset(16*u, 14*u), 1.6f*u)
        drawLine(Color.White, Offset(12*u, 10*u), Offset(12*u, 18*u), 1.6f*u)
    }
}
