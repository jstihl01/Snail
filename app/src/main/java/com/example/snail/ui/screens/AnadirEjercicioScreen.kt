package com.example.snail.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.snail.ui.components.TrashIcon
import com.example.snail.ui.components.bottomActionsLayout
import com.example.snail.ui.theme.SnailDarkGray
import com.example.snail.ui.theme.SnailLightGray
import com.example.snail.ui.theme.SnailMediumGray

data class ExerciseItem(
    val id: Long,
    val name: String = "",
    val selected: Boolean = false
)

@Composable
fun AñadirEjercicioScreen(
    exerciseItems: List<ExerciseItem>,
    onExerciseItemsChange: (List<ExerciseItem>) -> Unit,
    onBack: () -> Unit,
    onAdd: (List<String>) -> Unit
) {
    val canCreateExercise = exerciseItems.none { it.name.isBlank() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
        ) {
            items(
                items = exerciseItems,
                key = { it.id }
            ) { exercise ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            onExerciseItemsChange(
                                exerciseItems.filterNot { it.id == exercise.id }
                            )
                        }
                    ) {
                        TrashIcon()
                    }

                    OutlinedTextField(
                        value = exercise.name,
                        onValueChange = { newName ->
                            onExerciseItemsChange(exerciseItems.map {
                                if (it.id == exercise.id) {
                                    it.copy(
                                        name = newName,
                                        selected = it.selected && newName.isNotBlank()
                                    )
                                } else {
                                    it
                                }
                            })
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                text = "Nombre del Ejercicio",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        },
                        textStyle = TextStyle(textAlign = TextAlign.Start),
                        singleLine = true
                    )

                    Checkbox(
                        checked = exercise.selected,
                        enabled = exercise.name.isNotBlank(),
                        onCheckedChange = { selected ->
                            onExerciseItemsChange(exerciseItems.map {
                                if (it.id == exercise.id) it.copy(selected = selected) else it
                            })
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            uncheckedColor = Color.White,
                            checkmarkColor = Color.Black
                        )
                    )
                }
            }

            item(key = "create_exercise") {
                OutlinedButton(
                    onClick = {
                        val nextId = (exerciseItems.maxOfOrNull { it.id } ?: -1L) + 1L
                        onExerciseItemsChange(exerciseItems + ExerciseItem(id = nextId))
                    },
                    enabled = canCreateExercise,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    border = BorderStroke(
                        1.dp,
                        if (canCreateExercise) Color.White else SnailMediumGray
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White,
                        disabledContainerColor = Color.Black,
                        disabledContentColor = SnailLightGray
                    )
                ) {
                    Text("Crear Ejercicio")
                }
            }
        }

        Column(
            modifier = Modifier.bottomActionsLayout()
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                    border = BorderStroke(1.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        onAdd(
                            exerciseItems
                                .filter { it.selected && it.name.isNotBlank() }
                                .map { it.name.trim() }
                        )
                    },
                    enabled = exerciseItems.any { it.selected && it.name.isNotBlank() },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        disabledContainerColor = SnailDarkGray,
                        disabledContentColor = SnailLightGray
                    )
                ) {
                    Text("Añadir")
                }
            }
        }
    }
}
