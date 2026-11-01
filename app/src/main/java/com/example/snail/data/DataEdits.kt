package com.example.snail.data

import com.example.snail.ui.models.RoutineExercise
import com.example.snail.ui.models.SavedRoutine
import com.example.snail.ui.models.SavedWorkout
import com.example.snail.ui.screens.ExerciseItem

/** Edits replace the catalogue entry, leaving the snapshots used elsewhere untouched. */
fun AppData.editRoutine(id: Long, replacementId: Long, name: String, exercises: List<RoutineExercise>): AppData {
    require(replacementId != id && routines.none { it.id == replacementId })
    return copy(routines = routines.map {
        if (it.id == id) SavedRoutine(replacementId, name.trim(), exercises, it.colorIndex, it.folderId) else it
    })
}

fun List<ExerciseItem>.editExercise(id: Long?, replacement: ExerciseItem): List<ExerciseItem> {
    require(replacement.id != id && none { it.id == replacement.id })
    return filterNot { it.id == id } + replacement
}

fun AppData.editWorkout(replacement: SavedWorkout): AppData = copy(workouts = workouts.map {
    if (it.id == replacement.id) replacement.copy(completedAt = it.completedAt) else it
})

fun AppData.moveRoutine(id: Long, direction: Int): AppData {
    require(direction == -1 || direction == 1)
    val routine = routines.firstOrNull { it.id == id } ?: return this
    val siblings = routines.filter { it.folderId == routine.folderId }
    val neighbor = siblings.getOrNull(siblings.indexOf(routine) + direction) ?: return this
    return copy(routines = routines.map {
        when (it.id) { routine.id -> neighbor; neighbor.id -> routine; else -> it }
    })
}

fun AppData.changeRoutineColor(id: Long, color: Int): AppData = copy(
    routines = routines.map { if (it.id == id) it.copy(colorIndex = color) else it },
    workouts = workouts.map { if (it.routineId == id) it.copy(colorIndex = color) else it }
)
