package com.taller.gemini.model

//import com.squareup.moshi.JsonClass

// TODO(PASO 4): Descomentar @JsonClass(generateAdapter = true) (requiere KSP del Paso 2).
// @JsonClass(generateAdapter = true)
data class IncidentInfo(
    val categoria: String,
    val urgencia: String,
    val resumen: String
)