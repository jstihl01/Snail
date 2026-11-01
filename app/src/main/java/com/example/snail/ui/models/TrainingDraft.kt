package com.example.snail.ui.models

data class TrainingField(val exercise: Int, val set: Int, val column: Int)

fun trainingFields(sets: List<List<CompletedSet>>): List<TrainingField> = sets.flatMapIndexed { exercise, rows ->
    rows.flatMapIndexed { set, _ -> listOf(TrainingField(exercise, set, 0), TrainingField(exercise, set, 1)) }
}

fun String.validNumber() = replace(',', '.').toBigDecimalOrNull()?.signum()?.let { it >= 0 } == true &&
    matches(Regex("[0-9]+([.,][0-9]+)?"))

// Existing routines may contain a RIR range; retain it when editing other fields.
fun String.validRirTarget(): Boolean {
    if (isBlank() || validNumber()) return true
    val (minimum, maximum) = splitRepetitionRange(this)
    return minimum.validNumber() && maximum.validNumber()
}

fun CompletedSet.isComplete(cardio: Boolean) = if (cardio) intensity.validNumber() && minutes.validNumber()
    else kilograms.validNumber() && repetitions.validNumber()

fun newExtraSet(sets: List<CompletedSet>, rir: String) = CompletedSet(
    number = (sets.maxOfOrNull { it.number } ?: 0) + 1, kilograms = "", repetitions = "", rir = rir, isExtra = true
)
