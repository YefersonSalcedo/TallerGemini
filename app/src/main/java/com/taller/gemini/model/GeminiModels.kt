package com.taller.gemini.model

/*
 * =============================================================================
 * TODO(PASO 4): Definir los modelos de datos para la API REST de Gemini
 * =============================================================================
 *
 * Estructura jerárquica del JSON:
 * - Request:  contents -> parts -> (text | inline_data { mime_type, data })
 *             + generationConfig { responseMimeType } (opcional, para JSON estructurado)
 * - Response: candidates -> content -> parts -> text
 *
 * RECUERDA:
 * - Anota cada clase con @JsonClass(generateAdapter = true) (Moshi + KSP).
 * - Usa @Json(name = "inline_data") y @Json(name = "mime_type") donde el JSON usa snake_case.
 * - Descomenta las clases que vayas definiendo.
 */