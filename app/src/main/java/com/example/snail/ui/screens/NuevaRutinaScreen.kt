package com.example.snail.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.snail.ui.components.TrashIcon
import com.example.snail.ui.models.RoutineExercise
import com.example.snail.ui.theme.SnailDarkGray
import com.example.snail.ui.theme.SnailLightGray

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
    val canSave = routineName.isNotBlank() && exerciseItems.all { exercise ->
        exercise.series.isNotBlank() && exercise.repetitions.isNotBlank()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
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
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
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
                    Text(
                        text = exercise.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                        ExerciseValueField(
                            value = exercise.series,
                            onValueChange = { value ->
                                onExerciseItemsChange(exerciseItems.map {
                                    if (it.id == exercise.id) it.copy(series = value) else it
                                })
                            },
                            placeholder = "Series*",
                            modifier = Modifier.weight(1f)
                        )
                        ExerciseValueField(
                            value = exercise.repetitions,
                            onValueChange = { value ->
                                onExerciseItemsChange(exerciseItems.map {
                                    if (it.id == exercise.id) it.copy(repetitions = value) else it
                                })
                            },
                            placeholder = "Reps.*",
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
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
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

@Composable
private fun ExerciseValueField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = placeholder,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        textStyle = TextStyle(textAlign = TextAlign.Center),
        singleLine = true
    )
}
