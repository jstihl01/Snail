package com.example.snail.ui.models

data class RoutineExercise(
    val id: Long,
    val name: String,
    val series: String = "",
    val minRepetitions: String = "",
    val maxRepetitions: String = "",
    val rir: String = "",
    val note: String = "",
    val muscleGroup: String = "",
    val isCardio: Boolean = false,
    val intensity: String = "",
    val minutes: String = "",
    val sourceExerciseId: Long? = null
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
    val colorIndex: Int = 0,
    val folderId: Long? = null
)

data class CompletedSet(
    val number: Int,
    val kilograms: String,
    val repetitions: String,
    val rir: String,
    val intensity: String = "",
    val minutes: String = "",
    val isExtra: Boolean = false
)

data class CompletedExercise(
    val name: String,
    val sets: List<CompletedSet>,
    val note: String = "",
    val muscleGroup: String = "",
    val isCardio: Boolean = false,
    val targetRepetitions: String = "",
    val targetRir: String = "",
    val sourceExerciseId: Long? = null
)

data class SavedWorkout(
    val id: Long,
    val routineName: String,
    val completedAt: Long,
    val exercises: List<CompletedExercise>,
    val colorIndex: Int = 0,
    val routineId: Long? = null,
    val startMotivation: String = DefaultStartMotivation,
    val endMotivation: String = DefaultEndMotivation
)

const val DefaultStartMotivation = "😞"
const val DefaultEndMotivation = "😄"
val WorkoutMotivations = setOf("🤕", "😞", "🙂", "😄", "🔥")

data class RoutineFolder(val id: Long, val name: String)

/** Keep selection order when strength and cardio need different table headings. */
fun List<RoutineExercise>.sectionsByExerciseType(): List<List<RoutineExercise>> = buildList<MutableList<RoutineExercise>> {
    for (exercise in this@sectionsByExerciseType) {
        val section = lastOrNull()
        if (section != null && section.first().isCardio == exercise.isCardio) section.add(exercise)
        else add(mutableListOf(exercise))
    }
}

fun normalizedRoutineColorIndex(index: Int): Int = when (index) {
    in 0..7 -> index
    8, 9 -> 7
    10 -> 1
    else -> 0
}
