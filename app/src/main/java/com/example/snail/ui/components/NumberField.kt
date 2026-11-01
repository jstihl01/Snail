package com.example.snail.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NumberField(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier,
    focus: Boolean = false, onFocused: () -> Unit = {}, onNext: () -> Unit = {}, last: Boolean = false,
    boldHint: Boolean = false, integer: Boolean = false, allowRange: Boolean = false, label: String = hint) {
    val requester = remember { FocusRequester() }
    val bring = remember { BringIntoViewRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val keyboardBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(focus) {
        if (focus) {
            withFrameNanos { }
            requester.requestFocus()
            keyboard?.show()
        }
    }
    // Reposition after the keyboard has reduced the viewport, including partially visible rows.
    LaunchedEffect(focused, keyboardBottom) {
        if (focused) { withFrameNanos { }; bring.bringIntoView() }
    }
    OutlinedTextField(value, { input ->
        val normalized = input.replace('.', ',').replace('–', '-').replace('—', '-')
        val pattern = when {
            integer -> Regex("\\d*")
            allowRange -> Regex("\\d*(,\\d*)?(-\\d*(,\\d*)?)?")
            else -> Regex("\\d*(,\\d*)?")
        }
        if (normalized.matches(pattern)) onChange(normalized)
    }, modifier = modifier.focusRequester(requester).bringIntoViewRequester(bring).onFocusChanged {
        focused = it.isFocused
        if (it.isFocused) onFocused()
    }.semanticsLabel(label), singleLine = true,
        placeholder = { Text(hint, Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
            fontWeight = if (boldHint) FontWeight.Bold else FontWeight.Normal, maxLines = 1) },
        textStyle = TextStyle(textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(keyboardType = if (integer) KeyboardType.Number else KeyboardType.Decimal,
            imeAction = if (last) ImeAction.Done else ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { onNext() }, onDone = { onNext() }))
}
