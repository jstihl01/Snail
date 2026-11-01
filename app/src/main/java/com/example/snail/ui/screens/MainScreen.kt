package com.example.snail.ui.screens
import com.example.snail.ui.components.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.snail.ui.components.TrashIcon
import com.example.snail.ui.components.bottomActionsLayout
import com.example.snail.ui.models.SavedWorkout
import com.example.snail.ui.theme.routineHistoryColorFor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    workouts: List<SavedWorkout>,
    canStartTraining: Boolean,
    onNewRoutine: () -> Unit,
    onNewTraining: () -> Unit,
    onDeleteWorkout: (Long) -> Unit,
    onEditWorkout: (SavedWorkout) -> Unit,
    onTransfer: () -> Unit,
    onProgress: () -> Unit
) {
    val confirmation = rememberConfirmationState()
    val historyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = workouts.lastIndex.coerceAtLeast(0)
    )

    LaunchedEffect(workouts.lastOrNull()?.id) {
        if (workouts.isNotEmpty()) {
            historyListState.scrollToItem(workouts.lastIndex)
        }
    }

    ConfirmationHost(confirmation) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onTransfer, modifier = Modifier.semanticsLabel("Exportar o importar datos")) { Text("⇅") }
        }
        LazyColumn(
            state = historyListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom)
        ) {
            itemsIndexed(
                items = workouts,
                key = { _, workout -> workout.id }
            ) { index, workout ->
                val historyContentColor = Color.White.copy(alpha = 0.68f)
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val previousWeek = workouts.getOrNull(index - 1)?.completedAt?.weekStart()
                    val currentWeek = workout.completedAt.weekStart()
                    if (previousWeek != null && currentWeek != null &&
                        previousWeek != currentWeek) {
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = Color.Gray
                        )
                    }

                    Text(
                        text = workout.completedAt.toDisplayDateTime(),
                        modifier = Modifier.fillMaxWidth(),
                        color = historyContentColor
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(routineHistoryColorFor(workout.colorIndex))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            confirmation.request(DeleteConfirmation) { onDeleteWorkout(workout.id) }
                        }) {
                            TrashIcon(
                                tint = historyContentColor,
                                contentDescription = "Eliminar entrenamiento"
                            )
                        }
                        Text(
                            text = workout.routineName,
                            modifier = Modifier.weight(1f),
                            color = historyContentColor
                        )
                        Text(
                            text = "${workout.startMotivation} ➜ ${workout.endMotivation}",
                            color = historyContentColor
                        )
                        IconButton(onClick = { onEditWorkout(workout) }, modifier = Modifier.semanticsLabel("Editar entrenamiento")) {
                            Text("✎", color = historyContentColor)
                        }
                    }

                    workout.exercises.forEach { exercise ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    border = BorderStroke(1.dp, historyContentColor),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            WorkoutCell(
                                text = exercise.name,
                                modifier = Modifier.fillMaxWidth(),
                                color = historyContentColor
                            )
                            exercise.sets.forEach { set ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min)
                                ) {
                                    WorkoutCell(
                                        "${set.number}${if (set.isExtra) " +" else ""}",
                                        Modifier.weight(1f),
                                        historyContentColor
                                    )
                                    WorkoutCell(
                                        text = if (exercise.isCardio) "${set.intensity} Intensidad × ${set.minutes} Minutos"
                                            else "${set.kilograms} KG × ${set.repetitions} Reps.",
                                        modifier = Modifier.weight(2f),
                                        color = historyContentColor
                                    )
                                }
                            }
                        }
                    }
                    }
                }
            }
        }

        OutlinedButton(onClick = onProgress, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp),
            border = BorderStroke(1.dp, Color.White), colors = outlinedActionColors()) { ActionText("Progreso") }
        Row(
            modifier = Modifier.bottomActionsLayout()
        ) {
            OutlinedButton(
                onClick = onNewRoutine,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                border = BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                ActionText("+ Rutina")
            }

            Button(
                onClick = onNewTraining,
                enabled = canStartTraining,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = com.example.snail.ui.theme.SnailDarkGray,
                    disabledContentColor = com.example.snail.ui.theme.SnailLightGray
                )
            ) {
                ActionText("+ Entrenamiento")
            }
        }
    }
}

}

private fun Long.weekStart(): String? {
    if (this <= 0L) return null
    val calendar = Calendar.getInstance().apply {
        timeInMillis = this@weekStart
        add(Calendar.DAY_OF_MONTH, -((get(Calendar.DAY_OF_WEEK) + 5) % 7))
    }
    return SimpleDateFormat("yyyyMMdd", Locale.ROOT).format(calendar.time)
}

private fun Long.toDisplayDateTime(): String {
    if (this <= 0L) return "Fecha y hora no disponibles"
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(this))
}

@Composable
private fun WorkoutCell(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .border(0.5.dp, color)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            color = color,
            textAlign = TextAlign.Center
        )
    }
}
