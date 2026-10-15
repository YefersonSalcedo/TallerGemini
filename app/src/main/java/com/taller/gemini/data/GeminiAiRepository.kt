package com.taller.gemini.data

import android.graphics.Bitmap
import com.squareup.moshi.Moshi
import com.taller.gemini.model.GeminiContent
import com.taller.gemini.model.GeminiGenerationConfig
import com.taller.gemini.model.GeminiInlineData
import com.taller.gemini.model.GeminiPart
import com.taller.gemini.model.GeminiRequest
import com.taller.gemini.model.IncidentInfo


class GeminiAiRepository(
    private val api: GeminiApi,
    private val apiKey: String = com.taller.gemini.BuildConfig.GEMINI_API_KEY,
    private val modelName: String = com.taller.gemini.util.Constants.MODEL_NAME,
    private val moshi: Moshi = Moshi.Builder().build()
) : AiRepository {


    override suspend fun askText(prompt: String): Result<String> {
        return try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt))
                    )
                )
            )
            val response = api.generateContent(modelName, apiKey, request)
            val text = response.firstText()
            if (text != null) {
                Result.success(text)
            } else {
                Result.failure(Exception("El modelo devolvió una respuesta vacía"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun askAboutImage(image: Bitmap, prompt: String): Result<String> {
        return try {
            val outputStream = java.io.ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val base64Image = android.util.Base64.encodeToString(
                outputStream.toByteArray(),
                android.util.Base64.NO_WRAP
            )

            val parts = listOf(
                GeminiPart(text = prompt),
                GeminiPart(
                    inlineData = GeminiInlineData(
                        mimeType = "image/jpeg",
                        data = base64Image
                    )
                )
            )

            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = parts))
            )

            val response = api.generateContent(modelName, apiKey, request)
            val text = response.firstText()
            if (text != null) {
                Result.success(text)
            } else {
                Result.failure(Exception("Sin respuesta de análisis de imagen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun classifyIncident(description: String): Result<IncidentInfo> {
        return try {
            val prompt = """
            Eres un sistema clasificador municipal de reportes urbanos.
            Analiza el siguiente reporte del ciudadano:
            "$description"
            
            Devuelve exclusivamente un JSON válido con estos tres campos:
            - "categoria": una categoría como "Malla Vial",
              "Agua y Alcantarillado", "Alumbrado Público",
              "Gestión Ambiental" o "Seguridad".
            - "urgencia": "Baja", "Media", "Alta" o "Crítica".
            - "resumen": una frase breve que describa el problema.
            
            Si el texto no describe una incidencia urbana o no encaja en ninguna
            categoría, usa "N/A" en "categoria" y en "urgencia".
        """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = prompt))
                    )
                ),
                generationConfig = GeminiGenerationConfig(responseMimeType = "application/json")
            )

            val response = api.generateContent(modelName, apiKey, request)
            val rawJson = response.firstText() ?: return Result.failure(Exception("JSON no recibido"))

            // Limpia delimitadores si el modelo incluyera ```json ... ```
            val cleanJson = rawJson
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val incidentInfo = moshi.adapter(IncidentInfo::class.java).fromJson(cleanJson)
                ?: return Result.failure(Exception("JSON vacío o inválido"))

            Result.success(incidentInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}