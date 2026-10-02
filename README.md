# Taller Práctico: Integración de APIs de IA con Google Gemini en Android

¡Bienvenido al taller de **Integración de APIs de Inteligencia Artificial con Gemini en Android**!

En este taller aprenderás a conectar una aplicación moderna en **Kotlin con Jetpack Compose y Material 3** con los servicios de IA de última generación de Google Gemini, cubriendo:

1. **Generación de texto simple (Zero-Shot / Prompting básico)**.
2. **Entrada multimodal con visión artificial (Photo Picker + análisis de imágenes en Base64)**.
3. **Salidas estructuradas en formato JSON (Extracción de entidades y clasificación urbana)**.

Para consumir la API REST usamos **Retrofit + OkHttp**, y para convertir el JSON a objetos Kotlin usamos **Moshi** (con generación de código mediante **KSP**).

---

## Arquitectura de la Aplicación

El proyecto sigue las directrices oficiales de arquitectura de Android (Clean Architecture simplificada):

```
app/src/main/java/com/taller/gemini/
├── MainActivity.kt                # Única Activity
├── di/
│   └── AppModule.kt               # Contenedor de dependencias (Retrofit + Moshi)
├── util/
│   └── Constants.kt               # URL base y selección del modelo Gemini
├── model/
│   ├── IncidentInfo.kt            # Modelo de dominio para reporte urbano
│   └── GeminiModels.kt            # DTOs de petición y respuesta para la API REST
├── data/
│   ├── AiRepository.kt            # Interfaz / Contrato del repositorio de IA
│   ├── GeminiApi.kt               # Interfaz Retrofit para Google Gemini
│   └── GeminiAiRepository.kt      # Implementación real con llamadas HTTP a Gemini
├── ui/
│   ├── UiState.kt      
│   ├── components/           
│   ├── navigation/
│   ├── screens/
│   └── theme/                     
└── viewmodel/
    ├── TextViewModel.kt           
    ├── ImageViewModel.kt          
    └── JsonViewModel.kt          
```

---

## Guía Paso a Paso (TODOs del 1 al 8)

### PASO 1: Obtener API Key y Configurar `local.properties`

1. Ingresa a [Google AI Studio](https://aistudio.google.com/app/apikey) con tu cuenta de Google.
2. Haz clic en **"Get API key"** y crea una nueva clave gratuita.
3. En la raíz de tu proyecto, en el archivo `local.properties`, copia y cambia lo siguiente:

```
GEMINI_API_KEY=TuClaveRealDeGeminiAqui
```

4. Nota: `local.properties` ya está incluido en `.gitignore` para no exponer tu clave en GitHub.
5. No compartas tu clave ni la muestres en capturas de pantalla o en el proyector.

---

### PASO 2: Descomentar Plugin y Dependencias en Gradle

El archivo `gradle/libs.versions.toml` ya incluye las versiones de **Moshi** y **KSP**. Estas son las entradas que usa el proyecto, para que sepas de dónde salen los alias:

```toml
[versions]
moshi = "1.15.2"
ksp = "2.0.21-1.0.28" 

[libraries]
moshi = { group = "com.squareup.moshi", name = "moshi", version.ref = "moshi" }
moshi-kotlin-codegen = { group = "com.squareup.moshi", name = "moshi-kotlin-codegen", version.ref = "moshi" }
retrofit-converter-moshi = { group = "com.squareup.retrofit2", name = "converter-moshi", version.ref = "retrofit" }

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

Ahora abre los archivos `build.gradle.kts`:

1. En el `build.gradle.kts` de la **raíz** del proyecto, descomenta:

```kotlin
alias(libs.plugins.ksp) apply false
```

2. En `app/build.gradle.kts`, bloque `plugins`, descomenta:

```kotlin
alias(libs.plugins.ksp)
```

3. En el bloque `dependencies` de `app/build.gradle.kts`, descomenta las 5 líneas de Retrofit, Moshi y OkHttp:

```kotlin
implementation(libs.retrofit)
implementation(libs.retrofit.converter.moshi)
implementation(libs.moshi)
implementation(libs.okhttp.logging.interceptor)
ksp(libs.moshi.kotlin.codegen)
```

4. Haz clic en **"Sync Now"** en Android Studio.

> **¿Para qué sirve cada una?** `retrofit` hace las llamadas HTTP, `converter-moshi` convierte el JSON en objetos Kotlin y viceversa, `moshi` es la librería de JSON, y `moshi-kotlin-codegen` (con **KSP**) genera en tiempo de compilación los adaptadores de tus clases, sin usar reflexión.

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

Descomenta y define las clases. Cada una lleva `@JsonClass(generateAdapter = true)` para que KSP genere su adaptador, y `@Json(name = "...")` donde el nombre del JSON (snake_case) difiere del nombre en Kotlin:

```kotlin
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
```

Descomenta también `@JsonClass` en `app/src/main/java/com/taller/gemini/model/IncidentInfo.kt`:

```kotlin
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IncidentInfo(
    val categoria: String,
    val urgencia: String,
    val resumen: String
)
```

> Moshi omite por defecto los campos `null` al enviar la petición e ignora las claves desconocidas al recibir la respuesta, por lo que no necesitas configuración extra.

#### 4.2 En `app/src/main/java/com/taller/gemini/data/GeminiApi.kt`:

Declara el método POST para el endpoint de Gemini:

```kotlin
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
import com.squareup.moshi.Moshi
import com.taller.gemini.model.GeminiContent
import com.taller.gemini.model.GeminiGenerationConfig
import com.taller.gemini.model.GeminiInlineData
import com.taller.gemini.model.GeminiPart
import com.taller.gemini.model.GeminiRequest
import com.taller.gemini.model.IncidentInfo
```

1. Añade los parámetros en el constructor de la clase. Fíjate en `moshi`, que se usará para convertir el JSON de la clasificación en un objeto `IncidentInfo`:

```kotlin
class GeminiAiRepository(
    private val api: GeminiApi,
    private val apiKey: String = com.taller.gemini.BuildConfig.GEMINI_API_KEY,
    private val modelName: String = com.taller.gemini.util.Constants.MODEL_NAME,
    private val moshi: Moshi = Moshi.Builder().build()
) : AiRepository
```

2. **Implementa `askText`** (envía un prompt de texto y devuelve la respuesta):

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

3. **Implementa `askAboutImage`** (comprime la imagen a JPEG, la codifica en Base64 y la envía junto al prompt):

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

4. **Implementa `classifyIncident`** (pide la respuesta en JSON y la convierte a `IncidentInfo` con Moshi):

```kotlin
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
```

> **Reto extra:** las reglas del clasificador ("Eres un sistema clasificador...") son en realidad un *system prompt*. Busca en la documentación de la API REST de Gemini el campo `systemInstruction` del request, muévelas allí y deja en el prompt solo la descripción del ciudadano. ¿Cambia la calidad de la respuesta?

---

### PASO 6: Conectar `GeminiAiRepository` en `AppModule.kt`

Abre `app/src/main/java/com/taller/gemini/di/AppModule.kt`:
Instancia Moshi y Retrofit con OkHttp, y conecta el repositorio:

```kotlin
import com.squareup.moshi.Moshi
import com.taller.gemini.data.AiRepository
import com.taller.gemini.data.GeminiAiRepository
import com.taller.gemini.data.GeminiApi
import com.taller.gemini.util.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object AppModule {

    // Moshi con los adaptadores generados por KSP.
    // Ignora por defecto las claves desconocidas del JSON de respuesta.
    private val moshi = Moshi.Builder().build()

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
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val geminiApi = retrofit.create(GeminiApi::class.java)

    val aiRepository: AiRepository by lazy {
        GeminiAiRepository(geminiApi, moshi = moshi)
    }
}
```

---

### PASO 7: Configurar `MODEL_NAME` en `Constants.kt`

Abre `app/src/main/java/com/taller/gemini/util/Constants.kt`:
Reemplaza `"MODELO_AQUI"` por un modelo disponible en la capa gratuita. Los modelos de Gemini cambian con frecuencia y los antiguos se retiran, así que **confirma el nombre exacto** en [Google AI Studio](https://aistudio.google.com) o en la [lista de modelos](https://ai.google.dev/gemini-api/docs/models). Usa un modelo de la familia *Flash* o *Flash-Lite*, por ejemplo:

```kotlin
object Constants {
    const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"

    // Modelo rápido y multimodal (Flash-Lite). Verifica que siga vigente.
    const val MODEL_NAME: String = "gemini-3.5-flash-lite"
}
```

> Si te da error 404, el modelo no existe o fue retirado: prueba con otro de la lista (por ejemplo `gemini-3.8-flash`).

---

### PASO 8: ¡Compilar, Ejecutar y Probar!

1. Compila con **Build > Rebuild Project** (KSP genera los adaptadores de Moshi en este paso) y presiona **Run 'app'** (`Shift + F10`) en Android Studio.
2. Abre la pestaña **Logcat** y filtra por `OkHttp` para ver el tráfico de red en vivo (JSON enviado y recibido; la API key aparece oculta).
3. Prueba las 3 pestañas de la app:
   - **Texto:** Haz una pregunta técnica y verifica la respuesta generada por Gemini.
   - **Imagen:** Selecciona una fotografía de la galería y pide a Gemini que describa los detalles. **No uses fotos personales ni de otras personas:** en la capa gratuita el contenido puede usarse para mejorar los productos de Google. Usa imágenes de objetos o de internet.
   - **JSON:** Escribe una incidencia ciudadana y observa cómo la tarjeta se pinta automáticamente con la categoría, color de urgencia y resumen.
4. **Prueba de errores:** activa el modo avión en el emulador y repite una consulta. La app debe mostrar un mensaje de error, no cerrarse.

---

## Tabla de Referencia de los `TODO(PASO X)`

| Paso       | Archivo                                                   | Descripción de la Tarea                                                     |
| ---------- |-----------------------------------------------------------|-----------------------------------------------------------------------------|
| **PASO 1** | `local.properties`                                        | En `local.properties` y agregar `GEMINI_API_KEY=...`                        |
| **PASO 2** | `build.gradle.kts` (raíz) y `app/build.gradle.kts`        | Descomentar plugin `ksp` y las librerías de Retrofit, Moshi y OkHttp        |
| **PASO 3** | `app/src/main/AndroidManifest.xml`                        | Descomentar `<uses-permission android:name="android.permission.INTERNET" />` |
| **PASO 4** | `app/src/main/java/com/taller/gemini/model/GeminiModels.kt` | Declarar DTOs `GeminiRequest`, `Content`, `Part`, `GeminiResponse` con `@JsonClass` |
| **PASO 4** | `app/src/main/java/com/taller/gemini/model/IncidentInfo.kt` | Descomentar `@JsonClass(generateAdapter = true)`                               |
| **PASO 4** | `app/src/main/java/com/taller/gemini/data/GeminiApi.kt`   | Declarar interfaz Retrofit con `@POST("v1beta/models/{model}:generateContent")` |
| **PASO 5** | `app/src/main/java/com/taller/gemini/data/GeminiAiRepository.kt` | Implementar `askText()`, `askAboutImage()` y `classifyIncident()`               |
| **PASO 6** | `app/src/main/java/com/taller/gemini/di/AppModule.kt`     | Instanciar `GeminiAiRepository(geminiApi, moshi = moshi)` con Retrofit y Moshi    |
| **PASO 7** | `app/src/main/java/com/taller/gemini/util/Constants.kt`   | Reemplazar `"MODELO_AQUI"` por un modelo vigente (ej. `"gemini-3.5-flash-lite"`)  |
| **PASO 8** | Logcat                                             | Probar los 3 casos de uso en emulador con OkHttp Logging activado                   |

---

## Consejos de Depuración para el Taller

- **Error 400 (API_KEY_INVALID):** Verifica que no haya espacios en blanco en tu `GEMINI_API_KEY` dentro de `local.properties` y haz un *Rebuild Project*.
- **Error 404 (Model Not Found):** El modelo no existe o fue retirado. Verifica el nombre exacto en AI Studio o en la [lista de modelos](https://ai.google.dev/gemini-api/docs/models) y ponlo en `Constants.MODEL_NAME` sin prefijos ni barras (ej. `gemini-3.5-flash-lite`).
- **Error 429 (Too Many Requests):** Superaste el límite de la capa gratuita (peticiones por minuto, por día o tokens por minuto). Espera un minuto y reintenta; evita enviar muchas consultas seguidas. Los límites cambian, consúltalos en la documentación oficial.
- **SecurityException:** Verifica que hayas descomentado el permiso `INTERNET` en `AndroidManifest.xml` (Paso 3).
- **`JsonDataException` / `JsonEncodingException`:** Moshi no pudo convertir el JSON a `IncidentInfo`. Asegúrate de limpiar las comillas invertidas de markdown (` ```json `) antes de llamar a `fromJson` y revisa en Logcat que los nombres de los campos coincidan (`categoria`, `urgencia`, `resumen`).
- **"Cannot serialize Kotlin type ... Reflective serialization of Kotlin classes without using kotlin-reflect...":** Moshi no encontró el adaptador generado. Verifica que el plugin `ksp` y `ksp(libs.moshi.kotlin.codegen)` estén descomentados (Paso 2), que cada DTO tenga `@JsonClass(generateAdapter = true)` y haz un *Rebuild Project*.
- **Privacidad:** en la capa gratuita lo que envíes puede usarse para mejorar los productos de Google. No envíes datos personales ni imágenes de personas.