package com.taller.gemini.data

import android.graphics.Bitmap
import com.taller.gemini.model.IncidentInfo

/**
 * Contrato de repositorio de IA para la aplicación.
 * Define las 3 capacidades clave que se explorarán en el taller.
 */
interface AiRepository {

    /**
     * Envía una consulta de texto plano y devuelve la respuesta del modelo.
     */
    suspend fun askText(prompt: String): Result<String>

    /**
     * Envía una imagen junto con una instrucción/pregunta multimodal.
     */
    suspend fun askAboutImage(image: Bitmap, prompt: String): Result<String>

    /**
     * Analiza el reporte de una incidencia urbana y devuelve una estructura tipada [IncidentInfo].
     */
    suspend fun classifyIncident(description: String): Result<IncidentInfo>
}