package com.example.snail.ui.models

import java.util.Locale

fun exerciseGroupLabel(muscleGroup: String, isCardio: Boolean): String {
    val locale = Locale.forLanguageTag("es")
    return muscleGroup.trim().ifBlank { if (isCardio) "Cardio" else "Sin grupo muscular" }
        .lowercase(locale).replaceFirstChar { it.titlecase(locale) }
}
