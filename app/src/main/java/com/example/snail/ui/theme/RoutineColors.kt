package com.example.snail.ui.theme

import androidx.compose.ui.graphics.Color

val RoutineColors = listOf(
    SnailDarkGray,
    Color(0xFF6B2737), // Rojo baya
    Color(0xFF7A2E2E), // Rojo
    Color(0xFF7A4524), // Naranja
    Color(0xFF6B5A20), // Amarillo
    Color(0xFF2F5D3A), // Verde
    Color(0xFF1F5B5B), // Cian
    Color(0xFF3F5F8A), // Azul aciano
    Color(0xFF284A73), // Azul
    Color(0xFF533A70), // Morado
    Color(0xFF6E315F)  // Magenta
)

fun routineColorFor(index: Int): Color = RoutineColors.getOrElse(index) { SnailDarkGray }

fun routineHistoryColorFor(index: Int): Color = routineColorFor(index).copy(alpha = 0.55f)
