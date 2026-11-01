package com.example.snail

import com.example.snail.ui.models.*
import org.junit.Assert.*
import org.junit.Test

class TrainingProgressTest {
    @Test fun addedSetsAreEmptyAndNavigationIncludesThem() {
        val original = CompletedSet(1, "67,5", "8", "1")
        val extra = newExtraSet(listOf(original), "1")
        assertEquals("", extra.kilograms)
        assertEquals("", extra.repetitions)
        assertTrue(extra.isExtra)
        assertEquals(listOf(TrainingField(0,0,0), TrainingField(0,0,1), TrainingField(0,1,0), TrainingField(0,1,1),
            TrainingField(1,0,0), TrainingField(1,0,1)), trainingFields(listOf(listOf(original, extra), listOf(original))))
    }

    @Test fun volumeExcludesCardioAndAggregatesRepeatedExercises() {
        val press = CompletedExercise("Press", listOf(CompletedSet(1, "10,5", "8", "")), muscleGroup = "Pecho")
        val cardio = CompletedExercise("Bici", listOf(CompletedSet(1,"","","", intensity = "5", minutes = "20")), isCardio = true)
        val history = listOf(SavedWorkout(1,"Día",200, listOf(press, press, cardio), startMotivation = "😞", endMotivation = "😄"))
        assertEquals(168.0, history.volumeProgress().single().value, 0.01)
        assertEquals(168.0, history.exerciseProgress().getValue("Pecho").values.single().single().value, 0.01)
        assertEquals(20.0, history.exerciseProgress().getValue("Cardio").values.single().single().value, 0.01)
        assertEquals(2.0, history.motivationProgress().single().value, 0.01)
    }

    @Test fun cardioRequiresItsOwnValuesAndStrengthIgnoresOptionalRir() {
        assertTrue(CompletedSet(1,"60","8","").isComplete(false))
        assertFalse(CompletedSet(1,"60","8","").isComplete(true))
        assertTrue(CompletedSet(1,"","","", "4,5","20").isComplete(true))
        listOf("", "-1", "NaN", "1,", "1e5").forEach { assertFalse(it.validNumber()) }
    }

    @Test fun mixedRoutineTablesKeepTheSelectedExerciseOrder() {
        val exercises = listOf(RoutineExercise(1, "Press"), RoutineExercise(2, "Bici", isCardio = true),
            RoutineExercise(3, "Remo"), RoutineExercise(4, "Jalón"))
        val sections = exercises.sectionsByExerciseType()
        assertEquals(listOf(1, 1, 2), sections.map { it.size })
        assertEquals(exercises, sections.flatten())
    }
}
