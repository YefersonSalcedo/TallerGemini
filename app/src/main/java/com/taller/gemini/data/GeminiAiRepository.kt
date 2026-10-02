package com.taller.gemini.data

import android.graphics.Bitmap
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
    // Constructor
) : AiRepository {


    override suspend fun askText(prompt: String): Result<String> {
        TODO("PASO 5: implementar askText")
    }

    override suspend fun askAboutImage(image: Bitmap, prompt: String): Result<String> {
        TODO("PASO 5: implementar askAboutImage")
    }

    override suspend fun classifyIncident(description: String): Result<IncidentInfo> {
        TODO("PASO 5: implementar classifyIncident")
    }
}