package com.example.snail

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.snail.ui.screens.NuevaRutinaScreen
import com.example.snail.ui.theme.SnailTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SnailTheme {
                var currentScreen by rememberSaveable { mutableStateOf(AppScreen.MAIN) }

                BackHandler(enabled = currentScreen != AppScreen.MAIN) {
                    currentScreen = AppScreen.MAIN
                }

                when (currentScreen) {
                    AppScreen.MAIN -> MainScreen(
                        onNewRoutine = { currentScreen = AppScreen.NEW_ROUTINE }
                    )

                    AppScreen.NEW_ROUTINE -> NuevaRutinaScreen()
                }
            }
        }
    }
}

private enum class AppScreen {
    MAIN,
    NEW_ROUTINE
}

@Composable
private fun MainScreen(onNewRoutine: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
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
                Text("+ Rutina")
            }

            Button(
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text("+ Entrenamiento")
            }
        }
    }
}
