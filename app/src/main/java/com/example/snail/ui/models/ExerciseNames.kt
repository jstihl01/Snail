package com.example.snail.ui.models

import java.util.Locale

private val exerciseWord = Regex("[\\p{L}\\p{M}\\p{N}]+")
private val nameConnectors = setOf(
    "de", "del", "al", "a", "con", "en", "sin", "por", "para",
    "y", "e", "o", "u", "el", "la", "los", "las", "un", "una", "unos", "unas"
)
private val spanishLocale = Locale.forLanguageTag("es")

fun String.toExerciseTitle(): String {
    if (exerciseWord.findAll(this).count() < 2) return this
    return exerciseWord.replace(this) { match ->
        val word = match.value
        val lowercase = word.lowercase(spanishLocale)
        when {
            lowercase in nameConnectors -> lowercase
            word.length > 1 && word.all { it.isUpperCase() } -> word
            else -> lowercase.replaceFirstChar { it.titlecase(spanishLocale) }
        }
    }
}
