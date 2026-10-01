package com.taller.gemini.data

import android.graphics.Bitmap
import com.taller.gemini.model.GeminiContent
import com.taller.gemini.model.GeminiGenerationConfig
import com.taller.gemini.model.GeminiInlineData
import com.taller.gemini.model.GeminiPart
import com.taller.gemini.model.GeminiRequest
import com.taller.gemini.model.IncidentInfo


/**
 * =============================================================================
 * TODO(PASO 5): Implementar GeminiAiRepository conectando la API real de Gemini
 * =============================================================================
 *
 * Esta clase implementa el contrato [AiRepository] consumiendo la interfaz
 * Retrofit [GeminiApi] mediante corrutinas de Kotlin.
 *
 * Parámetros en el constructor:
 * - api: GeminiApi
 * - apiKey: String (obtenida desde BuildConfig.GEMINI_API_KEY)
 * - modelName: String (obtenida desde Constants.MODEL_NAME)
 */
@Suppress("JSON_FORMAT_REDUNDANT")
class GeminiAiRepository(
    private val api: GeminiApi,
    private val apiKey: String = com.taller.gemini.BuildConfig.GEMINI_API_KEY,
    private val modelName: String = com.taller.gemini.util.Constants.MODEL_NAME
) : AiRepository {


    /**
     * =========================================================================
     * TODO(PASO 5): Implementar consulta simple de texto (askText)
     * =========================================================================
     *
     * PISTAS:
     * 1. Construye un GeminiRequest con un GeminiContent y un GeminiPart que contenga el prompt.
     * 2. Ejecuta en un bloque try-catch:
     *      val response = api.generateContent(modelName, apiKey, request)
     *      val text = response.firstText() ?: return Result.failure(...)
     *      Result.success(text)
     * 3. Retorna Result.failure(exception) ante fallos de red o parsing.
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
     * =========================================================================
     * TODO(PASO 5): Implementar consulta multimodal con imagen (askAboutImage)
     * =========================================================================
     *
     * PISTAS:
     * 1. Convierte el [image] Bitmap a ByteArray en formato JPEG comprimido (ej: calidad 80-85%).
     *    val stream = ByteArrayOutputStream()
     *    image.compress(Bitmap.CompressFormat.JPEG, 85, stream)
     *    val base64String = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
     *
     * 2. Agrega dos GeminiPart a la lista de partes:
     *    - Una parte de texto con el prompt del usuario.
     *    - Una parte inlineData con mimeType = "image/jpeg" y data = base64String.
     *
     * 3. Llama a la API con ese request y retorna el texto resultante envuelto en Result.success.
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
     * =========================================================================
     * TODO(PASO 5): Implementar salida estructurada en JSON (classifyIncident)
     * =========================================================================
     *
     * PISTAS:
     * 1. Diseña un prompt estructurado pidiendo analizar la incidencia urbana y responder
     *    ÚNICAMENTE con un JSON con los campos: "categoria", "urgencia", "resumen".
     *
     *    Ejemplo de System Prompt / Prompt:
     *    "Eres un asistente municipal de atención ciudadana. Analiza este reporte:
     *     '$description'
     *     Devuelve un JSON con:
     *     - categoria (ej: Malla vial, Alumbrado, Fuga de agua, Basura, Seguridad)
     *     - urgencia (Baja, Media, Alta, Crítica)
     *     - resumen (breve síntesis de 1 frase)
     *     Responde en formato JSON válido."
     *
     * 2. Opcional (avanzado): Activa generationConfig = GeminiGenerationConfig(responseMimeType = "application/json")
     *    para forzar al modelo a devolver JSON puro sin delimitadores markdown.
     *
     * 3. Limpia posibles delimitadores markdown (```json ... ```) si es necesario y deserializa:
     *    val incident = Json.decodeFromString<IncidentInfo>(cleanJsonText)
     *    Result.success(incident)
     */
    override suspend fun classifyIncident(description: String): Result<IncidentInfo> {
        return try {
            val prompt = """
            Eres un sistema clasificador municipal de reportes urbanos.
            Analiza el siguiente reporte del ciudadano:
            "$description"
            
            Devuelve exclusivamente un JSON con estos 3 campos:
            - "categoria": Ejemplos: "Malla Vial", "Agua y Alcantarillado", "Alumbrado Público", "Gestión Ambiental", "Seguridad"
            - "urgencia": Uno de: "Baja", "Media", "Alta", "Crítica"
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

            val incidentInfo = kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            }.decodeFromString<IncidentInfo>(cleanJson)

            Result.success(incidentInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}