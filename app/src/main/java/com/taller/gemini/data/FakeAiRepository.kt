package com.taller.gemini.data

import android.graphics.Bitmap
import com.taller.gemini.model.IncidentInfo
import kotlinx.coroutines.delay

/**
 * Implementación simulada (Mock) de [AiRepository].
 * Proporciona respuestas predefinidas con 1 segundo de retraso para que la app
 * compile y sea 100% interactiva antes de conectar la API real de Gemini.
 */
class FakeAiRepository : AiRepository {

    override suspend fun askText(prompt: String): Result<String> {
        delay(1000) // Simula la latencia de red

        if (prompt.isBlank()) {
            return Result.failure(IllegalArgumentException("El prompt no puede estar vacío"))
        }

        val respuestaSimulada = """
            [Respuesta de FakeAiRepository]
            Has consultado: "$prompt"
            
            Gemini es una familia de modelos de lenguaje multimodal desarrollada por Google AI. 
            Esta respuesta proviene de la implementación simulada. Una vez completado el taller, 
            esta respuesta provendrá directamente de los servidores de Google Gemini en tiempo real.
        """.trimIndent()

        return Result.success(respuestaSimulada)
    }

    override suspend fun askAboutImage(image: Bitmap, prompt: String): Result<String> {
        delay(1000)

        val dimensiones = "${image.width}x${image.height}"
        val pregunta = if (prompt.isNotBlank()) prompt else "¿Qué ves en esta imagen?"

        val respuestaSimulada = """
            [Análisis Multimodal de FakeAiRepository]
            Pregunta: "$pregunta"
            Dimensiones de la imagen recibida: $dimensiones px.
            
            Simulación: La imagen parece mostrar una situación en la vía pública o un entorno urbano. 
            Se detectan elementos de infraestructura y contraste visual. Cuando completes el PASO 5, 
            Gemini analizará los píxeles reales en Base64.
        """.trimIndent()

        return Result.success(respuestaSimulada)
    }

    override suspend fun classifyIncident(description: String): Result<IncidentInfo> {
        delay(1000)

        if (description.isBlank()) {
            return Result.failure(IllegalArgumentException("La descripción del incidente está vacía"))
        }

        // Lógica simple simulada basada en palabras clave
        val descLower = description.lowercase()
        val (categoria, urgencia) = when {
            descLower.contains("fuga") || descLower.contains("agua") || descLower.contains("inundaci") -> {
                "Agua y Alcantarillado" to "Alta"
            }
            descLower.contains("bache") || descLower.contains("calle") || descLower.contains("asfalto") -> {
                "Malla Vial" to "Media"
            }
            descLower.contains("luz") || descLower.contains("cable") || descLower.contains("poste") || descLower.contains("semáforo") -> {
                "Alumbrado y Electricidad" to "Crítica"
            }
            descLower.contains("basura") || descLower.contains("limpieza") -> {
                "Gestión Ambiental" to "Baja"
            }
            else -> {
                "Mantenimiento General" to "Media"
            }
        }

        val fakeInfo = IncidentInfo(
            categoria = categoria,
            urgencia = urgencia,
            resumen = "Incidencia detectada: ${description.take(60)}... [Generado por FakeAiRepository]"
        )

        return Result.success(fakeInfo)
    }
}