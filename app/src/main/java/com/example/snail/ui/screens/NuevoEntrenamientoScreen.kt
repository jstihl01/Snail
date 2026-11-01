package com.example.snail.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snail.ui.components.*
import com.example.snail.ui.models.*
import com.example.snail.ui.theme.routineColorFor

@Composable
fun NuevoEntrenamientoScreen(routine: SavedRoutine, workouts: List<SavedWorkout>, onBack: () -> Unit,
    onSave: (List<CompletedExercise>, String, String) -> Unit, editing: SavedWorkout? = null) {
    val history = remember(workouts, editing?.id) { workouts.filterNot { it.id == editing?.id } }
    val specs = remember(routine, editing) {
        editing?.exercises ?: routine.exercises.map { exercise ->
            CompletedExercise(exercise.name, List(exercise.series.toIntOrNull()?.coerceIn(1, 500) ?: 1) {
                CompletedSet(it + 1, "", "", exercise.rir)
            }, exercise.note, exercise.muscleGroup, exercise.isCardio, exercise.repetitions, exercise.rir, exercise.sourceExerciseId)
        }
    }
    var sets by remember(specs) { mutableStateOf(specs.map { it.sets }) }
    var startMotivation by remember { mutableStateOf(editing?.startMotivation) }
    var endMotivation by remember { mutableStateOf(editing?.endMotivation) }
    val confirmation = rememberConfirmationState()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val list = rememberLazyListState()
    var pending by remember { mutableStateOf<TrainingField?>(null) }
    val fields = trainingFields(sets)
    val background = routineColorFor(routine.colorIndex)
    val requestExit: () -> Unit = {
        if (sets != specs.map { it.sets } || startMotivation != editing?.startMotivation || endMotivation != editing?.endMotivation)
            confirmation.request(ExitConfirmation, onBack) else onBack()
    }
    BackHandler { requestExit() }
    LaunchedEffect(pending) {
        pending?.let { target ->
            // Motivation + (header, rows, footer) for every preceding exercise.
            val index = 2 + (0 until target.exercise).sumOf { sets[it].size + 2 } + target.set
            if (list.layoutInfo.visibleItemsInfo.none { it.index == index }) list.scrollToItem(index)
        }
    }
    fun next(field: TrainingField) {
        val target = fields.getOrNull(fields.indexOf(field) + 1)
        if (target == null) { focusManager.clearFocus(); keyboard?.hide() } else pending = target
    }
    fun change(exercise: Int, row: Int, value: CompletedSet) {
        sets = sets.mapIndexed { e, rows -> if (e == exercise) rows.mapIndexed { s, old -> if (s == row) value else old } else rows }
    }
    val canSave = startMotivation != null && endMotivation != null && sets.indices.any { e -> sets[e].any { it.isComplete(specs[e].isCardio) } }
    ConfirmationHost(confirmation) {
        Column(Modifier.fillMaxSize().background(Color.Black).imePadding()) {
            LazyColumn(state = list, modifier = Modifier.weight(1f).fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp)) {
                item(key = "motivation-start") { MotivationSelector("Motivación inicial", startMotivation, background) { startMotivation = it } }
                specs.forEachIndexed { e, exercise ->
                    item(key = "header-$e") {
                        Column(Modifier.padding(top = 12.dp).background(background, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)).padding(12.dp)) {
                            Text(exercise.name)
                            if (exercise.note.isNotBlank()) Text(exercise.note, style = MaterialTheme.typography.bodySmall)
                            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Serie", Modifier.weight(0.65f), textAlign = TextAlign.Center)
                                Text(if (exercise.isCardio) "Intensidad" else "KG", Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(if (exercise.isCardio) "Minutos" else "Reps.", Modifier.weight(1f), textAlign = TextAlign.Center)
                                if (!exercise.isCardio && (exercise.targetRir.isNotBlank() || sets[e].any { it.rir.isNotBlank() }))
                                    Text("RIR", Modifier.weight(0.65f), textAlign = TextAlign.Center)
                            }
                        }
                    }
                    sets[e].forEachIndexed { s, set ->
                        item(key = "set-$e-${set.number}") {
                            Row(Modifier.fillMaxWidth().background(background).padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("${set.number}${if (set.isExtra) " +" else ""}", Modifier.weight(0.65f)
                                    .semanticsLabel(if (set.isExtra) "Serie ${set.number}, extra" else "Serie ${set.number}"), textAlign = TextAlign.Center)
                                val kgReference = remember(exercise, history) {
                                    RoutineExercise(e.toLong(), exercise.name, minRepetitions = splitRepetitionRange(exercise.targetRepetitions).first)
                                        .kilogramsReference(history)
                                }
                                for (column in 0..1) {
                                    val field = TrainingField(e, s, column)
                                    val value = if (exercise.isCardio) { if (column == 0) set.intensity else set.minutes }
                                        else { if (column == 0) set.kilograms else set.repetitions }
                                    val hint = if (exercise.isCardio) {
                                        routine.exercises.getOrNull(e)?.let { if (column == 0) it.intensity else it.minutes }.orEmpty()
                                    } else if (column == 0) kgReference else exercise.targetRepetitions
                                    NumberField(value, { input -> change(e, s, if (exercise.isCardio) {
                                        if (column == 0) set.copy(intensity = input) else set.copy(minutes = input)
                                    } else { if (column == 0) set.copy(kilograms = input) else set.copy(repetitions = input) }) }, hint,
                                        Modifier.weight(1f), focus = pending == field, onFocused = { if (pending == field) pending = null },
                                        onNext = { next(field) }, last = field == fields.lastOrNull(), boldHint = !exercise.isCardio && column == 0,
                                        label = "${exercise.name}, serie ${set.number}, ${if (exercise.isCardio) { if (column == 0) "Intensidad" else "Minutos" } else { if (column == 0) "KG" else "Reps." }}")
                                }
                                if (!exercise.isCardio && (exercise.targetRir.isNotBlank() || sets[e].any { it.rir.isNotBlank() }))
                                    Text(set.rir, Modifier.weight(0.65f), textAlign = TextAlign.Center)
                            }
                        }
                    }
                    item(key = "footer-$e") {
                        Row(Modifier.fillMaxWidth().background(background, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)).padding(8.dp),
                            horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = { sets = sets.mapIndexed { index, rows -> if (index == e) rows + newExtraSet(rows, exercise.targetRir) else rows } },
                                modifier = Modifier.semanticsLabel("Añadir serie a ${exercise.name}")) {
                                Box(Modifier.size(32.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                                    Text("+", color = background, fontSize = 24.sp)
                                }
                            }
                        }
                    }
                }
                item(key = "motivation-end") {
                    Box(Modifier.padding(top = 12.dp)) { MotivationSelector("Motivación final", endMotivation, background) { endMotivation = it } }
                }
            }
            Row(Modifier.bottomActionsLayout(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = requestExit, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, Color.White), colors = outlinedActionColors()) { ActionText("Cancelar") }
                Button(onClick = {
                    onSave(specs.mapIndexedNotNull { e, exercise ->
                        val complete = sets[e].filter { it.isComplete(exercise.isCardio) }
                        if (complete.isEmpty()) null else exercise.copy(sets = complete)
                    }, requireNotNull(startMotivation), requireNotNull(endMotivation))
                }, enabled = canSave, modifier = Modifier.weight(1f)) { ActionText("Guardar") }
            }
        }
    }
}

@Composable
private fun MotivationSelector(title: String, selected: String?, background: Color, onSelected: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().background(background, RoundedCornerShape(12.dp)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title)
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).border(1.dp, Color.White, RoundedCornerShape(8.dp))) {
            WorkoutMotivations.forEach { emoji ->
                Box(Modifier.weight(1f).background(if (emoji == selected) Color.White else background)
                    .clickable { onSelected(emoji) }.semanticsLabel("$title, $emoji")
                    .border(0.5.dp, Color.White).padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                    Text(emoji, fontSize = 24.sp)
                }
            }
        }
    }
}
