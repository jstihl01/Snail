package com.example.snail.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.snail.ui.components.*
import com.example.snail.ui.models.*
import com.example.snail.ui.theme.SnailDarkGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgresoScreen(workouts: List<SavedWorkout>, onBack: () -> Unit) {
    BackHandler { onBack() }
    val groups = remember(workouts) { workouts.exerciseProgress() }
    Column(Modifier.fillMaxSize().background(Color.Black).statusBarsPadding()) {
        LazyColumn(Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("Progreso", style = MaterialTheme.typography.titleLarge) }
            item { Chart("Volumen total · KG × Reps.", workouts.volumeProgress()) }
            item { Chart("Motivación · final − inicial", workouts.motivationProgress(), true) }
            groups.forEach { (group, exercises) ->
                item { Text(group, style = MaterialTheme.typography.titleMedium) }
                exercises.forEach { (name, points) -> item { Chart(name, points) } }
            }
        }
        OutlinedButton(onBack, Modifier.bottomActionsLayout(), border = BorderStroke(1.dp, Color.White), colors = outlinedActionColors()) { ActionText("Volver") }
    }
}

@Composable
private fun Chart(title: String, points: List<ProgressPoint>, motivation: Boolean = false) {
    var expanded by remember { mutableStateOf(false) }
    val date = remember { SimpleDateFormat("dd/MM/yy", Locale.forLanguageTag("es")) }
    Column(Modifier.fillMaxWidth().background(SnailDarkGray, RoundedCornerShape(12.dp)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title)
        if (points.isEmpty()) { Text("Todavía no hay datos"); return@Column }
        val min = if (motivation) -4.0 else 0.0
        val max = if (motivation) 4.0 else maxOf(1.0, points.maxOf { it.value })
        Text("${"%.1f".format(min)} — ${"%.1f".format(max)}", style = MaterialTheme.typography.bodySmall)
        Canvas(Modifier.fillMaxWidth().height(160.dp).semanticsLabel("$title. ${points.size} entrenamientos. Último valor ${points.last().value}")) {
            val margin = 8.dp.toPx()
            fun x(index: Int) = if (points.size == 1) size.width / 2 else margin + index * (size.width - 2 * margin) / (points.size - 1)
            fun y(value: Double) = (size.height - margin - (value - min) / (max - min) * (size.height - 2 * margin)).toFloat()
            drawLine(Color.Gray, Offset(margin, y(0.0)), Offset(size.width - margin, y(0.0)), 1.dp.toPx())
            val path = Path()
            points.forEachIndexed { index, point ->
                if (index == 0) path.moveTo(x(index), y(point.value)) else path.lineTo(x(index), y(point.value))
                drawCircle(Color.White, 3.dp.toPx(), Offset(x(index), y(point.value)))
            }
            drawPath(path, Color.White, style = Stroke(2.dp.toPx()))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(date.format(Date(points.first().date)), style = MaterialTheme.typography.bodySmall)
            Text(date.format(Date(points.last().date)), style = MaterialTheme.typography.bodySmall)
        }
        TextButton(onClick = { expanded = !expanded }) { ActionText(if (expanded) "Ocultar valores" else "Ver valores") }
        if (expanded) points.forEach { Text("${date.format(Date(it.date))} · ${it.label}: ${"%.2f".format(it.value)}", style = MaterialTheme.typography.bodySmall) }
    }
}
