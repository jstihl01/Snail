package com.example.snail.ui.screens
import com.example.snail.ui.components.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snail.ui.components.TrashIcon
import com.example.snail.ui.components.bottomActionsLayout
import com.example.snail.ui.theme.SnailDarkGray
import com.example.snail.ui.theme.SnailLightGray
import com.example.snail.ui.theme.SnailMediumGray
import java.text.Collator
import java.util.Locale

data class ExerciseItem(
    val id: Long,
    val name: String = "",
    val selected: Boolean = false,
    val selectionOrder: Int? = null,
    val confirmed: Boolean = false
)

fun List<ExerciseItem>.sortedByExerciseName(): List<ExerciseItem> {
    val collator = Collator.getInstance(Locale.forLanguageTag("es"))
    collator.strength = Collator.PRIMARY
    return sortedWith { first, second -> collator.compare(first.name, second.name) }
}

private fun List<ExerciseItem>.normalizeSelectionOrder(): List<ExerciseItem> {
    val orderedIds = filter { it.selected }
        .sortedBy { it.selectionOrder ?: Int.MAX_VALUE }
        .mapIndexed { index, exercise -> exercise.id to index + 1 }
        .toMap()
    return map { exercise ->
        exercise.copy(selectionOrder = orderedIds[exercise.id])
    }
}

@Composable
fun AñadirEjercicioScreen(
    exerciseItems: List<ExerciseItem>,
    onExerciseItemsChange: (List<ExerciseItem>) -> Unit,
    onBack: () -> Unit,
    onAdd: (List<String>) -> Unit
) {
    val confirmation = rememberConfirmationState()
    val canCreateExercise = exerciseItems.none { it.name.isBlank() }
    val focusManager = LocalFocusManager.current

    ConfirmationHost(confirmation) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .imePadding()
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
                var hadFocus by remember(exercise.id) { mutableStateOf(false) }
                val foregroundColor = if (exercise.selected) Color.Black else Color.White
                fun confirmName() {
                    if (exercise.name.isNotBlank()) {
                        val nextSelectionOrder =
                            (exerciseItems.mapNotNull { it.selectionOrder }.maxOrNull() ?: 0) + 1
                        onExerciseItemsChange(exerciseItems.map {
                            if (it.id == exercise.id) {
                                it.copy(
                                    name = it.name.trim(),
                                    confirmed = true,
                                    selected = true,
                                    selectionOrder = it.selectionOrder ?: nextSelectionOrder
                                )
                            } else it
                        }.sortedByExerciseName())
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = exercise.confirmed && exercise.name.isNotBlank()) {
                            if (exercise.selected) {
                                onExerciseItemsChange(
                                    exerciseItems.map {
                                        if (it.id == exercise.id) {
                                            it.copy(selected = false, selectionOrder = null)
                                        } else it
                                    }.normalizeSelectionOrder()
                                )
                            } else {
                                val nextSelectionOrder =
                                    (exerciseItems.mapNotNull { it.selectionOrder }.maxOrNull() ?: 0) + 1
                                onExerciseItemsChange(exerciseItems.map {
                                    if (it.id == exercise.id) {
                                        it.copy(selected = true, selectionOrder = nextSelectionOrder)
                                    } else it
                                })
                            }
                        }
                        .background(if (exercise.selected) Color.White else SnailDarkGray)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            confirmation.request(DeleteConfirmation) {
                            onExerciseItemsChange(
                                exerciseItems
                                    .filterNot { it.id == exercise.id }
                                    .normalizeSelectionOrder()
                            )
                            }
                        }
                    ) {
                        TrashIcon(tint = foregroundColor)
                    }

                    if (exercise.confirmed) {
                        Text(
                            text = exercise.name,
                            modifier = Modifier.weight(1f),
                            color = foregroundColor
                        )
                    } else {
                    OutlinedTextField(
                        value = exercise.name,
                        onValueChange = { newName ->
                            onExerciseItemsChange(exerciseItems.map {
                                if (it.id == exercise.id) {
                                    it.copy(
                                        name = newName,
                                        selected = it.selected && newName.isNotBlank(),
                                        selectionOrder = it.selectionOrder
                                            ?.takeIf { newName.isNotBlank() }
                                    )
                                } else {
                                    it
                                }
                            })
                        },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { state ->
                                if (state.isFocused) {
                                    hadFocus = true
                                } else if (hadFocus) {
                                    hadFocus = false
                                    confirmName()
                                }
                            },
                        placeholder = {
                            Text(
                                text = "Nombre del Ejercicio",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        },
                        textStyle = TextStyle(textAlign = TextAlign.Start),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            confirmName()
                            focusManager.clearFocus()
                        }),
                        singleLine = true
                    )
                    }

                    exercise.selectionOrder?.let { order ->
                        Text(
                            text = "${order}",
                            modifier = Modifier.padding(start = 8.dp),
                            color = foregroundColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                }
            }

            item(key = "create_exercise") {
                OutlinedButton(
                    onClick = {
                        val nextId = (exerciseItems.maxOfOrNull { it.id } ?: -1L) + 1L
                        val nextSelectionOrder =
                            (exerciseItems.mapNotNull { it.selectionOrder }.maxOrNull() ?: 0) + 1
                        onExerciseItemsChange(
                            exerciseItems.map {
                                it.copy(
                                    confirmed = it.name.isNotBlank(),
                                    selected = it.selected || (!it.confirmed && it.name.isNotBlank()),
                                    selectionOrder = if (!it.confirmed && it.name.isNotBlank()) {
                                        it.selectionOrder ?: nextSelectionOrder
                                    } else {
                                        it.selectionOrder
                                    }
                                )
                            }.sortedByExerciseName() + ExerciseItem(id = nextId)
                        )
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
                                .sortedBy { it.selectionOrder }
                                .map { it.name.trim() }
                        )
                    },
                    enabled = exerciseItems.none { !it.confirmed } &&
                        exerciseItems.any { it.selected && it.name.isNotBlank() },
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
}
