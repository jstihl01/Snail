package com.example.snail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.snail.ui.components.ActionText
import com.example.snail.ui.components.DeleteConfirmation
import com.example.snail.ui.models.*
import com.example.snail.ui.screens.*
import com.example.snail.ui.theme.SnailTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowDialog
import org.robolectric.shadows.ShadowViewRootImpl
import org.robolectric.util.ReflectionHelpers
import android.view.inputmethod.InputMethodManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NewFeaturesUiTest {
    @get:Rule val compose = createComposeRule()

    private fun field(label: String) = compose.onNodeWithContentDescription(label)
    private fun scrollTo(label: String) {
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasContentDescription(label))
    }

    @Test fun keyboardAdvancesThroughOffscreenAndExtraRowsThenNextExercise() {
        val routine = SavedRoutine(1, "Prueba", listOf(
            RoutineExercise(1, "Press", "8", "6", "10"), RoutineExercise(2, "Remo", "1", "8", rir = "1")))
        compose.setContent { SnailTheme { Box(Modifier.height(500.dp)) {
            NuevoEntrenamientoScreen(routine, emptyList(), {}, { _, _, _ -> })
        } } }
        scrollTo("Añadir serie a Press")
        field("Añadir serie a Press").performClick()
        compose.onNodeWithContentDescription("Serie 9, extra").assertExists()
        scrollTo("Press, serie 1, KG")
        field("Press, serie 1, KG").performClick().performTextInput("47.5")
        field("Press, serie 1, KG").assertTextContains("47,5")
        for (set in 1..9) {
            field("Press, serie $set, KG").assertIsFocused().performImeAction()
            field("Press, serie $set, Reps.").assertIsFocused().assertIsDisplayed().performImeAction()
        }
        field("Remo, serie 1, KG").assertIsFocused().assertIsDisplayed().performImeAction()
        field("Remo, serie 1, Reps.").assertIsFocused().performImeAction()
        field("Remo, serie 1, Reps.").assertIsNotFocused()
    }

    @Test fun createAutomaticallyFocusesNameAndCanDiscardWithoutConfirmation() {
        var items by mutableStateOf(emptyList<ExerciseItem>())
        var id = 1L
        compose.setContent { SnailTheme {
            AñadirEjercicioScreen(items, { items = it }, {}, {}, { id++ })
        } }
        compose.onNodeWithText("Crear Ejercicio").performClick()
        field("Nombre del Ejercicio").assertIsFocused()
        // Robolectric does not deliver the window-focus event to a newly shown Dialog.
        // Deliver the OS event, then verify the app opens the keyboard without a field tap.
        compose.runOnIdle {
            val root = ReflectionHelpers.callInstanceMethod<Any>(ShadowDialog.getLatestDialog().window!!.decorView, "getViewRootImpl")
            Shadow.extract<ShadowViewRootImpl>(root).callWindowFocusChanged(true)
        }
        compose.waitForIdle()
        compose.runOnIdle {
            val keyboard = ShadowDialog.getLatestDialog().context.getSystemService(InputMethodManager::class.java)
            assertTrue(shadowOf(keyboard).isSoftInputVisible)
        }
        field("Nombre del Ejercicio").performTextInput("press")
        field("Nombre del Ejercicio").assertTextContains("Press")
        field("Descartar ejercicio en creación").performClick()
        compose.onNodeWithText(DeleteConfirmation).assertDoesNotExist()
        compose.runOnIdle { assertTrue(items.isEmpty()) }
        compose.onNodeWithText("Crear Ejercicio").performClick()
        field("Nombre del Ejercicio").performTextInput("remo")
        compose.onNodeWithText("Guardar").performClick()
        compose.runOnIdle {
            assertEquals("Remo", items.single().name)
            assertTrue(items.single().confirmed)
            assertTrue(items.single().selected)
            assertEquals(1, items.single().selectionOrder)
        }
    }

    @Test fun folderExpansionAndRoutineMenusAreIndependent() {
        var routines by mutableStateOf(listOf(SavedRoutine(1, "Torso", emptyList(), folderId = 10),
            SavedRoutine(2, "Pierna", emptyList(), folderId = 11)))
        var folders by mutableStateOf(listOf(RoutineFolder(10, "A"), RoutineFolder(11, "B")))
        compose.setContent { SnailTheme {
            MisRutinasScreen(routines, folders, {}, {}, { id, color ->
                routines = routines.map { if (it.id == id) it.copy(colorIndex = color) else it }
            }, {}, {}, { _, _ -> }, { _, _ -> }, { folders = folders + RoutineFolder(12, it) })
        } }
        compose.onNodeWithText("A (1)").performClick()
        compose.onNodeWithText("Torso").assertDoesNotExist()
        compose.onNodeWithText("Pierna").assertIsDisplayed()
        compose.onNodeWithText("B (1)").performClick()
        compose.onNodeWithText("A (1)").performClick()
        compose.onNodeWithText("Pierna").assertDoesNotExist()
        compose.onNodeWithText("Torso").assertIsDisplayed()
        field("Opciones de Torso").performClick()
        compose.onNodeWithText("Mover arriba").assertIsNotEnabled()
        compose.onNodeWithText("Cambiar color").performClick()
        // Opening the color submenu must not crash intrinsic menu measurement.
        compose.waitForIdle()
    }

    @Test fun cardioUsesIntensityAndMinutesAndStrengthWithoutRirHidesIt() {
        val routine = SavedRoutine(1, "Cardio", listOf(RoutineExercise(1, "Bici", "1", isCardio = true,
            intensity = "3", minutes = "20")))
        var saved: List<CompletedExercise>? = null
        compose.setContent { SnailTheme { NuevoEntrenamientoScreen(routine, emptyList(), {}, { exercises, _, _ -> saved = exercises }) } }
        compose.onNodeWithText("RIR").assertDoesNotExist()
        compose.onNodeWithText("KG").assertDoesNotExist()
        field("Bici, serie 1, Intensidad").performTextInput("4")
        field("Bici, serie 1, Minutos").performTextInput("25")
        compose.onNodeWithText("Guardar").assertIsNotEnabled()
        scrollTo("Motivación inicial, 🙂")
        field("Motivación inicial, 🙂").performClick()
        scrollTo("Motivación final, 🔥")
        field("Motivación final, 🔥").performClick()
        compose.onNodeWithText("Guardar").assertIsEnabled().performClick()
        compose.runOnIdle {
            assertEquals("4", saved!!.single().sets.single().intensity)
            assertEquals("25", saved!!.single().sets.single().minutes)
            assertTrue(saved!!.single().isCardio)
        }
    }

    @Test fun longButtonLabelFitsAtLargeFontScaleWithoutWrappingOrClipping() {
        compose.setContent { SnailTheme {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                Button({}, Modifier.width(135.dp)) { ActionText("+ Entrenamiento") }
            }
        } }
        val results = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText("+ Entrenamiento").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        assertEquals(1, results.single().lineCount)
        assertFalse(results.single().hasVisualOverflow)
    }

    @Test fun strengthWithoutRirKeepsOnlyKgAndRepetitionsIncludingExtraSets() {
        val routine = SavedRoutine(1, "Fuerza", listOf(RoutineExercise(1, "Press", "1", "6")))
        compose.setContent { SnailTheme { NuevoEntrenamientoScreen(routine, emptyList(), {}, { _, _, _ -> }) } }
        compose.onNodeWithText("RIR").assertDoesNotExist()
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(2)
        field("Añadir serie a Press").performClick()
        compose.onNodeWithText("RIR").assertDoesNotExist()
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(4)
    }
}
