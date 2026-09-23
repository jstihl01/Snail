package com.example.snail

import com.example.snail.ui.models.*
import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingReferenceTest {
    private fun workout(vararg values: Pair<String, String>, name: String = "Press") =
        SavedWorkout(1, "Otra rutina", 0, listOf(CompletedExercise(name,
            values.mapIndexed { index, (kg, reps) -> CompletedSet(index + 1, kg, reps, "") }
        )))

    @Test fun usesMaximumReachingMinimumIncludingRepetitionsAboveRange() {
        val exercise = RoutineExercise(1, "Press", minRepetitions = "6", maxRepetitions = "10")
        assertEquals("85", exercise.kilogramsReference(listOf(
            workout("100" to "5", "80" to "6", "85" to "12"),
            workout("200" to "8", name = "Sentadilla")
        )))
    }

    @Test fun sameExerciseCanHaveDifferentTargetsInSameRoutine() {
        val history = listOf(workout("100" to "5", "80" to "8"))
        assertEquals("100", RoutineExercise(1, "Press", minRepetitions = "5").kilogramsReference(history))
        assertEquals("80", RoutineExercise(2, "Press", minRepetitions = "8", maxRepetitions = "12").kilogramsReference(history))
    }

    @Test fun acceptsBothDecimalSeparatorsAndFormatsWithComma() {
        assertEquals("67,5", RoutineExercise(1, "Press", minRepetitions = "6,5", maxRepetitions = "10").kilogramsReference(
            listOf(workout("70" to "6", "67.5" to "6.5", "60,5" to "8"))
        ))
    }

    @Test fun emptyWhenNoQualifyingHistoryOrInvalidTarget() {
        val exercise = RoutineExercise(1, "Press", minRepetitions = "6", maxRepetitions = "10")
        assertEquals("", exercise.kilogramsReference(emptyList()))
        assertEquals("", exercise.kilogramsReference(listOf(workout("100" to "5", "bad" to "8"))))
        assertEquals("", exercise.copy(minRepetitions = "").kilogramsReference(listOf(workout("100" to "8"))))
    }
}
