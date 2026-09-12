package com.example.snail

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.snail.data.ExerciseStorage
import com.example.snail.data.RoutineStorage
import com.example.snail.data.WorkoutStorage
import com.example.snail.ui.models.RoutineExercise
import com.example.snail.ui.models.SavedRoutine
import com.example.snail.ui.models.SavedWorkout
import com.example.snail.ui.screens.AñadirEjercicioScreen
import com.example.snail.ui.screens.ExerciseItem
import com.example.snail.ui.screens.MisRutinasScreen
import com.example.snail.ui.screens.MainScreen
import com.example.snail.ui.screens.NuevoEntrenamientoScreen
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
                val storedWorkouts = remember { WorkoutStorage.load(context) }
                var currentScreen by rememberSaveable { mutableStateOf(AppScreen.MAIN) }
                var routineName by rememberSaveable { mutableStateOf("") }
                var exerciseDrafts by remember { mutableStateOf(storedExercises) }
                var routineExercises by remember { mutableStateOf(emptyList<RoutineExercise>()) }
                var nextRoutineExerciseId by rememberSaveable { mutableStateOf(0L) }
                var savedRoutines by remember { mutableStateOf(storedRoutines) }
                var activeTrainingRoutine by remember { mutableStateOf<SavedRoutine?>(null) }
                var savedWorkouts by remember { mutableStateOf(storedWorkouts) }
                var nextWorkoutId by rememberSaveable {
                    mutableStateOf((storedWorkouts.maxOfOrNull { it.id } ?: -1L) + 1L)
                }
                var nextRoutineId by rememberSaveable {
                    mutableStateOf((storedRoutines.maxOfOrNull { it.id } ?: -1L) + 1L)
                }

                fun clearExerciseSelections() {
                    val unselectedExercises = exerciseDrafts
                        .filter { it.name.isNotBlank() }
                        .map { it.copy(selected = false, confirmed = true) }
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
                        AppScreen.NEW_TRAINING -> {
                            activeTrainingRoutine = null
                            currentScreen = AppScreen.TRAINING
                        }
                        else -> currentScreen = AppScreen.MAIN
                    }
                }

                when (currentScreen) {
                    AppScreen.MAIN -> MainScreen(
                        workouts = savedWorkouts,
                        onNewRoutine = { currentScreen = AppScreen.NEW_ROUTINE },
                        onNewTraining = { currentScreen = AppScreen.TRAINING },
                        onDeleteWorkout = { workoutId ->
                            val updatedWorkouts = savedWorkouts.filterNot { it.id == workoutId }
                            savedWorkouts = updatedWorkouts
                            WorkoutStorage.save(context, updatedWorkouts)
                        }
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
                        },
                        onStart = { routine ->
                            activeTrainingRoutine = routine
                            currentScreen = AppScreen.NEW_TRAINING
                        },
                        onRoutineColorChange = { routineId, colorIndex ->
                            val routine = savedRoutines.first { it.id == routineId }
                            val updatedRoutines = savedRoutines.map {
                                if (it.id == routineId) it.copy(colorIndex = colorIndex) else it
                            }
                            savedRoutines = updatedRoutines
                            RoutineStorage.save(context, updatedRoutines)
                            val updatedWorkouts = savedWorkouts.map { workout ->
                                if (workout.routineId == routineId ||
                                    (workout.routineId == null &&
                                        workout.routineName == routine.name)) {
                                    workout.copy(colorIndex = colorIndex, routineId = routineId)
                                } else workout
                            }
                            savedWorkouts = updatedWorkouts
                            WorkoutStorage.save(context, updatedWorkouts)
                        }
                    )

                    AppScreen.NEW_TRAINING -> activeTrainingRoutine?.let { routine ->
                        NuevoEntrenamientoScreen(
                            routine = routine,
                            onBack = {
                                activeTrainingRoutine = null
                                currentScreen = AppScreen.TRAINING
                            },
                            onSave = { completedExercises ->
                                val updatedWorkouts = savedWorkouts + SavedWorkout(
                                    id = nextWorkoutId++,
                                    routineName = routine.name,
                                    completedAt = System.currentTimeMillis(),
                                    colorIndex = routine.colorIndex,
                                    routineId = routine.id,
                                    exercises = completedExercises
                                )
                                savedWorkouts = updatedWorkouts
                                WorkoutStorage.save(context, updatedWorkouts)
                                activeTrainingRoutine = null
                                currentScreen = AppScreen.MAIN
                            }
                        )
                    }
                }
            }
        }
    }
}

private enum class AppScreen {
    MAIN,
    NEW_ROUTINE,
    NEW_EXERCISE,
    TRAINING,
    NEW_TRAINING
}
