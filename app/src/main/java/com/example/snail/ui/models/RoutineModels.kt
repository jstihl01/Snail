package com.example.snail.ui.models

data class RoutineExercise(
    val id: Long,
    val name: String,
    val series: String = "",
    val minRepetitions: String = "",
    val maxRepetitions: String = "",
    val rir: String = ""
) {
    val repetitions: String
        get() = if (maxRepetitions.isBlank()) minRepetitions else "$minRepetitions-$maxRepetitions"
}

fun splitRepetitionRange(value: String): Pair<String, String> {
    val parts = value.trim().replace('.', ',').split(Regex("\\s*[-–—]\\s*"))
    if (parts.size == 2) {
        val first = parts[0].replace(',', '.').toBigDecimalOrNull()
        val second = parts[1].replace(',', '.').toBigDecimalOrNull()
        if (first != null && second != null) {
            return if (first <= second) parts[0] to parts[1] else parts[1] to parts[0]
        }
    }
    return value.trim().replace('.', ',') to ""
}

data class SavedRoutine(
    val id: Long,
    val name: String,
    val exercises: List<RoutineExercise>,
    val colorIndex: Int = 0
)

data class CompletedSet(
    val number: Int,
    val kilograms: String,
    val repetitions: String,
    val rir: String
)

data class CompletedExercise(
    val name: String,
    val sets: List<CompletedSet>
)

data class SavedWorkout(
    val id: Long,
    val routineName: String,
    val completedAt: Long,
    val exercises: List<CompletedExercise>,
    val colorIndex: Int = 0,
    val routineId: Long? = null
)
