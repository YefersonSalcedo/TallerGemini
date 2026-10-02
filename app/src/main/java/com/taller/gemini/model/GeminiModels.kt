package com.taller.gemini.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null,
    @Json(name = "inline_data")
    val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    @Json(name = "mime_type")
    val mimeType: String,
    val data: String // Codificado en Base64
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
) {
    fun firstText(): String? {
        return candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
    }
}

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null
)