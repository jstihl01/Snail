package com.example.snail.ui.screens
import com.example.snail.ui.components.*
import androidx.activity.compose.BackHandler

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.snail.ui.components.TrashIcon
import com.example.snail.ui.components.bottomActionsLayout
import com.example.snail.ui.models.RoutineExercise
import com.example.snail.ui.theme.SnailDarkGray
import com.example.snail.ui.theme.SnailLightGray

private data class RoutineFieldKey(val exerciseId: Long, val field: Int)

@Composable
fun NuevaRutinaScreen(
    routineName: String,
    onRoutineNameChange: (String) -> Unit,
    exerciseItems: List<RoutineExercise>,
    onExerciseItemsChange: (List<RoutineExercise>) -> Unit,
    onBack: () -> Unit,
    onNewExercise: () -> Unit,
    onSave: () -> Unit
) {
    val confirmation = rememberConfirmationState()
    val requestExit: () -> Unit = {
        if (routineName.isNotEmpty() || exerciseItems.isNotEmpty()) {
            confirmation.request(ExitConfirmation, onBack)
        } else onBack()
    }
    BackHandler { requestExit() }
    val canSave = routineName.isNotBlank() && exerciseItems.isNotEmpty() && exerciseItems.all { exercise ->
        val minimum = exercise.minRepetitions.replace(',', '.').toBigDecimalOrNull()
        val maximum = exercise.maxRepetitions.replace(',', '.').toBigDecimalOrNull()
        exercise.series.isNotBlank() && minimum != null && minimum.signum() >= 0 &&
            (exercise.maxRepetitions.isBlank() || (maximum != null && maximum >= minimum))
    }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val fieldOrder = exerciseItems.flatMap { exercise ->
        List(4) { field -> RoutineFieldKey(exercise.id, field) }
    }
    var pendingFocus by remember { mutableStateOf<RoutineFieldKey?>(null) }

    LaunchedEffect(pendingFocus) {
        val target = pendingFocus ?: return@LaunchedEffect
        val index = exerciseItems.indexOfFirst { it.id == target.exerciseId }
        if (index >= 0 && listState.layoutInfo.visibleItemsInfo.none { it.index == index }) {
            listState.scrollToItem(index)
        }
    }

    fun advanceFocus(current: RoutineFieldKey) {
        val next = fieldOrder.getOrNull(fieldOrder.indexOf(current) + 1)
        if (next == null) {
            focusManager.clearFocus()
            keyboard?.hide()
        } else {
            pendingFocus = next
        }
    }

    ConfirmationHost(confirmation) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .imePadding()
    ) {
        OutlinedTextField(
            value = routineName,
            onValueChange = onRoutineNameChange,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            placeholder = {
                Text(
                    text = "Nombre de la Rutina",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            textStyle = TextStyle(textAlign = TextAlign.Center),
            singleLine = true
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = exerciseItems,
                key = { it.id }
            ) { exercise ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SnailDarkGray)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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
                        Text(
                            text = exercise.name,
                            modifier = Modifier.weight(1f),
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExerciseValueField(
                            value = exercise.series,
                            onValueChange = { value ->
                                onExerciseItemsChange(exerciseItems.map {
                                    if (it.id == exercise.id) it.copy(series = value) else it
                                })
                            },
                            placeholder = "Series*",
                            requestFocus = pendingFocus == RoutineFieldKey(exercise.id, 0),
                            onFocused = { if (pendingFocus == RoutineFieldKey(exercise.id, 0)) pendingFocus = null },
                            onKeyboardAction = { advanceFocus(RoutineFieldKey(exercise.id, 0)) },
                            isLast = RoutineFieldKey(exercise.id, 0) == fieldOrder.lastOrNull(),
                            modifier = Modifier.weight(1f)
                        )
                        ExerciseValueField(
                            value = exercise.minRepetitions,
                            onValueChange = { value ->
                                onExerciseItemsChange(exerciseItems.map {
                                    if (it.id == exercise.id) it.copy(minRepetitions = value) else it
                                })
                            },
                            placeholder = "Mín. Reps.*",
                            requestFocus = pendingFocus == RoutineFieldKey(exercise.id, 1),
                            onFocused = { if (pendingFocus == RoutineFieldKey(exercise.id, 1)) pendingFocus = null },
                            onKeyboardAction = { advanceFocus(RoutineFieldKey(exercise.id, 1)) },
                            isLast = RoutineFieldKey(exercise.id, 1) == fieldOrder.lastOrNull(),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExerciseValueField(
                            value = exercise.maxRepetitions,
                            onValueChange = { value ->
                                onExerciseItemsChange(exerciseItems.map {
                                    if (it.id == exercise.id) it.copy(maxRepetitions = value) else it
                                })
                            },
                            placeholder = "Máx. Reps.",
                            requestFocus = pendingFocus == RoutineFieldKey(exercise.id, 2),
                            onFocused = { if (pendingFocus == RoutineFieldKey(exercise.id, 2)) pendingFocus = null },
                            onKeyboardAction = { advanceFocus(RoutineFieldKey(exercise.id, 2)) },
                            isLast = RoutineFieldKey(exercise.id, 2) == fieldOrder.lastOrNull(),
                            modifier = Modifier.weight(1f)
                        )
                        ExerciseValueField(
                            value = exercise.rir,
                            onValueChange = { value ->
                                onExerciseItemsChange(exerciseItems.map {
                                    if (it.id == exercise.id) it.copy(rir = value) else it
                                })
                            },
                            placeholder = "RIR",
                            requestFocus = pendingFocus == RoutineFieldKey(exercise.id, 3),
                            onFocused = { if (pendingFocus == RoutineFieldKey(exercise.id, 3)) pendingFocus = null },
                            onKeyboardAction = { advanceFocus(RoutineFieldKey(exercise.id, 3)) },
                            isLast = RoutineFieldKey(exercise.id, 3) == fieldOrder.lastOrNull(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item(key = "add_exercise") {
                OutlinedButton(
                    onClick = onNewExercise,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text("Añadir Ejercicio")
                }
            }
        }

        Column(
            modifier = Modifier.bottomActionsLayout()
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = requestExit,
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
                    onClick = onSave,
                    enabled = canSave,
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
                    Text("Guardar")
                }
            }
        }
    }
}

}

@Composable
private fun ExerciseValueField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    requestFocus: Boolean,
    onFocused: () -> Unit,
    onKeyboardAction: () -> Unit,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    val requester = remember { FocusRequester() }
    LaunchedEffect(requestFocus) {
        if (requestFocus) requester.requestFocus()
    }
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val normalized = newValue.replace('.', ',')
            if (normalized.isDecimalInput()) onValueChange(normalized)
        },
        modifier = modifier
            .focusRequester(requester)
            .onFocusChanged { if (it.isFocused) onFocused() },
        placeholder = {
            Text(
                text = placeholder,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        textStyle = TextStyle(textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = if (isLast) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { onKeyboardAction() },
            onDone = { onKeyboardAction() }
        ),
        singleLine = true
    )
}

private fun String.isDecimalInput(): Boolean = matches(Regex("\\d*(,\\d*)?"))
