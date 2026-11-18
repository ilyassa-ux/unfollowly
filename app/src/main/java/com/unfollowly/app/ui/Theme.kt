package com.unfollowly.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Colors = darkColorScheme(
    primary = Color(0xFF9C7BFF),
    secondary = Color(0xFF55D6BE),
    background = Color(0xFF111017),
    surface = Color(0xFF1B1923),
    surfaceVariant = Color(0xFF252230),
    onBackground = Color(0xFFF6F3FF),
    onSurface = Color(0xFFF6F3FF),
    error = Color(0xFFFF6B82)
)

@Composable fun UnfollowlyTheme(content: @Composable () -> Unit) = MaterialTheme(colorScheme = Colors, content = content)
