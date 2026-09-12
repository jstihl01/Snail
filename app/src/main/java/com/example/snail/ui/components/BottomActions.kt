package com.example.snail.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

fun Modifier.bottomActionsLayout(): Modifier = this
    .fillMaxWidth()
    .navigationBarsPadding()
    .padding(horizontal = 16.dp, vertical = 16.dp)
