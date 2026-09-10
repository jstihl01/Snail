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
    val exercises: List<RoutineExercise>
)
