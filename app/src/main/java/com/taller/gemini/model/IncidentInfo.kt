package com.taller.gemini.model

/**
 * Representa la información estructurada de una incidencia ciudadana
 * analizada por Gemini.
 *
 * TODO(PASO 2 / PASO 4): Descomentar la anotación @Serializable cuando
 * descomentes el plugin y la librería de kotlinx.serialization en build.gradle.kts.
 */
@kotlinx.serialization.Serializable
data class IncidentInfo(
    val categoria: String,
    val urgencia: String,
    val resumen: String
)