package com.taller.gemini.model

/**
 * =============================================================================
 * TODO(PASO 4): Definir los modelos de datos para la API REST de Google Gemini
 * =============================================================================
 *
 * PISTAS DE DISEÑO:
 * La API de Gemini espera una estructura JSON jerárquica:
 *
 * 1. Request:
 *    {
 *      "contents": [
 *        {
 *          "parts": [
 *            { "text": "Pregunta del usuario" },
 *            { "inline_data": { "mime_type": "image/jpeg", "data": "base64..." } }
 *          ]
 *        }
 *      ],
 *      "generationConfig": {
 *         "responseMimeType": "application/json" // Opcional, para respuestas JSON estructuradas
 *      }
 *    }
 *
 * 2. Response:
 *    {
 *      "candidates": [
 *        {
 *          "content": {
 *            "parts": [
 *              { "text": "Respuesta generada por el modelo..." }
 *            ]
 *          }
 *        }
 *      ]
 *    }
 *
 * RECUERDA:
 * - Anota cada clase con @kotlinx.serialization.Serializable
 * - Usa @SerialName("inline_data") o @SerialName("mime_type") si los nombres en JSON usan snake_case.
 * - Descomenta las clases que vayas definiendo a continuación.
 */


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

@Serializable
data class GeminiPart(
    val text: String? = null,
    @SerialName("inline_data")
    val inlineData: GeminiInlineData? = null
)

@Serializable
data class GeminiInlineData(
    @SerialName("mime_type")
    val mimeType: String,
    val data: String // Codificado en Base64
)

@Serializable
data class GeminiGenerationConfig(
    val responseMimeType: String? = null
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
) {
    fun firstText(): String? {
        return candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
    }
}

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)
