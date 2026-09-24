package com.example.snail.ui.theme

import androidx.compose.ui.graphics.Color

val RoutineColors = listOf(
    SnailDarkGray,
    Color(0xFF3B151E), // Rojo baya
    Color(0xFF431919), // Rojo
    Color(0xFF432615), // Naranja
    Color(0xFF3B3112), // Amarillo
    Color(0xFF1A3320), // Verde
    Color(0xFF16283F), // Azul
    Color(0xFF2E203E)  // Morado
)

fun routineColorFor(index: Int): Color = RoutineColors.getOrElse(index) { SnailDarkGray }

fun routineHistoryColorFor(index: Int): Color = routineColorFor(index).copy(alpha = 0.58f)
