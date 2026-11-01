package com.example.snail.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.snail.ui.components.*
import com.example.snail.ui.theme.SnailDarkGray
import com.example.snail.data.editExercise
import com.example.snail.ui.models.exerciseGroupLabel
import java.text.Collator
import java.util.Locale

data class ExerciseItem(
    val id: Long, val name: String = "", val selected: Boolean = false,
    val selectionOrder: Int? = null, val confirmed: Boolean = false,
    val note: String = "", val muscleGroup: String = "", val isCardio: Boolean = false
)

fun List<ExerciseItem>.sortedByExerciseName(): List<ExerciseItem> {
    val collator = Collator.getInstance(Locale.forLanguageTag("es")).apply { strength = Collator.PRIMARY }
    return sortedWith { a, b -> collator.compare(a.name, b.name) }
}

@Composable
fun AñadirEjercicioScreen(
    exerciseItems: List<ExerciseItem>, onExerciseItemsChange: (List<ExerciseItem>) -> Unit,
    onBack: () -> Unit, onAdd: (List<ExerciseItem>) -> Unit, nextId: () -> Long
) {
    val confirmation = rememberConfirmationState()
    var editor by remember { mutableStateOf<ExerciseItem?>(null) }
    var originalId by remember { mutableStateOf<Long?>(null) }
    fun toggle(exercise: ExerciseItem) {
        val selected = exerciseItems.filter { it.selected && it.id != exercise.id }.sortedBy { it.selectionOrder }.map { it.id } +
            if (exercise.selected) emptyList() else listOf(exercise.id)
        onExerciseItemsChange(exerciseItems.map {
            it.copy(selected = it.id in selected, selectionOrder = selected.indexOf(it.id).takeIf { order -> order >= 0 }?.plus(1))
        })
    }
    ConfirmationHost(confirmation) {
        Column(Modifier.fillMaxSize().background(Color.Black).imePadding()) {
            LazyColumn(Modifier.weight(1f).fillMaxWidth().statusBarsPadding().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val grouped = exerciseItems.sortedByExerciseName().groupBy {
                    exerciseGroupLabel(it.muscleGroup, it.isCardio)
                }.toSortedMap(compareBy<String> { it == "Sin grupo muscular" }.thenBy { it.lowercase() })
                grouped.forEach { (group, exercises) ->
                    item(key = "group-$group") { Text(group, color = Color.LightGray) }
                    items(exercises, key = { it.id }) { exercise ->
                        val tint = if (exercise.selected) Color.Black else Color.White
                        Row(Modifier.fillMaxWidth().background(if (exercise.selected) Color.White else SnailDarkGray, RoundedCornerShape(12.dp))
                            .clickable { toggle(exercise) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val remove = {
                                    val remaining = exerciseItems.filterNot { it.id == exercise.id }
                                    val selectedIds = remaining.filter { it.selected }.sortedBy { it.selectionOrder }.map { it.id }
                                    onExerciseItemsChange(remaining.map { it.copy(selectionOrder = selectedIds.indexOf(it.id).takeIf { index -> index >= 0 }?.plus(1)) })
                                }
                                if (!exercise.confirmed || exercise.name.isBlank()) remove() else confirmation.request(DeleteConfirmation, remove)
                            }) { TrashIcon(tint = tint, contentDescription = "Borrar ${exercise.name}") }
                            Column(Modifier.weight(1f)) {
                                Text(exercise.name, color = tint)
                                if (exercise.note.isNotBlank()) Text(exercise.note, color = tint, style = MaterialTheme.typography.bodySmall)
                            }
                            exercise.selectionOrder?.let { Text(it.toString(), color = tint, fontWeight = FontWeight.Bold) }
                            IconButton(onClick = { originalId = exercise.id; editor = exercise }) {
                                Text("✎", color = tint, modifier = Modifier.semanticsLabel("Editar ${exercise.name}"))
                            }
                        }
                    }
                }
                item {
                    OutlinedButton(onClick = { originalId = null; editor = ExerciseItem(nextId()) }, modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color.White), colors = outlinedActionColors()) { ActionText("Crear Ejercicio") }
                }
            }
            Row(Modifier.bottomActionsLayout(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, Color.White), colors = outlinedActionColors()) { ActionText("Cancelar") }
                Button(onClick = { onAdd(exerciseItems.filter { it.selected && it.confirmed }.sortedBy { it.selectionOrder }) },
                    enabled = editor == null && exerciseItems.any { it.selected && it.confirmed }, modifier = Modifier.weight(1f)) { ActionText("Añadir") }
            }
        }
    }
    editor?.let { draft ->
        ExerciseEditor(draft, onChange = { editor = it }, onDismiss = { editor = null }, onSave = {
            val order = draft.selectionOrder ?: ((exerciseItems.mapNotNull { it.selectionOrder }.maxOrNull() ?: 0) + 1)
            val replacement = draft.copy(id = if (originalId != null) nextId() else draft.id,
                name = draft.name.trim(), muscleGroup = draft.muscleGroup.trim(), note = draft.note.trim(),
                confirmed = true, selected = true, selectionOrder = order)
            onExerciseItemsChange(exerciseItems.editExercise(originalId, replacement).sortedByExerciseName())
            editor = null
        })
    }
}

@Composable
private fun ExerciseEditor(value: ExerciseItem, onChange: (ExerciseItem) -> Unit, onDismiss: () -> Unit, onSave: () -> Unit) {
    val focus = remember { FocusRequester() }
    Dialog(onDismissRequest = onDismiss) {
        val keyboard = LocalSoftwareKeyboardController.current
        val windowFocused = LocalWindowInfo.current.isWindowFocused
        Column(Modifier.fillMaxWidth().background(SnailDarkGray, RoundedCornerShape(12.dp)).padding(16.dp)
            .verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (value.confirmed) "Editar ejercicio" else "Crear ejercicio")
            OutlinedTextField(value.name, { name -> onChange(value.copy(name = name.replaceFirstChar { it.titlecase() })) },
                modifier = Modifier.fillMaxWidth().focusRequester(focus).semanticsLabel("Nombre del Ejercicio"), singleLine = true,
                placeholder = { Text("Nombre del Ejercicio") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next))
            OutlinedTextField(value.muscleGroup, { onChange(value.copy(muscleGroup = it)) }, modifier = Modifier.fillMaxWidth(),
                label = { Text("Grupo muscular (opcional)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(value.isCardio, { onChange(value.copy(isCardio = it)) }); Text("Cardio")
            }
            OutlinedTextField(value.note, { onChange(value.copy(note = it)) }, modifier = Modifier.fillMaxWidth(),
                label = { Text("Nota (opcional)") }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (value.name.isNotBlank()) onSave() }))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), colors = outlinedActionColors()) { ActionText("Cancelar") }
                Button(onClick = onSave, enabled = value.name.isNotBlank(), modifier = Modifier.weight(1f)) { ActionText("Guardar") }
                if (!value.confirmed) IconButton(onClick = onDismiss) { TrashIcon(contentDescription = "Descartar ejercicio en creación") }
            }
        }
        LaunchedEffect(Unit) {
            withFrameNanos { }
            focus.requestFocus()
        }
        // A dialog may receive window focus after its first composition.
        LaunchedEffect(windowFocused) {
            if (!windowFocused) return@LaunchedEffect
            withFrameNanos { }
            keyboard?.show()
        }
    }
}
