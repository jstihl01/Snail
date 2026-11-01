package com.example.snail

import android.content.Intent
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.core.content.FileProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.snail.data.*
import com.example.snail.ui.models.*
import com.example.snail.ui.screens.ExerciseItem
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.After
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h740dp-xhdpi", shadows = [AndroidFilePathsShadow::class],
    instrumentedPackages = ["androidx.core.content"])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AppFlowsUiTest {
    @get:Rule val compose = createEmptyComposeRule()
    private lateinit var scenario: ActivityScenario<MainActivity>
    private lateinit var activity: MainActivity

    private val routine = SavedRoutine(2, "Torso", listOf(RoutineExercise(0, "Press", "2", "6", "10", "1-2",
        note = "Banco inclinado", muscleGroup = "Pecho", sourceExerciseId = 1)), 3, 9)
    private val workout = SavedWorkout(3, "Torso", 1793370000000, listOf(CompletedExercise("Press",
        listOf(CompletedSet(1, "60", "8", "1-2")), "Banco inclinado", "Pecho", targetRepetitions = "6-10", targetRir = "1-2", sourceExerciseId = 1)), 3, 2)
    private val data = AppData(listOf(ExerciseItem(1, "Press", confirmed = true)), listOf(routine), listOf(workout), listOf(RoutineFolder(9, "Fuerza")))

    private fun seed(value: AppData = data) {
        // Prepare persisted data before launching, as on an ordinary app restart.
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        AppDataStorage.save(context, value)
        FileProvider::class.java.getDeclaredField("sCache").apply { isAccessible = true }
            .let { (it.get(null) as MutableMap<*, *>).clear() }
        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity = it }
        compose.waitForIdle()
    }

    @After fun closeActivity() { if (::scenario.isInitialized) scenario.close() }

    private fun screenshot(name: String) {
        val file = File("build/reports/screenshots/$name.png")
        requireNotNull(file.parentFile).mkdirs()
        compose.runOnIdle {
            val view = activity.window.decorView
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            file.outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        }
    }

    @Test fun historyEditUsesTrainingFormAndPreservesDateAndRoutine() {
        seed()
        compose.onNodeWithContentDescription("Editar entrenamiento").performClick()
        compose.onNodeWithText("Cancelar").assertIsDisplayed()
        compose.onNodeWithText("Guardar").assertIsDisplayed()
        screenshot("nuevo-entrenamiento")
        compose.onNodeWithContentDescription("Press, serie 1, KG").assertTextContains("60").performTextReplacement("67.5")
        compose.onNodeWithText("Guardar").performClick()
        compose.onNodeWithText("67,5 KG × 8 Reps.").assertExists()
        val saved = AppDataStorage.load(activity)
        assertEquals(workout.id, saved.workouts.single().id)
        assertEquals(workout.completedAt, saved.workouts.single().completedAt)
        assertEquals(data.routines, saved.routines)
        screenshot("historial")
    }

    @Test fun routineEditPreservesLegacyRirAndReplacesRoutineWithoutChangingHistory() {
        seed()
        compose.onNodeWithText("+ Entrenamiento").performClick()
        compose.onNodeWithContentDescription("Opciones de Torso").performClick()
        compose.onNodeWithText("Editar").performClick()
        compose.onNodeWithText("Torso").performTextReplacement("Torso A")
        compose.onNodeWithContentDescription("Press, Mín. Reps.*").performTextReplacement("7")
        screenshot("nueva-rutina")
        compose.onNodeWithText("Guardar").assertIsEnabled().performClick()
        val saved = AppDataStorage.load(activity)
        assertEquals("Torso A", saved.routines.single().name)
        assertEquals(routine.folderId, saved.routines.single().folderId)
        assertEquals(routine.colorIndex, saved.routines.single().colorIndex)
        assertNotEquals(routine.id, saved.routines.single().id)
        assertEquals(data.workouts, saved.workouts)
        screenshot("rutinas")
    }

    @Test fun exportImmediatelyOpensShareChooserWithReadableCompleteBackup() {
        seed()
        compose.onNodeWithContentDescription("Exportar o importar datos").performClick()
        compose.onNodeWithText("Exportar y compartir").performClick()
        var chooser: Intent? = null
        try {
            compose.waitUntil(10_000) {
                compose.waitForIdle()
                chooser = chooser ?: shadowOf(activity.application).nextStartedActivity
                chooser != null
            }
        } catch (failure: Throwable) {
            val roots = compose.onAllNodes(isRoot())
            throw AssertionError(roots.fetchSemanticsNodes().indices.joinToString("\n") { roots[it].printToString() }, failure)
        }
        assertEquals(Intent.ACTION_CHOOSER, chooser!!.action)
        @Suppress("DEPRECATION") val send = chooser!!.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("application/json", send.type)
        assertTrue(send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        @Suppress("DEPRECATION") val uri = send.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)!!
        assertEquals("content", uri.scheme)
        val json = activity.contentResolver.openInputStream(uri)!!.bufferedReader().use { it.readText() }
        assertEquals(data, DataJson.decode(json))
    }

    // Robolectric's native Dialog+TextField measurement loops above its default 320dp width.
    // https://github.com/robolectric/robolectric/issues/8460
    @Test @Config(qualifiers = "w320dp-h740dp-xhdpi") fun folderCreationAssignmentAndColorArePersisted() {
        seed()
        compose.onNodeWithText("+ Entrenamiento").performClick()
        compose.onNodeWithContentDescription("Crear carpeta").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("Semana")
        compose.onNodeWithText("Crear").performClick()
        compose.onNodeWithContentDescription("Opciones de Torso").performClick()
        compose.onNodeWithText("Mover a carpeta").performClick()
        compose.onNodeWithText("Semana").performClick()
        val saved = AppDataStorage.load(activity)
        assertEquals(saved.folders.last().id, saved.routines.single().folderId)
        compose.onNodeWithContentDescription("Opciones de Torso").performClick()
        compose.onNodeWithText("Cambiar color").performClick()
        compose.onNodeWithContentDescription("Color 6").performClick()
        assertEquals(6, AppDataStorage.load(activity).routines.single().colorIndex)
        assertEquals(6, AppDataStorage.load(activity).workouts.single().colorIndex)
    }

    @Test fun importingChosenCategoryThroughPickerPreservesExistingData() {
        seed()
        val file = File(activity.cacheDir, "exports/import-test.json")
        requireNotNull(file.parentFile).mkdirs()
        file.writeText(DataJson.encode(data.copy(exercises = listOf(ExerciseItem(7, "Bici", confirmed = true, isCardio = true)))))
        val uri = FileProvider.getUriForFile(activity, "com.example.snail.files", file)
        compose.onNodeWithContentDescription("Exportar o importar datos").performClick()
        compose.onNodeWithText("Importar archivo").performClick()
        val request = shadowOf(activity).nextStartedActivityForResult
        assertEquals(Intent.ACTION_OPEN_DOCUMENT, request.intent.action)
        compose.runOnUiThread { shadowOf(activity).receiveResult(request.intent, Activity.RESULT_OK, Intent().setData(uri)) }
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Importar selección").fetchSemanticsNodes().isNotEmpty() }
        compose.onAllNodes(isToggleable())[1].performClick()
        compose.onAllNodes(isToggleable())[2].performClick()
        compose.onNodeWithText("Importar selección").performClick()
        val result = AppDataStorage.load(activity)
        assertEquals(listOf("Press", "Bici"), result.exercises.map { it.name })
        assertEquals(data.routines, result.routines)
        assertEquals(data.folders, result.folders)
        assertEquals(data.workouts, result.workouts)
    }

    @Test @Config(qualifiers = "w320dp-h740dp-xhdpi") fun exerciseEditingReplacesCatalogueEntryAndRetainsRoutineSnapshots() {
        seed()
        compose.onNodeWithText("+ Rutina").performClick()
        compose.onNodeWithText("Añadir Ejercicio").performClick()
        compose.onNodeWithContentDescription("Editar Press").performClick()
        compose.onNodeWithContentDescription("Nombre del Ejercicio").performTextReplacement("remo")
        compose.onNodeWithText("Grupo muscular (opcional)").performTextInput("Espalda")
        compose.onNodeWithText("Nota (opcional)").performTextInput("Sentado")
        compose.onNodeWithText("Guardar").performClick()
        val result = AppDataStorage.load(activity)
        assertEquals("Remo", result.exercises.single().name)
        assertEquals("Espalda", result.exercises.single().muscleGroup)
        assertEquals("Sentado", result.exercises.single().note)
        assertNotEquals(1L, result.exercises.single().id)
        assertEquals(data.routines, result.routines)
        assertEquals(data.workouts, result.workouts)
        screenshot("ejercicios")
    }

    @Test fun progressDisplaysWorkoutVolumeMotivationAndMuscleGroups() {
        seed(data.copy(workouts = listOf(workout, workout.copy(id = 4, completedAt = workout.completedAt + 86_400_000,
            endMotivation = "🔥"))))
        compose.onNodeWithText("Progreso").performClick()
        compose.onNodeWithText("Volumen total · KG × Reps.").assertIsDisplayed()
        compose.onNodeWithText("Motivación · final − inicial").assertExists()
        screenshot("progreso")
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Press · KG × Reps."))
        compose.onNodeWithText("Press · KG × Reps.").assertIsDisplayed()
    }
}
