package com.example.snail.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.snail.ui.components.TrashIcon
import com.example.snail.ui.models.SavedRoutine
import com.example.snail.ui.theme.SnailDarkGray
import com.example.snail.ui.theme.SnailLightGray

@Composable
fun MisRutinasScreen(
    routines: List<SavedRoutine>,
    onBack: () -> Unit,
    onDeleteRoutine: (Long) -> Unit
) {
    var selectedRoutineId by rememberSaveable { mutableStateOf<Long?>(null) }

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
                .padding(start = 16.dp, top = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = routines,
                key = { it.id }
            ) { routine ->
                val selected = selectedRoutineId == routine.id
                val backgroundColor = if (selected) Color.White else SnailDarkGray
                val foregroundColor = if (selected) Color.Black else Color.White

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            selectedRoutineId = if (selected) null else routine.id
                        }
                        .background(backgroundColor)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (selectedRoutineId == routine.id) {
                                    selectedRoutineId = null
                                }
                                onDeleteRoutine(routine.id)
                            }
                        ) {
                            TrashIcon(
                                tint = foregroundColor,
                                contentDescription = "Eliminar rutina"
                            )
                        }

                        Text(
                            text = routine.name,
                            modifier = Modifier.weight(1f),
                            color = foregroundColor
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                border = BorderStroke(1.dp, foregroundColor),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            TableCell("Ejercicio", Modifier.weight(1f), foregroundColor)
                            TableCell("Series", Modifier.weight(1f), foregroundColor)
                            TableCell("Reps.", Modifier.weight(1f), foregroundColor)
                            TableCell("RIR", Modifier.weight(1f), foregroundColor)
                        }

                        routine.exercises.forEach { exercise ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                            ) {
                                TableCell(
                                    text = exercise.name,
                                    modifier = Modifier.weight(1f),
                                    color = foregroundColor,
                                    textAlign = TextAlign.Start
                                )
                                TableCell(exercise.series, Modifier.weight(1f), foregroundColor)
                                TableCell(exercise.repetitions, Modifier.weight(1f), foregroundColor)
                                TableCell(exercise.rir, Modifier.weight(1f), foregroundColor)
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
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
                onClick = {},
                enabled = selectedRoutineId != null,
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
                Text("Empezar")
            }
        }
    }
}

@Composable
private fun TableCell(
    text: String,
    modifier: Modifier = Modifier,
    color: Color,
    textAlign: TextAlign = TextAlign.Center
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
            textAlign = textAlign
        )
    }
}
