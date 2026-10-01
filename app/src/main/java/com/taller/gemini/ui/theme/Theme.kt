package com.taller.gemini.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GeminiLightColorScheme = lightColorScheme(
    primary = GeminiBlue,
    onPrimary = Color.White,
    primaryContainer = GeminiBlueContainer,
    onPrimaryContainer = GeminiOnBlueContainer,
    secondary = GeminiTextSecondary,
    onSecondary = Color.White,
    secondaryContainer = GeminiSurfaceVariant,
    onSecondaryContainer = GeminiTextPrimary,
    tertiary = GeminiPurple,
    onTertiary = Color.White,
    tertiaryContainer = GeminiPurpleContainer,
    onTertiaryContainer = GeminiOnPurpleContainer,
    background = GeminiSurfaceLight,
    onBackground = GeminiTextPrimary,
    surface = GeminiSurfaceLight,
    onSurface = GeminiTextPrimary,
    surfaceVariant = GeminiSurfaceVariant,
    onSurfaceVariant = GeminiTextSecondary,
    surfaceContainerLowest = GeminiCard,
    surfaceContainerLow = GeminiSurfaceLight,
    surfaceContainer = Color(0xFFF1F3F4),
    surfaceContainerHigh = GeminiSurfaceVariant,
    surfaceContainerHighest = GeminiSurfaceVariant,
    outline = GeminiOutlineStrong,
    outlineVariant = GeminiOutline,
    error = GeminiError,
    onError = Color.White,
    errorContainer = GeminiErrorContainer,
    onErrorContainer = GeminiOnErrorContainer
)

@Composable
fun TallerGeminiAndroidTheme(
    content: @Composable () -> Unit
) {
    // Solo modo claro y sin colores dinámicos del sistema
    MaterialTheme(
        colorScheme = GeminiLightColorScheme,
        typography = Typography,
        content = content
    )
}
