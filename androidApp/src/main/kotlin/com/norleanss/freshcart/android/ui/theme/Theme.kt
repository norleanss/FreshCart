package com.norleanss.freshcart.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF2E7D32)
private val GreenLight = Color(0xFF81C784)
private val Red = Color(0xFFD32F2F)

private val LightColors = lightColorScheme(
    primary = Green,
    secondary = Color(0xFFF9A825),
    error = Red,
)

private val DarkColors = darkColorScheme(
    primary = GreenLight,
    secondary = Color(0xFFFFD54F),
    error = Color(0xFFEF9A9A),
)

@Composable
fun FreshCartTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
