package com.example.snail

import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import androidx.test.platform.app.InstrumentationRegistry
import android.content.Context
import android.content.ContextWrapper
import com.example.snail.data.*
import com.example.snail.ui.models.*
import com.example.snail.ui.screens.ExerciseItem
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DataBackupTest {
    private val sample = AppData(
        exercises = listOf(ExerciseItem(1,"Bici",confirmed = true,note = "Sillín 4", muscleGroup = "Pierna",isCardio = true)),
        routines = listOf(SavedRoutine(2,"Cardio",listOf(RoutineExercise(0,"Bici", "2", note = "Sillín 4", isCardio = true,
            intensity = "3,5", minutes = "20", sourceExerciseId = 1)), 6, 3)),
        folders = listOf(RoutineFolder(3,"Semana")),
        workouts = listOf(SavedWorkout(4,"Cardio",1000,listOf(CompletedExercise("Bici",listOf(
            CompletedSet(1,"","","", "3,5","20",true)),note = "Sillín 4", muscleGroup = "Pierna",
            isCardio = true,sourceExerciseId = 1)),6,2,"🙂","🔥"))
    )
    @Test fun fullRoundTripPreservesAllFields() { assertEquals(sample, DataJson.decode(DataJson.encode(sample))) }
    @Test fun partialImportLeavesOtherCategoriesUntouchedAndDetachesForeignIds() {
        val merged = sample.importData(sample, setOf(ImportCategory.WORKOUTS))
        assertEquals(sample.exercises, merged.exercises)
        assertEquals(sample.routines, merged.routines)
        assertEquals(sample.folders, merged.folders)
        assertEquals(2, merged.workouts.size)
        assertNull(merged.workouts.last().routineId)
        assertNull(merged.workouts.last().exercises.single().sourceExerciseId)
        assertNotEquals(merged.workouts.first().id, merged.workouts.last().id)
    }
    @Test fun fullImportRemapsAllRelationsWithoutCollisions() {
        val result = sample.importData(sample, ImportCategory.entries.toSet())
        assertEquals(result.routines.last().id, result.workouts.last().routineId)
        assertEquals(result.folders.last().id, result.routines.last().folderId)
        assertEquals(result.exercises.last().id, result.routines.last().exercises.single().sourceExerciseId)
        assertEquals(sample.workouts.first(), result.workouts.first())
    }
    @Test fun malformedAndFutureBackupsAreRejectedBeforeAnyWrite() {
        listOf("{}", "broken", DataJson.encode(sample).replace("\"version\": 1", "\"version\": 999"))
            .forEach { assertTrue(runCatching { DataJson.decode(it) }.isFailure) }
    }

    private fun isolatedContext(): Context {
        val prefix = "test-${UUID.randomUUID()}-"
        return object : ContextWrapper(InstrumentationRegistry.getInstrumentation().targetContext) {
            override fun getSharedPreferences(name: String, mode: Int) = super.getSharedPreferences(prefix + name, mode)
        }
    }

    @Test fun storageReloadPreservesAllCategoriesAndTheirLinks() {
        val context = isolatedContext()
        val expected = sample.importData(sample, ImportCategory.entries.toSet())
        AppDataStorage.save(context, expected)
        assertEquals(expected, AppDataStorage.load(context))
    }

    @Test fun legacyMigrationPreservesRangesHistoryAndOriginalFiles() {
        val context = isolatedContext()
        val routineJson = """[{"id":2,"name":"Torso","colorIndex":3,"exercises":[{"id":0,"name":"Press de Pecho","series":"3","repetitions":"6-10","rir":"1-2"}]}]"""
        val workoutJson = """[{"id":4,"routineName":"Torso","completedAt":1000,"colorIndex":3,"exercises":[{"name":"Press de Pecho","sets":[{"number":2,"kilograms":"47,5","repetitions":"8","rir":"1-2"}]}]}]"""
        context.getSharedPreferences("snail_routines", 0).edit().putString("routines", routineJson).commit()
        context.getSharedPreferences("snail_workouts", 0).edit().putString("workouts", workoutJson).commit()
        val result = AppDataStorage.load(context)
        assertEquals("6", result.routines.single().exercises.single().minRepetitions)
        assertEquals("10", result.routines.single().exercises.single().maxRepetitions)
        assertEquals("47,5", result.workouts.single().exercises.single().sets.single().kilograms)
        assertEquals(2L, result.workouts.single().routineId)
        assertEquals(DefaultStartMotivation, result.workouts.single().startMotivation)
        assertEquals(routineJson, context.getSharedPreferences("snail_routines", 0).getString("routines", null))
        assertEquals(workoutJson, context.getSharedPreferences("snail_workouts", 0).getString("workouts", null))
        assertEquals(result, AppDataStorage.load(context))
    }

    @Test fun corruptLegacyFileNeverBecomesAnEmptyNewDatabase() {
        val context = isolatedContext()
        context.getSharedPreferences("snail_routines", 0).edit().putString("routines", "broken").commit()
        assertTrue(runCatching { AppDataStorage.load(context) }.isFailure)
        assertFalse(context.getSharedPreferences("snail_data", 0).contains("snapshot"))
        assertEquals("broken", context.getSharedPreferences("snail_routines", 0).getString("routines", null))
    }

    @Test fun dataSavedByTheAppDoesNotBecomeUnreadableAtAnArbitrarySize() {
        val context = isolatedContext()
        val large = AppData(exercises = listOf(ExerciseItem(1, "Press", confirmed = true, note = "a".repeat(10 * 1024 * 1024 + 1))))
        AppDataStorage.save(context, large)
        assertEquals(large, AppDataStorage.load(context))
    }
}
