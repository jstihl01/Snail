package com.example.snail.ui.models

private val repetitionTarget = Regex("^([0-9]+(?:[.,][0-9]+)?)(?:\\s*[-–—]\\s*([0-9]+(?:[.,][0-9]+)?))?$")

fun RoutineExercise.kilogramsReference(workouts: List<SavedWorkout>): String {
    val target = repetitionTarget.matchEntire(repetitions.trim()) ?: return ""
    val minimum = target.groupValues.drop(1)
        .filter { it.isNotEmpty() }
        .map { it.replace(',', '.').toBigDecimal() }
        .minOrNull() ?: return ""

    return workouts.asSequence()
        .flatMap { it.exercises.asSequence() }
        .filter { it.name == name }
        .flatMap { it.sets.asSequence() }
        .filter { set ->
            val performed = set.repetitions.trim().replace(',', '.').toBigDecimalOrNull()
            performed != null && performed >= minimum
        }
        .mapNotNull { it.kilograms.trim().replace(',', '.').toBigDecimalOrNull() }
        .filter { it.signum() >= 0 }
        .maxOrNull()
        ?.stripTrailingZeros()
        ?.toPlainString()
        ?.replace('.', ',')
        .orEmpty()
}
