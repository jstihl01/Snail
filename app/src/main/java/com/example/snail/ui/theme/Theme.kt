package com.example.snail.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val SnailColorScheme = darkColorScheme(
    primary = SnailWhite,
    onPrimary = SnailBlack,
    primaryContainer = SnailDarkGray,
    onPrimaryContainer = SnailWhite,
    secondary = SnailLightGray,
    onSecondary = SnailBlack,
    secondaryContainer = SnailDarkGray,
    onSecondaryContainer = SnailWhite,
    tertiary = SnailMediumGray,
    onTertiary = SnailWhite,
    background = SnailBlack,
    onBackground = SnailWhite,
    surface = SnailBlack,
    onSurface = SnailWhite,
    surfaceVariant = SnailDarkGray,
    onSurfaceVariant = SnailLightGray,
    outline = SnailWhite,
    outlineVariant = SnailMediumGray,
    error = SnailWhite,
    onError = SnailBlack
)

@Composable
fun SnailTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SnailColorScheme,
        typography = Typography,
    ) {
        CompositionLocalProvider(LocalContentColor provides SnailWhite, content = content)
    }
}
