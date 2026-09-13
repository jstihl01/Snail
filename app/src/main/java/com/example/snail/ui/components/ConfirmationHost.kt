package com.example.snail.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

class ConfirmationState {
    var action by mutableStateOf<(() -> Unit)?>(null)
        private set
    var message by mutableStateOf("")
        private set

    fun request(message: String, action: () -> Unit) {
        this.message = message
        this.action = action
    }

    fun dismiss() { action = null }
    fun confirm() {
        val pending = action
        dismiss()
        pending?.invoke()
    }
}

const val ExitConfirmation = "¿Está seguro de que desea salir sin guardar?"
const val DeleteConfirmation = "¿Está seguro de que desea borrar este elemento?"
private val ConfirmationRed = Color(0xFFC98282)

@Composable
fun rememberConfirmationState(): ConfirmationState = remember { ConfirmationState() }

@Composable
fun ConfirmationHost(state: ConfirmationState, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().then(if (state.action != null) Modifier.blur(8.dp) else Modifier)) {
        content()
    }
    if (state.action != null) {
        Dialog(
            onDismissRequest = state::dismiss,
            properties = DialogProperties(dismissOnClickOutside = false)
        ) {
            Column(
                Modifier.fillMaxWidth().background(Color.Black, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(state.message, color = Color.White, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = state::dismiss, modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color.White),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Black, contentColor = Color.White)) { Text("No") }
                    OutlinedButton(onClick = state::confirm, modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, ConfirmationRed),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Black, contentColor = ConfirmationRed)) { Text("Sí") }
                }
            }
        }
    }
}
