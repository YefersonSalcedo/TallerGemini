package com.taller.gemini.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Acentos Gemini
val GeminiBlue = Color(0xFF1A73E8)
val GeminiBlueContainer = Color(0xFFD3E3FD)
val GeminiOnBlueContainer = Color(0xFF041E49)
val GeminiPurple = Color(0xFF9177C7)
val GeminiPurpleContainer = Color(0xFFEADDFF)
val GeminiOnPurpleContainer = Color(0xFF2B1B52)
val GeminiPink = Color(0xFFCA6673)

// Superficies y textos
val GeminiSurfaceLight = Color(0xFFF8F9FA)
val GeminiSurfaceVariant = Color(0xFFE8EAED)
val GeminiCard = Color.White
val GeminiTextPrimary = Color(0xFF1F1F1F)
val GeminiTextSecondary = Color(0xFF5F6368)

// Bordes: "strong" para campos de texto (contraste suficiente), "variant" para tarjetas
val GeminiOutlineStrong = Color(0xFF80868B)
val GeminiOutline = Color(0xFFDADCE0)

// Errores
val GeminiError = Color(0xFFD93025)
val GeminiErrorContainer = Color(0xFFFCE8E6)
val GeminiOnErrorContainer = Color(0xFF601410)

// Urgencia de incidencias (antes estaban escritas a mano en JsonScreen)
val UrgencyCritical = Color(0xFFB00020)
val UrgencyCriticalBg = Color(0xFFFFEBEE)
val UrgencyHigh = Color(0xFFD84315)
val UrgencyHighBg = Color(0xFFFBE9E7)
val UrgencyMedium = Color(0xFF8D5A00)
val UrgencyMediumBg = Color(0xFFFFF3CD)
val UrgencyLow = Color(0xFF2E7D32)
val UrgencyLowBg = Color(0xFFE8F5E9)

// Degradado característico de Gemini (útil para títulos, botones o el logo)
val GeminiGradient = Brush.horizontalGradient(
    listOf(Color(0xFF4796E3), Color(0xFF9177C7), Color(0xFFCA6673))
)
