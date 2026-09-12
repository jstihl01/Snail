package com.example.snail.ui.models

data class RoutineExercise(
    val id: Long,
    val name: String,
    val series: String = "",
    val repetitions: String = "",
    val rir: String = ""
)

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
