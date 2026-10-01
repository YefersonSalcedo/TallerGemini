package com.taller.gemini.data

/**
 * =============================================================================
 * TODO(PASO 4): Definir la interfaz Retrofit para la API de Google Gemini
 * =============================================================================
 *
 * PISTAS DE IMPLEMENTACIÓN:
 * 1. La URL base de Gemini es: https://generativelanguage.googleapis.com/
 * 2. El endpoint para generar contenido es:
 *    POST v1beta/models/{model}:generateContent
 * 3. La API Key se puede enviar en el encabezado HTTP:
 *    "x-goog-api-key: TU_KEY"
 *    o como parámetro de consulta:
 *    "?key=TU_KEY"
 * 4. Requiere un @Body con el objeto GeminiRequest y devuelve un GeminiResponse.
 */
import com.taller.gemini.model.GeminiRequest
import com.taller.gemini.model.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}