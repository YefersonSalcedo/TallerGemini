# Taller Práctico (1 hora): Integración de APIs de IA con Google Gemini en Android

¡Bienvenido al taller de **Integración de APIs de Inteligencia Artificial con Gemini en Android**!

En este taller de 60 minutos aprenderás a conectar una aplicación moderna en **Kotlin con Jetpack Compose y Material 3** con los servicios de IA de última generación de Google Gemini, cubriendo:
1. **Generación de texto simple (Zero-Shot / Prompting básico)**.
2. **Entrada multimodal con visión artificial (Photo Picker + análisis de imágenes en Base64)**.
3. **Salidas estructuradas en formato JSON (Extracción de entidades y clasificación urbana)**.

---

## Arquitectura de la Aplicación

El proyecto sigue las directrices oficiales de arquitectura de Android (Clean Architecture simplificada):

```
app/src/main/java/com/taller/gemini/
├── MainActivity.kt                # Única Activity 
├── di/
│   └── AppModule.kt               # Contenedor de dependencias 
├── util/
│   └── Constants.kt               # URL base y selección del modelo Gemini
├── model/
│   ├── IncidentInfo.kt            # Modelo de dominio para reporte urbano
│   └── GeminiModels.kt            # DTOs de petición y respuesta para la API REST
├── data/
│   ├── AiRepository.kt            # Interfaz / Contrato del repositorio de IA
│   ├── FakeAiRepository.kt        # Mock para compilar y probar la UI de inmediato
│   ├── GeminiApi.kt               # Interfaz Retrofit para Google Gemini
│   └── GeminiAiRepository.kt      # Implementación real con llamadas HTTP a Gemini
├── ui/
│   ├── UiState.kt                 # Sealed interface: Idle, Loading, Success, Error
│   ├── navigation/
│   │   ├── NavRoutes.kt           # Definición de rutas y destinos
│   │   └── AppNavigation.kt       # Scaffold, BottomBar y NavHost de Compose
│   ├── screens/
│   │   ├── TextScreen.kt          # Pantalla 1: Consulta de texto
│   │   ├── ImageScreen.kt         # Pantalla 2: Consulta multimodal con Photo Picker
│   │   └── JsonScreen.kt          # Pantalla 3: Clasificación estructurada en JSON
│   └── theme/                     # Sistema de diseño Material 3
└── viewmodel/
    ├── TextViewModel.kt           # Lógica y StateFlow para Pantalla 1
    ├── ImageViewModel.kt          # Lógica y StateFlow para Pantalla 2
    └── JsonViewModel.kt           # Lógica y StateFlow para Pantalla 3
```

---

## ⏱️ Cronograma sugerido del Taller (60 minutos)

| Tiempo | Bloque | Actividad |
|---|---|---|
| **00 - 10 min** | Introducción | Explicación de la arquitectura, clonar repo y correr con `FakeAiRepository`. |
| **10 - 20 min** | Configuración | Obtener API Key de AI Studio, `local.properties`, dependencias Gradle y permisos (Pasos 1, 2 y 3). |
| **20 - 35 min** | Contrato de Red | Modelos DTO de Gemini y definición de la interfaz Retrofit (Paso 4). |
| **35 - 50 min** | Implementación | Lógica del repositorio: Texto, Imagen y JSON estructurado (Pasos 5 y 6). |
| **50 - 55 min** | Configuración Final | Ajuste de `MODEL_NAME` (Paso 7) y compilación final. |
| **55 - 60 min** | Pruebas y Cierre | Demo en emulador/dispositivo real, preguntas y conclusiones (Paso 8). |



## Guía Paso a Paso (TODOs del 1 al 8)

### PASO 1: Obtener API Key y Configurar `local.properties`
1. Ingresa a [Google AI Studio](https://aistudio.google.com/app/apikey) con tu cuenta de Google.
2. Haz clic en **"Get API key"** y crea una nueva clave gratuita.
3. En la raíz de tu proyecto, copia el archivo `local.properties.example` y renómbralo a `local.properties`:
   ```properties
   GEMINI_API_KEY=AIzaSyTuClaveRealDeGeminiAqui
   ```
4. Nota: `local.properties` ya está incluido en `.gitignore` para no exponer tu clave en GitHub.
5. No compartas tu clave ni la muestres en capturas de pantalla o en el proyector.

---

### PASO 2: Descomentar Dependencias en Gradle
Abre el archivo `app/build.gradle.kts`:
1. En el bloque de plugins superiores, descomenta:
   ```kotlin
   alias(libs.plugins.kotlinx.serialization)
   ```
   *(Y si es necesario en el `build.gradle.kts` raíz, el plugin de serialization).*
2. En el bloque `dependencies`, descomenta las 4 librerías de Retrofit y Serialization:
   ```kotlin
   implementation(libs.kotlinx.serialization.json)
   implementation(libs.retrofit)
   implementation(libs.retrofit.converter.kotlinx.serialization)
   implementation(libs.okhttp.logging.interceptor)
   ```
3. Haz clic en **"Sync Now"** en Android Studio.

---

### PASO 3: Descomentar el Permiso de Internet en el Manifest
Abre `app/src/main/AndroidManifest.xml`:
Descomenta la etiqueta del permiso:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

### PASO 4: Definir los Modelos DTO y la Interfaz Retrofit
Gemini utiliza una estructura jerárquica: `contents` -> `parts` -> (`text` o `inline_data`).

#### 4.1 En `app/src/main/java/com/taller/gemini/model/GeminiModels.kt`:
Descomenta y define las clases serializables:
```kotlin
package com.taller.gemini.model

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
```

Descomenta también `@Serializable` en `app/src/main/java/com/taller/gemini/model/IncidentInfo.kt`:
```kotlin
@kotlinx.serialization.Serializable
data class IncidentInfo(
    val categoria: String,
    val urgencia: String,
    val resumen: String
)
```

#### 4.2 En `app/src/main/java/com/taller/gemini/data/GeminiApi.kt`:
Declara el método POST para el endpoint de Gemini:
```kotlin
package com.taller.gemini.data

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
```

---

### PASO 5: Implementar `GeminiAiRepository.kt`
En `app/src/main/java/com/taller/gemini/data/GeminiAiRepository.kt`:

Agrega estos imports al inicio del archivo (o usa `Alt + Enter` sobre cada error en rojo):
```kotlin
import android.graphics.Bitmap
import com.taller.gemini.model.GeminiContent
import com.taller.gemini.model.GeminiGenerationConfig
import com.taller.gemini.model.GeminiInlineData
import com.taller.gemini.model.GeminiPart
import com.taller.gemini.model.GeminiRequest
import com.taller.gemini.model.IncidentInfo
```

1. Añade los parámetros en el constructor de la clase:
   ```kotlin
   class GeminiAiRepository(
       private val api: GeminiApi,
       private val apiKey: String = com.taller.gemini.BuildConfig.GEMINI_API_KEY,
       private val modelName: String = com.taller.gemini.util.Constants.MODEL_NAME
   ) : AiRepository
   ```

2. **Implementa `askText`**:
   ```kotlin
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
   ```

3. **Implementa `askAboutImage`**:
   ```kotlin
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
   ```

4. **Implementa `classifyIncident`**:
   ```kotlin
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
   ```

> **Reto extra:** las reglas del clasificador ("Eres un sistema clasificador...") son en realidad un *system prompt*. Busca en la documentación de la API REST de Gemini el campo `systemInstruction` del request, muévelas allí y deja en el prompt solo la descripción del ciudadano. ¿Cambia la calidad de la respuesta?

---

### PASO 6: Cambiar `FakeAiRepository` por `GeminiAiRepository` en `AppModule.kt`
Abre `app/src/main/java/com/taller/gemini/di/AppModule.kt`:
Instancia Retrofit con OkHttp y conecta el repositorio real:

```kotlin
package com.taller.gemini.di

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.taller.gemini.data.AiRepository
import com.taller.gemini.data.GeminiAiRepository
import com.taller.gemini.data.GeminiApi
import com.taller.gemini.util.Constants
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object AppModule {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // BODY imprime el JSON enviado y recibido (útil para aprender).
        level = HttpLoggingInterceptor.Level.BODY
        // Oculta la API key en Logcat (va en este header)
        redactHeader("x-goog-api-key")
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.GEMINI_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val geminiApi = retrofit.create(GeminiApi::class.java)

    val aiRepository: AiRepository by lazy {
        GeminiAiRepository(geminiApi)
    }
}
```

---

### PASO 7: Configurar `MODEL_NAME` en `Constants.kt`
Abre `app/src/main/java/com/taller/gemini/util/Constants.kt`:
Reemplaza `"MODELO_AQUI"` por un modelo disponible en la capa gratuita. Los modelos de Gemini cambian con frecuencia y los antiguos se retiran, así que **confirma el nombre exacto** en [Google AI Studio](https://aistudio.google.com) o en la [lista de modelos](https://ai.google.dev/gemini-api/docs/models). Usa un modelo de la familia *Flash* o *Flash-Lite*, por ejemplo:

```kotlin
package com.taller.gemini.util

object Constants {
    const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"

    // Modelo rápido y multimodal (Flash-Lite). Verifica que siga vigente.
    const val MODEL_NAME: String = "gemini-3.5-flash-lite"
}
```

> Si te da error 404, el modelo no existe o fue retirado: prueba con otro de la lista (por ejemplo `gemini-3.8-flash`).

---

### PASO 8: ¡Compilar, Ejecutar y Probar!
1. Presiona **Run 'app'** (`Shift + F10`) en Android Studio.
2. Abre la pestaña **Logcat** y filtra por `OkHttp` para ver el tráfico de red en vivo (JSON enviado y recibido; la API key aparece oculta).
3. Prueba las 3 pestañas de la app:
   - **Texto:** Haz una pregunta técnica y verifica la respuesta generada por Gemini.
   - **Imagen:** Selecciona una fotografía de la galería y pide a Gemini que describa los detalles. **No uses fotos personales ni de otras personas:** en la capa gratuita el contenido puede usarse para mejorar los productos de Google. Usa imágenes de objetos o de internet.
   - **JSON:** Escribe una incidencia ciudadana y observa cómo la tarjeta se pinta automáticamente con la categoría, color de urgencia y resumen.
4. **Prueba de errores:** activa el modo avión en el emulador y repite una consulta. La app debe mostrar un mensaje de error, no cerrarse.

---

## Tabla de Referencia de los `TODO(PASO X)`

| Paso | Archivo | Líneas aprox. | Descripción de la Tarea |
|---|---|---|---|
| **PASO 1** | `local.properties.example` | 1 - 6 | Copiar como `local.properties` y agregar `GEMINI_API_KEY=...` |
| **PASO 2** | `app/build.gradle.kts` | 6 - 8, 70 - 75 | Descomentar plugin `kotlinx.serialization` y librerías Retrofit |
| **PASO 3** | `app/src/main/AndroidManifest.xml` | 6 | Descomentar `<uses-permission android:name="android.permission.INTERNET" />` |
| **PASO 4** | `app/src/main/java/com/taller/gemini/model/GeminiModels.kt` | 3 - 60 | Declarar DTOs `GeminiRequest`, `Content`, `Part`, `GeminiResponse` |
| **PASO 4** | `app/src/main/java/com/taller/gemini/data/GeminiApi.kt` | 3 - 35 | Declarar interfaz Retrofit con `@POST("v1beta/models/{model}:generateContent")` |
| **PASO 5** | `app/src/main/java/com/taller/gemini/data/GeminiAiRepository.kt` | 32, 54, 88 | Implementar `askText()`, `askAboutImage()` y `classifyIncident()` |
| **PASO 6** | `app/src/main/java/com/taller/gemini/di/AppModule.kt` | 13 - 52 | Cambiar `FakeAiRepository()` por `GeminiAiRepository(geminiApi)` con Retrofit |
| **PASO 7** | `app/src/main/java/com/taller/gemini/util/Constants.kt` | 9 | Reemplazar `"MODELO_AQUI"` por un modelo vigente (ej. `"gemini-2.5-flash-lite"`) |
| **PASO 8** | `TALLER.md` / Logcat | Sección 8 | Probar los 3 casos de uso en emulador con OkHttp Logging activado |

---

## Consejos de Depuración para el Taller
- **Error 400 (API_KEY_INVALID):** Verifica que no haya espacios en blanco en tu `GEMINI_API_KEY` dentro de `local.properties` y haz un *Rebuild Project*.
- **Error 404 (Model Not Found):** El modelo no existe o fue retirado. Verifica el nombre exacto en AI Studio o en la [lista de modelos](https://ai.google.dev/gemini-api/docs/models) y ponlo en `Constants.MODEL_NAME` sin prefijos ni barras (ej. `gemini-3.5-flash-lite`).
- **Error 429 (Too Many Requests):** Superaste el límite de la capa gratuita (peticiones por minuto, por día o tokens por minuto). Espera un minuto y reintenta; evita enviar muchas consultas seguidas. Los límites cambian, consúltalos en la documentación oficial.
- **SecurityException:** Verifica que hayas descomentado el permiso `INTERNET` en `AndroidManifest.xml` (Paso 3).
- **SerializationException:** Asegúrate de limpiar posibles comillas invertidas de markdown (` ```json `) antes de hacer `Json.decodeFromString`.
- **Privacidad:** en la capa gratuita lo que envíes puede usarse para mejorar los productos de Google. No envíes datos personales ni imágenes de personas.
