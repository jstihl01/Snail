package com.example.snail

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.snail.data.ExerciseStorage
import com.example.snail.data.RoutineStorage
import com.example.snail.ui.models.RoutineExercise
import com.example.snail.ui.models.SavedRoutine
import com.example.snail.ui.screens.AñadirEjercicioScreen
import com.example.snail.ui.screens.ExerciseItem
import com.example.snail.ui.screens.MisRutinasScreen
import com.example.snail.ui.screens.NuevaRutinaScreen
import com.example.snail.ui.theme.SnailTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnailTheme {
                val context = LocalContext.current
                val storedRoutines = remember { RoutineStorage.load(context) }
                val storedExercises = remember { ExerciseStorage.load(context) }
                var currentScreen by rememberSaveable { mutableStateOf(AppScreen.MAIN) }
                var routineName by rememberSaveable { mutableStateOf("") }
                var exerciseDrafts by remember { mutableStateOf(storedExercises) }
                var routineExercises by remember { mutableStateOf(emptyList<RoutineExercise>()) }
                var nextRoutineExerciseId by rememberSaveable { mutableStateOf(0L) }
                var savedRoutines by remember { mutableStateOf(storedRoutines) }
                var nextRoutineId by rememberSaveable {
                    mutableStateOf((storedRoutines.maxOfOrNull { it.id } ?: -1L) + 1L)
                }

                fun clearExerciseSelections() {
                    val unselectedExercises = exerciseDrafts
                        .filter { it.name.isNotBlank() }
                        .map { it.copy(selected = false) }
                    exerciseDrafts = unselectedExercises
                    ExerciseStorage.save(context, unselectedExercises)
                }

                fun cancelNewRoutine() {
                    routineName = ""
                    routineExercises = emptyList()
                    currentScreen = AppScreen.MAIN
                }

                BackHandler(enabled = currentScreen != AppScreen.MAIN) {
                    when (currentScreen) {
                        AppScreen.NEW_EXERCISE -> {
                            clearExerciseSelections()
                            currentScreen = AppScreen.NEW_ROUTINE
                        }
                        AppScreen.NEW_ROUTINE -> cancelNewRoutine()
                        else -> currentScreen = AppScreen.MAIN
                    }
                }

                when (currentScreen) {
                    AppScreen.MAIN -> MainScreen(
                        onNewRoutine = { currentScreen = AppScreen.NEW_ROUTINE },
                        onNewTraining = { currentScreen = AppScreen.TRAINING }
                    )

                    AppScreen.NEW_ROUTINE -> NuevaRutinaScreen(
                        routineName = routineName,
                        onRoutineNameChange = { routineName = it },
                        exerciseItems = routineExercises,
                        onExerciseItemsChange = { routineExercises = it },
                        onBack = { cancelNewRoutine() },
                        onNewExercise = { currentScreen = AppScreen.NEW_EXERCISE },
                        onSave = {
                            val updatedRoutines = savedRoutines + SavedRoutine(
                                id = nextRoutineId++,
                                name = routineName.trim(),
                                exercises = routineExercises.map { exercise ->
                                    exercise.copy(
                                        series = exercise.series.trim(),
                                        repetitions = exercise.repetitions.trim(),
                                        rir = exercise.rir.trim()
                                    )
                                }
                            )
                            savedRoutines = updatedRoutines
                            RoutineStorage.save(context, updatedRoutines)
                            routineName = ""
                            routineExercises = emptyList()
                            currentScreen = AppScreen.MAIN
                        }
                    )

                    AppScreen.NEW_EXERCISE -> AñadirEjercicioScreen(
                        exerciseItems = exerciseDrafts,
                        onExerciseItemsChange = { updatedExercises ->
                            exerciseDrafts = updatedExercises
                            ExerciseStorage.save(context, updatedExercises)
                        },
                        onBack = {
                            clearExerciseSelections()
                            currentScreen = AppScreen.NEW_ROUTINE
                        },
                        onAdd = { selectedNames ->
                            val newExercises = selectedNames.map { name ->
                                RoutineExercise(
                                    id = nextRoutineExerciseId++,
                                    name = name
                                )
                            }
                            routineExercises = routineExercises + newExercises
                            clearExerciseSelections()
                            currentScreen = AppScreen.NEW_ROUTINE
                        }
                    )

                    AppScreen.TRAINING -> MisRutinasScreen(
                        routines = savedRoutines,
                        onBack = { currentScreen = AppScreen.MAIN },
                        onDeleteRoutine = { routineId ->
                            val updatedRoutines = savedRoutines.filterNot { it.id == routineId }
                            savedRoutines = updatedRoutines
                            RoutineStorage.save(context, updatedRoutines)
                        }
                    )
                }
            }
        }
    }
}

private enum class AppScreen {
    MAIN,
    NEW_ROUTINE,
    NEW_EXERCISE,
    TRAINING
}

@Composable
private fun MainScreen(
    onNewRoutine: () -> Unit,
    onNewTraining: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            OutlinedButton(
                onClick = onNewRoutine,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                border = BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text("+ Rutina")
            }

            Button(
                onClick = onNewTraining,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text("+ Entrenamiento")
            }
        }
    }
}
