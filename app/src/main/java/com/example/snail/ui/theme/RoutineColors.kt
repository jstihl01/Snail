package com.example.snail.ui.theme

import androidx.compose.ui.graphics.Color

val RoutineColors = listOf(
    SnailDarkGray,
    Color(0xFF183B56),
    Color(0xFF254D38),
    Color(0xFF49315F),
    Color(0xFF5C2E3A),
    Color(0xFF58401F),
    Color(0xFF1E4B4B),
    Color(0xFF633B24)
)

fun routineColorFor(index: Int): Color = RoutineColors.getOrElse(index) { SnailDarkGray }
