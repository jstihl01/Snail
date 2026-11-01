package com.example.snail.ui.components

import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

/** Fit the entire label on one line, including with large system font settings. */
@Composable
fun ActionText(text: String, modifier: Modifier = Modifier) {
    val style = LocalTextStyle.current
    val base = if (style.fontSize.value.isNaN()) 14.sp else style.fontSize
    Text(text, modifier, maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
        autoSize = TextAutoSize.StepBased(minFontSize = 1.sp, maxFontSize = base, stepSize = 0.5.sp))
}
