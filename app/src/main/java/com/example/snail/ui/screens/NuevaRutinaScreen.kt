package com.example.snail.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.snail.ui.components.*
import com.example.snail.ui.models.*
import com.example.snail.ui.theme.SnailDarkGray

@Composable
fun NuevaRutinaScreen(routineName: String, onRoutineNameChange: (String) -> Unit,
    exerciseItems: List<RoutineExercise>, onExerciseItemsChange: (List<RoutineExercise>) -> Unit,
    onBack: () -> Unit, onNewExercise: () -> Unit, onSave: () -> Unit) {
    val confirmation = rememberConfirmationState()
    val requestExit: () -> Unit = {
        if (routineName.isNotEmpty() || exerciseItems.isNotEmpty()) confirmation.request(ExitConfirmation, onBack) else onBack()
    }
    BackHandler { requestExit() }
    val valid = routineName.isNotBlank() && exerciseItems.isNotEmpty() && exerciseItems.all { exercise ->
        val minimum = exercise.minRepetitions.replace(',', '.').toBigDecimalOrNull()
        val maximum = exercise.maxRepetitions.replace(',', '.').toBigDecimalOrNull()
        (exercise.series.toIntOrNull() ?: 0) in 1..500 && if (exercise.isCardio)
            exercise.intensity.validNumber() && exercise.minutes.validNumber()
        else exercise.minRepetitions.validNumber() &&
            (exercise.maxRepetitions.isBlank() || (maximum != null && minimum != null && maximum >= minimum)) &&
            exercise.rir.validRirTarget()
    }
    val list = rememberLazyListState()
    val manager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var pending by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val order = exerciseItems.flatMapIndexed { index, exercise -> (0 until if (exercise.isCardio) 3 else 4).map { index to it } }
    LaunchedEffect(pending) { pending?.let { if (list.layoutInfo.visibleItemsInfo.none { info -> info.index == it.first }) list.scrollToItem(it.first) } }
    fun next(current: Pair<Int, Int>) {
        val target = order.getOrNull(order.indexOf(current) + 1)
        if (target == null) { manager.clearFocus(); keyboard?.hide() } else pending = target
    }
    ConfirmationHost(confirmation) {
        Column(Modifier.fillMaxSize().background(Color.Black).imePadding()) {
            OutlinedTextField(routineName, onRoutineNameChange, Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
                placeholder = { Text("Nombre de la Rutina", Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                textStyle = TextStyle(textAlign = TextAlign.Center), singleLine = true)
            LazyColumn(state = list, modifier = Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsIndexed(exerciseItems, key = { _, exercise -> exercise.id }) { index, exercise ->
                    Column(Modifier.fillMaxWidth().background(SnailDarkGray, RoundedCornerShape(12.dp)).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onExerciseItemsChange(exerciseItems.filterNot { it.id == exercise.id }) }) { TrashIcon() }
                            Text(exercise.name, Modifier.weight(1f))
                        }
                        if (exercise.note.isNotBlank()) Text(exercise.note, style = MaterialTheme.typography.bodySmall)
                        val values = if (exercise.isCardio) listOf(exercise.series, exercise.intensity, exercise.minutes)
                            else listOf(exercise.series, exercise.minRepetitions, exercise.maxRepetitions, exercise.rir)
                        val hints = if (exercise.isCardio) listOf("Series*", "Intensidad*", "Minutos*")
                            else listOf("Series*", "Mín. Reps.*", "Máx. Reps.", "RIR")
                        values.indices.chunked(2).forEach { columns ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                columns.forEach { field ->
                                    val key = index to field
                                    NumberField(values[field], { value ->
                                        val updated = when (field) {
                                            0 -> exercise.copy(series = value)
                                            1 -> if (exercise.isCardio) exercise.copy(intensity = value) else exercise.copy(minRepetitions = value)
                                            2 -> if (exercise.isCardio) exercise.copy(minutes = value) else exercise.copy(maxRepetitions = value)
                                            else -> exercise.copy(rir = value)
                                        }
                                        onExerciseItemsChange(exerciseItems.map { if (it.id == exercise.id) updated else it })
                                    }, hints[field], Modifier.weight(1f), focus = pending == key,
                                        onFocused = { if (pending == key) pending = null }, onNext = { next(key) }, last = key == order.lastOrNull(), integer = field == 0,
                                        allowRange = !exercise.isCardio && field == 3, label = "${exercise.name}, ${hints[field]}")
                                }
                                if (columns.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
                item { OutlinedButton(onNewExercise, Modifier.fillMaxWidth(), border = BorderStroke(1.dp, Color.White), colors = outlinedActionColors()) { ActionText("Añadir Ejercicio") } }
            }
            Row(Modifier.bottomActionsLayout(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(requestExit, Modifier.weight(1f), border = BorderStroke(1.dp, Color.White), colors = outlinedActionColors()) { ActionText("Cancelar") }
                Button(onSave, Modifier.weight(1f), enabled = valid) { ActionText("Guardar") }
            }
        }
    }
}
