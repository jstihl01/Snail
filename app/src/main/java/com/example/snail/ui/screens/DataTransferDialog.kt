package com.example.snail.ui.screens

import android.content.Intent
import android.content.ClipData
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.example.snail.data.*
import com.example.snail.ui.components.ActionText
import com.example.snail.ui.components.outlinedActionColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun DataTransferDialog(data: AppData, onImport: (AppData) -> Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var incoming by remember { mutableStateOf<AppData?>(null) }
    var selected by remember { mutableStateOf(ImportCategory.entries.toSet()) }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            try {
                incoming = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                        DataJson.decode(it.readText())
                    }
                        ?: error("No se pudo abrir el archivo")
                }
            } catch (e: Exception) { message = "No se importó ningún dato: ${e.message}" }
            busy = false
        }
    }
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text("Exportar / importar") }, text = {
        Column {
            message?.let { Text(it) }
            if (busy) CircularProgressIndicator()
            val backup = incoming
            if (backup != null) {
                Text("Selecciona qué añadir. Tus datos actuales se conservarán.")
                ImportCategory.entries.forEach { category ->
                    val count = when (category) { ImportCategory.EXERCISES -> backup.exercises.size; ImportCategory.ROUTINES -> backup.routines.size; ImportCategory.WORKOUTS -> backup.workouts.size }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(category in selected, { selected = if (it) selected + category else selected - category })
                        Text("${category.label} ($count)", Modifier.weight(1f))
                    }
                }
            } else {
                OutlinedButton(onClick = {
                    scope.launch {
                        busy = true
                        try {
                            val file = withContext(Dispatchers.IO) {
                                val folder = File(context.cacheDir, "exports").apply { mkdirs() }
                                File(folder, "Snail-${System.currentTimeMillis()}.json").also { it.writeText(DataJson.encode(data)) }
                            }
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"; putExtra(Intent.EXTRA_STREAM, uri)
                                clipData = ClipData.newRawUri("Copia de Snail", uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir copia de Snail"))
                        } catch (e: Exception) { message = "No se pudo exportar: ${e.message}" }
                        busy = false
                    }
                }, enabled = !busy, modifier = Modifier.fillMaxWidth(), colors = outlinedActionColors()) { ActionText("Exportar y compartir") }
                OutlinedButton(onClick = { picker.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
                    enabled = !busy, modifier = Modifier.fillMaxWidth(), colors = outlinedActionColors()) { ActionText("Importar archivo") }
            }
        }
    }, confirmButton = {
        if (incoming != null) TextButton(onClick = {
            try { if (onImport(data.importData(requireNotNull(incoming), selected))) onDismiss() }
            catch (e: Exception) { message = "No se importó ningún dato: ${e.message}" }
        }, enabled = !busy && selected.isNotEmpty()) { ActionText("Importar selección") }
    }, dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { ActionText("Cerrar") } })
}
