package com.example.snail.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.snail.ui.components.*
import com.example.snail.ui.models.*
import com.example.snail.ui.theme.*

@Composable
fun MisRutinasScreen(routines: List<SavedRoutine>, folders: List<RoutineFolder>, onBack: () -> Unit,
    onDeleteRoutine: (Long) -> Unit, onRoutineColorChange: (Long, Int) -> Unit, onStart: (SavedRoutine) -> Unit,
    onEdit: (SavedRoutine) -> Unit, onMove: (Long, Int) -> Unit, onAssign: (Long, Long?) -> Unit, onCreateFolder: (String) -> Unit) {
    val confirmation = rememberConfirmationState()
    var selected by remember { mutableStateOf<Long?>(null) }
    var collapsed by remember { mutableStateOf(emptySet<Long>()) }
    var menu by remember { mutableStateOf<Long?>(null) }
    var colors by remember { mutableStateOf<Long?>(null) }
    var moving by remember { mutableStateOf<SavedRoutine?>(null) }
    var creatingFolder by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    ConfirmationHost(confirmation) {
        Column(Modifier.fillMaxSize().background(Color.Black)) {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = { name = ""; creatingFolder = true }, modifier = Modifier.semanticsLabel("Crear carpeta")) { FolderPlusIcon() }
            }
            LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val groups = listOf(RoutineFolder(-1, "Sin carpeta")) + folders
                groups.forEach { folder ->
                    val folderId = folder.id.takeIf { it >= 0 }
                    val children = routines.filter { it.folderId == folderId }
                    if (folderId != null || children.isNotEmpty()) {
                        item(key = "folder-${folder.id}") {
                            Row(Modifier.fillMaxWidth().clickable {
                                collapsed = if (folder.id in collapsed) collapsed - folder.id else collapsed + folder.id
                            }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(if (folder.id in collapsed) "▸" else "▾", Modifier.padding(end = 8.dp))
                                Text("${folder.name} (${children.size})")
                            }
                        }
                        if (folder.id !in collapsed) items(children, key = { it.id }) { routine ->
                            val active = selected == routine.id
                            val tint = if (active) Color.Black else Color.White
                            val background = if (active) Color.White else routineColorFor(routine.colorIndex)
                            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(background)
                                .clickable { selected = if (active) null else routine.id }.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { confirmation.request(DeleteConfirmation) {
                                        if (selected == routine.id) selected = null
                                        onDeleteRoutine(routine.id)
                                    } }) { TrashIcon(tint = tint) }
                                    Text(routine.name, Modifier.weight(1f), color = tint)
                                    Box {
                                        IconButton(onClick = { menu = routine.id }, modifier = Modifier.semanticsLabel("Opciones de ${routine.name}")) { Text("⋮", color = tint) }
                                        DropdownMenu(menu == routine.id, onDismissRequest = { menu = null }) {
                                            DropdownMenuItem(text = { ActionText("Editar") }, onClick = { menu = null; onEdit(routine) })
                                            DropdownMenuItem(text = { ActionText("Cambiar color") }, onClick = { menu = null; colors = routine.id })
                                            DropdownMenuItem(text = { ActionText("Mover arriba") }, enabled = children.firstOrNull()?.id != routine.id,
                                                onClick = { menu = null; onMove(routine.id, -1) })
                                            DropdownMenuItem(text = { ActionText("Mover abajo") }, enabled = children.lastOrNull()?.id != routine.id,
                                                onClick = { menu = null; onMove(routine.id, 1) })
                                            DropdownMenuItem(text = { ActionText("Mover a carpeta") }, onClick = { menu = null; moving = routine })
                                        }
                                        DropdownMenu(colors == routine.id, onDismissRequest = { colors = null },
                                            modifier = Modifier.width(64.dp).border(1.dp, Color.White, RoundedCornerShape(32.dp)),
                                            shape = RoundedCornerShape(32.dp), containerColor = routineColorFor(routine.colorIndex)) {
                                            RoutineColors.forEachIndexed { index, color ->
                                                DropdownMenuItem(text = {
                                                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                                        Box(Modifier.size(30.dp).background(color, CircleShape)
                                                            .border(if (index == routine.colorIndex) 3.dp else 1.5.dp, Color.White, CircleShape))
                                                    }
                                                }, onClick = { colors = null; onRoutineColorChange(routine.id, index) },
                                                    modifier = Modifier.semanticsLabel("Color $index"), contentPadding = PaddingValues(0.dp))
                                            }
                                        }
                                    }
                                }
                                routine.exercises.sectionsByExerciseType().forEach { exercises ->
                                    val cardio = exercises.first().isCardio
                                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).border(1.dp, tint, RoundedCornerShape(8.dp))) {
                                        Row(Modifier.height(IntrinsicSize.Min)) {
                                            listOf("Ejercicio", "Series", if (cardio) "Intensidad" else "Reps.", if (cardio) "Minutos" else "RIR").forEach {
                                                TableCell(it, Modifier.weight(1f), tint)
                                            }
                                        }
                                        exercises.forEach { exercise ->
                                            Row(Modifier.height(IntrinsicSize.Min)) {
                                                TableCell(exercise.name, Modifier.weight(1f), tint, TextAlign.Start)
                                                TableCell(exercise.series, Modifier.weight(1f), tint)
                                                TableCell(if (cardio) exercise.intensity else exercise.repetitions, Modifier.weight(1f), tint)
                                                TableCell(if (cardio) exercise.minutes else exercise.rir, Modifier.weight(1f), tint)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Row(Modifier.bottomActionsLayout(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onBack, Modifier.weight(1f), border = BorderStroke(1.dp, Color.White), colors = outlinedActionColors()) { ActionText("Cancelar") }
                Button(onClick = { routines.firstOrNull { it.id == selected }?.let(onStart) }, enabled = routines.any { it.id == selected },
                    modifier = Modifier.weight(1f)) { ActionText("Empezar") }
            }
        }
    }
    if (creatingFolder) AlertDialog(onDismissRequest = { creatingFolder = false }, title = { Text("Nueva carpeta") },
        text = { OutlinedTextField(name, { name = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nombre") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onCreateFolder(name.trim()); creatingFolder = false }, enabled = name.isNotBlank()) { ActionText("Crear") } },
        dismissButton = { TextButton(onClick = { creatingFolder = false }) { ActionText("Cancelar") } })
    moving?.let { routine ->
        AlertDialog(onDismissRequest = { moving = null }, title = { Text("Mover a carpeta") },
            text = { Column(Modifier.verticalScroll(rememberScrollState())) {
                (listOf(RoutineFolder(-1, "Sin carpeta")) + folders).forEach { folder ->
                    TextButton(onClick = { onAssign(routine.id, folder.id.takeIf { it >= 0 }); collapsed = collapsed - folder.id; moving = null },
                        modifier = Modifier.fillMaxWidth()) { ActionText(folder.name) }
                }
            } }, confirmButton = { TextButton(onClick = { moving = null }) { ActionText("Cancelar") } })
    }
}

@Composable
private fun TableCell(text: String, modifier: Modifier, color: Color, alignment: TextAlign = TextAlign.Center) {
    Box(modifier.fillMaxHeight().border(0.5.dp, color).padding(horizontal = 6.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
        Text(text, Modifier.fillMaxWidth(), color = color, textAlign = alignment)
    }
}
