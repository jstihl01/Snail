package com.example.snail.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.snail.ui.models.SavedRoutine
import com.example.snail.ui.components.bottomActionsLayout
import com.example.snail.ui.models.CompletedExercise
import com.example.snail.ui.models.CompletedSet
import com.example.snail.ui.theme.SnailDarkGray
import com.example.snail.ui.theme.routineColorFor
import com.example.snail.ui.theme.SnailLightGray

private data class TrainingSetInput(
    val number: Int,
    val kilograms: String = "",
    val repetitions: String = ""
) {
    val isComplete: Boolean
        get() = kilograms.isNotBlank() && repetitions.isNotBlank()
}

private data class TrainingExerciseInput(
    val id: Long,
    val name: String,
    val targetRepetitions: String,
    val rir: String,
    val sets: List<TrainingSetInput>
)

@Composable
fun NuevoEntrenamientoScreen(
    routine: SavedRoutine,
    onBack: () -> Unit,
    onSave: (List<CompletedExercise>) -> Unit
) {
    var exercises by remember(routine.id) {
        mutableStateOf(
            routine.exercises.map { exercise ->
                val setCount = exercise.series.toIntOrNull()?.coerceAtLeast(0) ?: 0
                TrainingExerciseInput(
                    id = exercise.id,
                    name = exercise.name,
                    targetRepetitions = exercise.repetitions,
                    rir = exercise.rir,
                    sets = List(setCount) { index -> TrainingSetInput(number = index + 1) }
                )
            }
        )
    }
    val canSave = exercises.any { exercise -> exercise.sets.any { it.isComplete } }

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
                .padding(start = 16.dp, top = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = exercises,
                key = { it.id }
            ) { exercise ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(routineColorFor(routine.colorIndex))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = exercise.name,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White
                    )

                    exercise.sets.forEach { set ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = set.number.toString(),
                                modifier = Modifier.weight(0.35f),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            TrainingValueField(
                                value = set.kilograms,
                                onValueChange = { value ->
                                    exercises = exercises.updateSet(exercise.id, set.number) {
                                        it.copy(kilograms = value)
                                    }
                                },
                                placeholder = "KG",
                                keyboardType = KeyboardType.Decimal,
                                modifier = Modifier.weight(1f)
                            )

                            TrainingValueField(
                                value = set.repetitions,
                                onValueChange = { value ->
                                    exercises = exercises.updateSet(exercise.id, set.number) {
                                        it.copy(repetitions = value)
                                    }
                                },
                                placeholder = exercise.targetRepetitions,
                                keyboardType = KeyboardType.Number,
                                modifier = Modifier.weight(1f)
                            )

                            Box(
                                modifier = Modifier.weight(0.55f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = exercise.rir,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }

                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.bottomActionsLayout()
        ) {
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
                    onSave(
                        exercises.mapNotNull { exercise ->
                            val completedSets = exercise.sets
                                .filter { it.isComplete }
                                .map { set ->
                                    CompletedSet(
                                        number = set.number,
                                        kilograms = set.kilograms.trim(),
                                        repetitions = set.repetitions.trim(),
                                        rir = exercise.rir
                                    )
                                }
                            if (completedSets.isEmpty()) {
                                null
                            } else {
                                CompletedExercise(
                                    name = exercise.name,
                                    sets = completedSets
                                )
                            }
                        }
                    )
                },
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

@Composable
private fun TrainingValueField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
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
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

private fun List<TrainingExerciseInput>.updateSet(
    exerciseId: Long,
    setNumber: Int,
    update: (TrainingSetInput) -> TrainingSetInput
): List<TrainingExerciseInput> = map { exercise ->
    if (exercise.id == exerciseId) {
        exercise.copy(
            sets = exercise.sets.map { set ->
                if (set.number == setNumber) update(set) else set
            }
        )
    } else {
        exercise
    }
}
