package com.example.snail

import com.example.snail.data.*
import com.example.snail.ui.models.*
import com.example.snail.ui.screens.ExerciseItem
import org.junit.Assert.*
import org.junit.Test

class DataEditsTest {
    private val exercise = RoutineExercise(0, "Press", "2", "6", "10", sourceExerciseId = 1)
    private val routine = SavedRoutine(2, "Torso", listOf(exercise), 4, 9)
    private val workout = SavedWorkout(3, "Torso", 1234, listOf(
        CompletedExercise("Press", listOf(CompletedSet(1, "60", "8", "")), sourceExerciseId = 1)), 4, 2)
    private val data = AppData(listOf(ExerciseItem(1, "Press", confirmed = true)), listOf(routine), listOf(workout), listOf(RoutineFolder(9, "A")))

    @Test fun editedRoutineReplacesIdentityButKeepsPositionColorAndFolderAndDetachesHistory() {
        val updated = data.editRoutine(2, 10, "Torso nuevo", listOf(exercise.copy(minRepetitions = "12")))
        assertEquals(10L, updated.routines.single().id)
        assertEquals(4, updated.routines.single().colorIndex)
        assertEquals(9L, updated.routines.single().folderId)
        assertEquals(data.workouts, updated.workouts)
        assertEquals(data.workouts, updated.changeRoutineColor(10, 5).workouts)
    }

    @Test fun exerciseEditLeavesRoutineAndHistorySnapshotsUntouched() {
        val updated = data.copy(exercises = data.exercises.editExercise(1, ExerciseItem(10, "Remo", confirmed = true)))
        assertEquals("Remo", updated.exercises.single().name)
        assertEquals(data.routines, updated.routines)
        assertEquals(data.workouts, updated.workouts)
    }

    @Test fun workoutEditKeepsDateAndDoesNotAlterRoutineOrOtherWorkouts() {
        val replacement = workout.copy(completedAt = 9999, exercises = listOf(workout.exercises.single().copy(
            sets = listOf(CompletedSet(1, "67,5", "9", "")))))
        val updated = data.editWorkout(replacement)
        assertEquals(1234L, updated.workouts.single().completedAt)
        assertEquals("67,5", updated.workouts.single().exercises.single().sets.single().kilograms)
        assertEquals(data.routines, updated.routines)
    }

    @Test fun routineMovementOnlySwapsAdjacentSiblingsAndStopsAtEdges() {
        val original = data.copy(routines = listOf(routine, routine.copy(id = 4, folderId = null), routine.copy(id = 5)))
        assertEquals(original, original.moveRoutine(2, -1))
        val moved = original.moveRoutine(2, 1)
        assertEquals(listOf(5L, 4L, 2L), moved.routines.map { it.id })
        assertEquals(moved, moved.moveRoutine(2, 1))
        assertEquals(original, moved.moveRoutine(2, -1))
    }
}
