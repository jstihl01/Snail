package com.example.snail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.snail.data.*
import com.example.snail.ui.models.*
import com.example.snail.ui.screens.*
import com.example.snail.ui.theme.SnailTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SnailTheme { SnailApp() } }
    }
}

private enum class AppScreen { MAIN, ROUTINE, EXERCISES, ROUTINES, TRAINING, PROGRESS }

@Composable
private fun SnailApp() {
    val context = LocalContext.current
    val loaded = remember { runCatching { AppDataStorage.load(context) } }
    val initial = loaded.getOrNull()
    if (initial == null) { Text("No se pudieron abrir los datos. Se han conservado: ${loaded.exceptionOrNull()?.message}"); return }
    var data by remember { mutableStateOf(initial) }
    var error by remember { mutableStateOf<String?>(null) }
    var current by remember { mutableStateOf(AppScreen.MAIN) }
    var nextId by remember { mutableLongStateOf(data.nextId()) }
    var routineName by remember { mutableStateOf("") }
    var routineExercises by remember { mutableStateOf(emptyList<RoutineExercise>()) }
    var editingRoutine by remember { mutableStateOf<SavedRoutine?>(null) }
    var trainingRoutine by remember { mutableStateOf<SavedRoutine?>(null) }
    var editingWorkout by remember { mutableStateOf<SavedWorkout?>(null) }
    var transfer by remember { mutableStateOf(false) }

    fun save(updated: AppData): Boolean = try {
        AppDataStorage.save(context, updated)
        data = updated
        nextId = maxOf(nextId, updated.nextId())
        true
    } catch (exception: Exception) { error = exception.message ?: "Error al guardar"; false }
    fun cancelRoutine() {
        routineName = ""; routineExercises = emptyList()
        current = if (editingRoutine == null) AppScreen.MAIN else AppScreen.ROUTINES
        editingRoutine = null
    }
    fun closeExercises() {
        save(data.copy(exercises = data.exercises.filter { it.name.isNotBlank() }.map { it.copy(selected = false, selectionOrder = null) }))
        current = AppScreen.ROUTINE
    }
    fun changeExercises(items: List<ExerciseItem>) {
        fun catalogue(values: List<ExerciseItem>) = values.map { it.copy(selected = false, selectionOrder = null) }
        val updated = data.copy(exercises = items)
        // Selection is transient; avoid rewriting all history on every catalogue tap.
        if (catalogue(items) == catalogue(data.exercises)) data = updated else save(updated)
    }
    BackHandler(enabled = current == AppScreen.EXERCISES || current == AppScreen.ROUTINES || current == AppScreen.PROGRESS) {
        if (current == AppScreen.EXERCISES) closeExercises() else current = AppScreen.MAIN
    }
    when (current) {
        AppScreen.MAIN -> MainScreen(data.workouts, data.routines.isNotEmpty(),
            onNewRoutine = { editingRoutine = null; routineName = ""; routineExercises = emptyList(); current = AppScreen.ROUTINE },
            onNewTraining = { current = AppScreen.ROUTINES }, onDeleteWorkout = { id -> save(data.copy(workouts = data.workouts.filterNot { it.id == id })) },
            onEditWorkout = { workout ->
                editingWorkout = workout
                trainingRoutine = SavedRoutine(workout.routineId ?: -1, workout.routineName, emptyList(), workout.colorIndex)
                current = AppScreen.TRAINING
            }, onTransfer = { transfer = true }, onProgress = { current = AppScreen.PROGRESS })
        AppScreen.ROUTINE -> NuevaRutinaScreen(routineName, { routineName = it }, routineExercises, { routineExercises = it },
            onBack = ::cancelRoutine, onNewExercise = { current = AppScreen.EXERCISES }, onSave = {
                val previous = editingRoutine
                val updated = if (previous == null)
                    data.copy(routines = data.routines + SavedRoutine(nextId++, routineName.trim(), routineExercises))
                else data.editRoutine(previous.id, nextId++, routineName, routineExercises)
                if (save(updated)) cancelRoutine()
            })
        AppScreen.EXERCISES -> AñadirEjercicioScreen(data.exercises, ::changeExercises,
            onBack = ::closeExercises, nextId = { nextId++ }, onAdd = { selected ->
                var id = (routineExercises.maxOfOrNull { it.id } ?: -1) + 1
                routineExercises = routineExercises + selected.map {
                    RoutineExercise(id++, it.name, note = it.note, muscleGroup = it.muscleGroup, isCardio = it.isCardio, sourceExerciseId = it.id)
                }
                closeExercises()
            })
        AppScreen.ROUTINES -> MisRutinasScreen(data.routines, data.folders, onBack = { current = AppScreen.MAIN },
            onDeleteRoutine = { id -> save(data.copy(routines = data.routines.filterNot { it.id == id })) },
            onStart = { trainingRoutine = it; editingWorkout = null; current = AppScreen.TRAINING },
            onEdit = { editingRoutine = it; routineName = it.name; routineExercises = it.exercises; current = AppScreen.ROUTINE },
            onCreateFolder = { save(data.copy(folders = data.folders + RoutineFolder(nextId++, it))) },
            onAssign = { id, folder -> save(data.copy(routines = data.routines.map { if (it.id == id) it.copy(folderId = folder) else it })) },
            onMove = { id, direction -> save(data.moveRoutine(id, direction)) },
            onRoutineColorChange = { id, color -> save(data.changeRoutineColor(id, color)) })
        AppScreen.TRAINING -> trainingRoutine?.let { routine ->
            NuevoEntrenamientoScreen(routine, data.workouts, onBack = {
                current = if (editingWorkout == null) AppScreen.ROUTINES else AppScreen.MAIN
                trainingRoutine = null; editingWorkout = null
            }, editing = editingWorkout, onSave = { exercises, start, end ->
                val prior = editingWorkout
                val workout = prior?.copy(exercises = exercises, startMotivation = start, endMotivation = end)
                    ?: SavedWorkout(nextId++, routine.name, System.currentTimeMillis(), exercises, routine.colorIndex, routine.id, start, end)
                val updated = if (prior == null) data.copy(workouts = data.workouts + workout) else data.editWorkout(workout)
                if (save(updated)) { editingWorkout = null; trainingRoutine = null; current = AppScreen.MAIN }
            })
        }
        AppScreen.PROGRESS -> ProgresoScreen(data.workouts) { current = AppScreen.MAIN }
    }
    if (transfer) DataTransferDialog(data, onImport = { save(it) }, onDismiss = { transfer = false })
    error?.let { message -> androidx.compose.material3.AlertDialog(onDismissRequest = { error = null },
        text = { Text(message) }, confirmButton = { androidx.compose.material3.TextButton(onClick = { error = null }) { Text("Cerrar", maxLines = 1) } }) }
}
