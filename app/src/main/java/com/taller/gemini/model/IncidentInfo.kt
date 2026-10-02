package com.taller.gemini.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IncidentInfo(
    val categoria: String,
    val urgencia: String,
    val resumen: String
)