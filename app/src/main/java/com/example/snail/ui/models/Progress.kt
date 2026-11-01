package com.example.snail.ui.models

data class ProgressPoint(val date: Long, val value: Double, val label: String)
fun List<CompletedSet>.volume(): Double = sumOf {
    (it.kilograms.replace(',', '.').toDoubleOrNull() ?: 0.0) * (it.repetitions.replace(',', '.').toDoubleOrNull() ?: 0.0)
}
fun List<SavedWorkout>.volumeProgress(): List<ProgressPoint> = sortedBy { it.completedAt }.map {
    ProgressPoint(it.completedAt, it.exercises.filterNot { e -> e.isCardio }.sumOf { e -> e.sets.volume() }, it.routineName)
}
fun List<SavedWorkout>.motivationProgress(): List<ProgressPoint> = sortedBy { it.completedAt }.map {
    ProgressPoint(it.completedAt, (WorkoutMotivations.indexOf(it.endMotivation) - WorkoutMotivations.indexOf(it.startMotivation)).toDouble(), it.routineName)
}
fun List<SavedWorkout>.exerciseProgress(): Map<String, Map<String, List<ProgressPoint>>> {
    val rows = sortedBy { it.completedAt }.flatMap { workout ->
        workout.exercises.groupBy { Triple(exerciseGroupLabel(it.muscleGroup, it.isCardio), it.name, it.isCardio) }.map { (key, exercises) ->
            val value = if (key.third) exercises.sumOf { e -> e.sets.sumOf { s -> s.minutes.replace(',', '.').toDoubleOrNull() ?: 0.0 } }
                else exercises.sumOf { it.sets.volume() }
            Triple(key.first,
                key.second + if (key.third) " · minutos" else " · KG × Reps.", ProgressPoint(workout.completedAt, value, workout.routineName))
        }
    }
    return rows.groupBy { it.first }.toSortedMap().mapValues { (_, grouped) ->
        grouped.groupBy { it.second }.toSortedMap().mapValues { (_, values) -> values.map { it.third } }
    }
}
