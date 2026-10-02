package com.taller.gemini.data

import com.taller.gemini.model.GeminiRequest
import com.taller.gemini.model.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * =============================================================================
 * TODO(PASO 4): Definir la interfaz Retrofit para la API de Google Gemini
 * =============================================================================
 *
 * 1. URL base: https://generativelanguage.googleapis.com/
 * 2. Endpoint: POST v1beta/models/{model}:generateContent
 * 3. La API key va en el header "x-goog-api-key".
 * 4. Recibe un @Body GeminiRequest y devuelve un GeminiResponse (función suspend).
 */
interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}