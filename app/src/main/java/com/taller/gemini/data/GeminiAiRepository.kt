package com.taller.gemini.data

import android.graphics.Bitmap
import com.squareup.moshi.Moshi
import com.taller.gemini.model.GeminiContent
import com.taller.gemini.model.GeminiGenerationConfig
import com.taller.gemini.model.GeminiInlineData
import com.taller.gemini.model.GeminiPart
import com.taller.gemini.model.GeminiRequest
import com.taller.gemini.model.IncidentInfo


/**
 * =============================================================================
 * TODO(PASO 5): Implementar GeminiAiRepository con la API real de Gemini
 * =============================================================================
 *
 * Implementa [AiRepository] usando [GeminiApi] con corrutinas.
 * Constructor: api, apiKey (BuildConfig.GEMINI_API_KEY), modelName (Constants.MODEL_NAME)
 * y moshi (para convertir el JSON de la clasificación en [IncidentInfo]).
 */
class GeminiAiRepository(
    private val api: GeminiApi,
    private val apiKey: String = com.taller.gemini.BuildConfig.GEMINI_API_KEY,
    private val modelName: String = com.taller.gemini.util.Constants.MODEL_NAME,
    private val moshi: Moshi = Moshi.Builder().build()
) : AiRepository {



    /**
     * TODO(PASO 5): Implementar askText
     *
     * 1. Arma un GeminiRequest con un GeminiContent y un GeminiPart(text = prompt).
     * 2. Dentro de un try-catch llama a api.generateContent(modelName, apiKey, request).
     * 3. Devuelve Result.success(texto) con response.firstText(), o Result.failure(...)
     *    si la respuesta viene vacía o hay un error de red.
     */
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





    /**
     * TODO(PASO 5): Implementar askAboutImage (multimodal)
     *
     * 1. Comprime el Bitmap a JPEG (calidad ~85) y codifícalo en Base64 con Base64.NO_WRAP.
     * 2. Crea dos GeminiPart: una con el texto del prompt y otra con
     *    inlineData = GeminiInlineData(mimeType = "image/jpeg", data = base64).
     * 3. Llama a la API y devuelve el texto envuelto en Result.success.
     */
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




    /**
     * TODO(PASO 5): Implementar classifyIncident (salida estructurada en JSON)
     *
     * 1. Escribe un prompt que pida SOLO un JSON con "categoria", "urgencia" y "resumen".
     * 2. Usa generationConfig = GeminiGenerationConfig(responseMimeType = "application/json").
     * 3. Limpia posibles delimitadores ```json ... ``` y deserializa con
     *    moshi.adapter(IncidentInfo::class.java).fromJson(...).
     * 4. Devuelve Result.success(incidente) o Result.failure(...).
     */
    override suspend fun classifyIncident(description: String): Result<IncidentInfo> {
        return try {
            val prompt = """
            Eres un sistema clasificador municipal de reportes urbanos.
            Analiza el siguiente reporte del ciudadano:
            "$description"
            
            Devuelve exclusivamente un JSON con estos 3 campos:
            - "categoria": Ejemplos: "N/A"", Malla Vial", "Agua y Alcantarillado", "Alumbrado Público", "Gestión Ambiental", "Seguridad"
            - "urgencia": Uno de: "N/A', "Baja", "Media", "Alta", "Crítica"
            - "resumen": Una frase sintética del problema.
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