# Taller Práctico: Integración de APIs de IA con Google Gemini en Android

## 1. Introducción

En este taller se construye una aplicación Android capaz de comunicarse con la API REST de **Google Gemini**. El proyecto utiliza **Kotlin**, **Jetpack Compose**, **Material 3**, **Retrofit**, **OkHttp** y **Moshi**.

El objetivo no es solamente enviar una pregunta a un modelo de inteligencia artificial, sino aprender cómo se organiza una integración de este tipo: configurar las dependencias, construir una petición HTTP, representar los datos con clases Kotlin, procesar la respuesta y mostrar el resultado en la interfaz.

La aplicación contempla tres casos de uso:

1. **Generación de texto:** enviar una instrucción o pregunta y mostrar la respuesta.
2. **Análisis de imágenes:** seleccionar una imagen y enviarla junto con una pregunta para que Gemini la analice.
3. **Clasificación de incidencias urbanas:** enviar la descripción de un problema y recibir una respuesta estructurada en JSON con categoría, urgencia y resumen.

### Tecnologías utilizadas

| Tecnología | Función dentro del proyecto |
|---|---|
| Kotlin | Lenguaje de programación de la aplicación Android. |
| Jetpack Compose y Material 3 | Construcción de la interfaz gráfica. |
| Retrofit | Declaración y ejecución de las solicitudes HTTP a la API. |
| OkHttp | Cliente HTTP que realiza las conexiones de red y permite configurar tiempos de espera y registros. |
| Moshi | Conversión entre JSON y objetos Kotlin. |
| KSP | Procesamiento de anotaciones durante la compilación; permite generar adaptadores de Moshi. |
| Google Gemini API | Servicio de IA que procesa los textos y las imágenes enviados por la aplicación. |
| `local.properties` | Archivo local de configuración donde se guarda la clave de API durante el desarrollo. |

---

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

### ¿Qué hace cada parte?

- **`MainActivity.kt`:** punto de entrada de la interfaz Android. Normalmente establece el contenido Compose y el tema de la aplicación.
- **`di/AppModule.kt`:** construye y conecta los objetos necesarios para comunicarse con Gemini, como Retrofit, OkHttp, Moshi y el repositorio. En este taller funciona como un contenedor sencillo de dependencias.
- **`util/Constants.kt`:** concentra valores de configuración, como la URL base y el nombre del modelo.
- **`model/GeminiModels.kt`:** define las clases que representan la estructura de las peticiones y respuestas de la API.
- **`model/IncidentInfo.kt`:** representa el resultado de la clasificación de una incidencia.
- **`data/AiRepository.kt`:** contrato que describe las operaciones de IA que necesita la aplicación, sin especificar cómo se ejecutan.
- **`data/GeminiApi.kt`:** declara los endpoints HTTP que Retrofit debe invocar.
- **`data/GeminiAiRepository.kt`:** implementa las operaciones de IA y reúne la lógica para construir peticiones, llamar a Gemini y procesar las respuestas.
- **`ui/`:** contiene los elementos visuales, las pantallas, la navegación, el tema y los estados de la interfaz.
- **`viewmodel/`:** conecta la lógica de la pantalla con la interfaz. Los ViewModels suelen gestionar el estado de carga, los resultados y los errores.

**Flujo general:** la pantalla solicita una operación al ViewModel; este utiliza el repositorio; el repositorio construye la petición y llama a `GeminiApi`; Retrofit y OkHttp realizan la conexión; la respuesta JSON se convierte en objetos Kotlin con Moshi y el resultado vuelve a la interfaz.

---

## Guía de implementación paso a paso

> Antes de comenzar, abre el proyecto en Android Studio y espera a que termine la sincronización inicial de Gradle. Los nombres de archivos y paquetes de esta guía corresponden a la estructura mostrada arriba.

### PASO 1: Obtener API Key y Configurar `local.properties`

1. Ingresa a [Google AI Studio](https://aistudio.google.com/app/apikey) con tu cuenta de Google.
2. Haz clic en **"Get API key"** y crea una nueva clave gratuita.
3. En la raíz de tu proyecto, en el archivo `local.properties`, copia y cambia lo siguiente:

```
GEMINI_API_KEY=TuClaveRealDeGeminiAqui
```

4. Nota: `local.properties` ya está incluido en `.gitignore` para no exponer tu clave en GitHub.
5. No compartas tu clave ni la muestres en capturas de pantalla o en el proyector.


La clave identifica la aplicación ante el servicio de Gemini y permite autenticar las solicitudes. Más adelante, el código la leerá desde `BuildConfig.GEMINI_API_KEY` y la enviará en el encabezado HTTP `x-goog-api-key`.

`local.properties` se utiliza para configuración local y normalmente está excluido de Git. Comprueba que esté en `.gitignore` y que no hayas agregado la clave a un archivo versionado.

**Importante sobre seguridad:** una clave incluida en una aplicación Android compilada puede extraerse del APK; `local.properties` evita subirla accidentalmente al repositorio, pero no convierte la clave en un secreto seguro frente a los usuarios de la aplicación. Para una aplicación distribuida públicamente, considera realizar las llamadas desde un backend propio y aplicar restricciones y límites a la clave. No compartas la clave en capturas, repositorios ni registros.

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
Estos alias presuponen que el catálogo ya declara también las versiones y bibliotecas de Retrofit y OkHttp utilizadas por el proyecto. Si no aparecen, agrégalas de acuerdo con las versiones que tenga configuradas el proyecto; no dupliques alias existentes.

### 2.1. Archivo `build.gradle.kts` de la raíz

Ahora abre los archivos `build.gradle.kts`:

Dentro del bloque `plugins`, activa el alias de KSP:

```kotlin
alias(libs.plugins.ksp) apply false
```

`apply false` declara el plugin en el proyecto raíz, pero no lo aplica directamente a todos los módulos. El módulo que lo necesite lo aplicará de forma explícita.

### 2.2. Archivo `app/build.gradle.kts`

En el bloque `plugins`, activa:

```kotlin
alias(libs.plugins.ksp)
```

Después, dentro de `dependencies`, activa las dependencias correspondientes:

```kotlin
implementation(libs.retrofit)
implementation(libs.retrofit.converter.moshi)
implementation(libs.moshi)
implementation(libs.okhttp.logging.interceptor)
ksp(libs.moshi.kotlin.codegen)
```

### ¿Para qué sirve cada dependencia?

- **Retrofit:** permite declarar un método Kotlin como una operación HTTP, por ejemplo un `POST`, sin construir manualmente toda la solicitud.
- **`converter-moshi`:** conecta Retrofit con Moshi para convertir automáticamente los cuerpos JSON a objetos Kotlin y viceversa.
- **Moshi:** se encarga de serializar y deserializar los datos JSON.
- **OkHttp Logging Interceptor:** permite observar las solicitudes y respuestas durante la depuración.
- **Moshi Kotlin Codegen y KSP:** generan en la compilación los adaptadores que Moshi utiliza para convertir las clases anotadas.

Haz clic en **Sync Now** y espera a que Gradle termine. Si aparecen errores de versiones incompatibles, verifica que la versión de KSP sea compatible con la versión del plugin de Kotlin del proyecto.

---

### PASO 3: Descomentar el Permiso de Internet en el Manifest

Abre `app/src/main/AndroidManifest.xml`:
Descomenta la etiqueta del permiso:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Android utiliza este permiso para autorizar a la aplicación a abrir conexiones de red. Sin él, las llamadas HTTP a Gemini no podrán completarse correctamente.

Este permiso no suele requerir una ventana de autorización del usuario durante la ejecución. Aun así, la aplicación necesita una conexión real y debe manejar errores de red, tiempos de espera y respuestas fallidas.

---

### PASO 4: Definir los Modelos DTO y la Interfaz Retrofit

Gemini recibe peticiones con una estructura JSON jerárquica. En este taller se representa mediante `contents`, que contiene mensajes; cada mensaje tiene `parts`, que puede contener texto o datos de imagen.

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

### Explicación de las clases

- **`GeminiRequest`:** representa el cuerpo que se envía a la API. `contents` contiene el contenido de la petición y `generationConfig` permite indicar opciones para generar la respuesta.
- **`GeminiContent`:** representa un mensaje y agrupa sus partes. `role` es opcional en este modelo.
- **`GeminiPart`:** puede contener texto (`text`) o una imagen (`inlineData`). Los campos son opcionales porque una parte puede usar uno u otro formato.
- **`GeminiInlineData`:** contiene el tipo MIME de la imagen, por ejemplo `image/jpeg`, y sus bytes codificados como texto Base64.
- **`GeminiGenerationConfig`:** permite configurar el formato MIME de la respuesta. En la clasificación se solicita `application/json`.
- **`GeminiResponse`:** representa una respuesta de Gemini. La lista `candidates` puede contener una o más alternativas generadas.
- **`GeminiCandidate`:** representa una alternativa y su contenido.
- **`firstText()`:** intenta recuperar el texto de la primera alternativa y la primera parte. Devuelve `null` si alguna sección no está presente.

La anotación `@JsonClass(generateAdapter = true)` indica a Moshi que debe generar un adaptador para la clase. `@Json(name = "...")` permite asociar una propiedad Kotlin con el nombre exacto de una clave JSON. Por ejemplo, `inlineData` se envía como `inline_data`.

> La estructura de nombres de la API debe coincidir exactamente con el JSON que espera Gemini. Si una propiedad Kotlin utiliza camelCase y la API utiliza snake_case, declara explícitamente el nombre con `@Json`.

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

### ¿Qué hace esta interfaz?

- `@POST(...)` indica que Retrofit debe enviar una solicitud HTTP POST a la ruta de generación de contenido.
- `{model}` es un segmento variable de la ruta. `@Path("model")` lo reemplaza por el nombre del modelo configurado.
- `@Header("x-goog-api-key")` agrega la clave de API a la solicitud.
- `@Body request` indica que el objeto `GeminiRequest` debe convertirse en JSON y enviarse en el cuerpo HTTP.
- `suspend` permite llamar al método desde una corrutina, sin bloquear el hilo principal mientras espera la respuesta.
- El resultado se convierte en un objeto `GeminiResponse` mediante el convertidor Moshi configurado en Retrofit.

Esta interfaz declara la operación, pero no construye por sí sola el cliente Retrofit. 

---

## Paso 5. Implementar `GeminiAiRepository.kt`

El repositorio concentra las operaciones de IA que utilizará la aplicación. Su responsabilidad es construir la petición adecuada para cada caso, invocar la API y transformar la respuesta en un resultado que el resto de la aplicación pueda manejar.

Ubicación: `app/src/main/java/com/taller/gemini/data/GeminiAiRepository.kt`.

### 5.1. Imports y constructor

Los imports utilizados en los fragmentos de este paso incluyen:

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

La clase recibe los objetos necesarios para realizar las operaciones:

```kotlin
class GeminiAiRepository(
    private val api: GeminiApi,
    private val apiKey: String = com.taller.gemini.BuildConfig.GEMINI_API_KEY,
    private val modelName: String = com.taller.gemini.util.Constants.MODEL_NAME,
    private val moshi: Moshi = Moshi.Builder().build()
) : AiRepository
```

- `api`: interfaz Retrofit que realiza la llamada HTTP.
- `apiKey`: clave utilizada para autenticar la solicitud. Se obtiene de `BuildConfig`.
- `modelName`: nombre del modelo que se invocará.
- `moshi`: conversor utilizado para transformar el JSON de clasificación en `IncidentInfo`.
- `: AiRepository`: indica que esta clase implementa el contrato del repositorio. Los nombres y las firmas de los métodos deben coincidir con los declarados en esa interfaz.

El código anterior presupone que `BuildConfig.GEMINI_API_KEY` ya está configurado en Gradle y que la interfaz `AiRepository` declara las tres operaciones. Si el proyecto todavía no define esas piezas, deben configurarse antes de compilar.

### 5.2. Operación `askText`: generar texto

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

**Funcionamiento paso a paso:**

1. Recibe el texto que escribió el usuario en `prompt`.
2. Crea un `GeminiRequest` con un mensaje y una parte de texto.
3. Ejecuta `api.generateContent(...)` con el modelo, la clave y la petición.
4. Llama a `firstText()` para obtener el texto generado.
5. Si hay texto, devuelve `Result.success(text)`. Si no hay contenido, devuelve un fallo descriptivo.
6. Si ocurre una excepción durante la llamada o el procesamiento, la captura y la entrega dentro de `Result.failure(e)`.

`Result<String>` permite comunicar un éxito o un error sin obligar a que la función devuelva siempre una cadena válida. El ViewModel puede examinar el resultado y actualizar la interfaz.

### 5.3. Operación `askAboutImage`: analizar una imagen

```kotlin
override suspend fun askAboutImage(
    image: Bitmap,
    prompt: String
): Result<String> {
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

**Funcionamiento paso a paso:**

1. Recibe un `Bitmap` y un prompt que explica qué debe analizar Gemini.
2. Crea un `ByteArrayOutputStream`, que sirve para almacenar temporalmente los bytes de la imagen comprimida.
3. Convierte el bitmap a JPEG con calidad `85`. La calidad afecta el tamaño y el detalle de la imagen; no garantiza por sí sola que el archivo sea pequeño.
4. Convierte los bytes JPEG a Base64. Base64 permite representar los bytes binarios como texto dentro del JSON.
5. Construye dos partes para el mensaje: una con la pregunta y otra con los datos de la imagen.
6. Envía el cuerpo a Gemini y extrae el texto de respuesta.
7. Devuelve el texto como éxito o una excepción como fallo.

**Aspectos que conviene tener presentes:**

- El valor `mimeType = "image/jpeg"` debe corresponder con el formato real de los bytes enviados.
- Base64 aumenta el tamaño de los datos enviados. Para imágenes grandes, puede ser necesario reducir sus dimensiones antes de codificarlas.
- En este ejemplo se usa `Bitmap`; la pantalla y el selector de imágenes deben convertir la imagen seleccionada a ese tipo.
- No envíes imágenes personales o información sensible durante las pruebas. Revisa las condiciones de tratamiento de datos de Google y las políticas aplicables a la cuenta y al servicio que utilices.

### 5.4. Operación `classifyIncident`: obtener y convertir JSON

```kotlin
override suspend fun classifyIncident(
    description: String
): Result<IncidentInfo> {
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
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json"
            )
        )

        val response = api.generateContent(modelName, apiKey, request)
        val rawJson = response.firstText()
            ?: return Result.failure(Exception("JSON no recibido"))

        // Retira posibles delimitadores Markdown antes de analizar el JSON.
        val cleanJson = rawJson
            .replace("```json", "")
            .replace("```", "")
            .trim()

        val incidentInfo = moshi
            .adapter(IncidentInfo::class.java)
            .fromJson(cleanJson)
            ?: return Result.failure(Exception("JSON vacío o inválido"))

        Result.success(incidentInfo)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

**Funcionamiento paso a paso:**

1. Construye un prompt con las instrucciones de clasificación y la descripción escrita por el ciudadano.
2. Crea una petición y configura `responseMimeType = "application/json"` para solicitar una salida JSON.
3. Envía la petición y obtiene el texto generado.
4. Si no llega texto, devuelve un error indicando que no se recibió el JSON.
5. Elimina posibles delimitadores Markdown como ```` ```json ````. La limpieza es una medida defensiva; lo ideal es que la API devuelva directamente JSON válido.
6. Usa `moshi.adapter(IncidentInfo::class.java).fromJson(...)` para convertir el JSON en un objeto Kotlin.
7. Si la conversión funciona, devuelve `Result.success(incidentInfo)`. Si la respuesta está vacía, tiene una estructura incorrecta o se produce una excepción, devuelve un fallo.

**¿Por qué es útil una salida estructurada?** La interfaz puede mostrar cada dato por separado: categoría, urgencia y resumen. De esta manera, no necesita interpretar una frase libre para decidir cómo llenar la tarjeta de la incidencia.

**Nota:** el modelo puede proponer una categoría o urgencia incorrecta. La respuesta generada no sustituye las reglas de negocio ni la validación humana cuando la clasificación tenga consecuencias importantes. Si el sistema requiere valores estrictos, valida que `categoria` y `urgencia` pertenezcan a los valores permitidos.

**Reto opcional:** las instrucciones generales del clasificador están mezcladas con la descripción del usuario. Investiga el campo `systemInstruction` de la API de Gemini y considera trasladar allí las instrucciones generales, dejando la descripción como contenido del usuario. Para hacerlo, primero habría que ampliar los DTOs para representar el campo de la API.

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
### ¿Qué hace cada sección?

- **`Moshi.Builder().build()`:** crea la instancia de Moshi que utilizará el convertidor de Retrofit y el repositorio.
- **`HttpLoggingInterceptor`:** registra información de las solicitudes y respuestas HTTP. `BODY` incluye los cuerpos de petición y respuesta, por lo que es útil durante el aprendizaje.
- **`redactHeader("x-goog-api-key")`:** oculta el valor de la clave en los registros del encabezado. No elimina otros datos sensibles que pudieran aparecer en el cuerpo.
- **`OkHttpClient.Builder()`:** configura el cliente HTTP.
- **`connectTimeout(30, TimeUnit.SECONDS)`:** establece el tiempo máximo para establecer la conexión.
- **`readTimeout(30, TimeUnit.SECONDS)`:** establece el tiempo máximo de espera de lectura según la configuración del cliente.
- **`Retrofit.Builder()`:** crea el cliente Retrofit.
- **`baseUrl(...)`:** establece la dirección base del servicio.
- **`client(okHttpClient)`:** hace que Retrofit utilice el cliente OkHttp configurado.
- **`addConverterFactory(...)`:** indica a Retrofit que use Moshi para convertir JSON.
- **`retrofit.create(GeminiApi::class.java)`:** genera la implementación de la interfaz Retrofit.
- **`by lazy`:** crea el repositorio cuando se accede a `aiRepository` por primera vez, en vez de hacerlo inmediatamente al cargar el objeto.


### Precaución con los registros

`BODY` puede registrar prompts, datos enviados y respuestas completas. Aunque el encabezado de API se oculte, los cuerpos podrían contener información privada. Úsalo para desarrollo con datos de prueba y reduce o desactiva el registro detallado en versiones de producción.

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

## Paso 8. Compilar, ejecutar y probar

1. En Android Studio, ejecuta **Sync Project with Gradle Files** si acabas de cambiar dependencias.
2. Selecciona **Build > Rebuild Project**. Durante la compilación, KSP debe generar los adaptadores de Moshi para las clases anotadas.
3. Ejecuta la aplicación con **Run 'app'** (`Shift + F10`) en un emulador o dispositivo.
4. Abre **Logcat** y filtra por `OkHttp` o por la etiqueta que utilice el interceptor.
5. Prueba los tres casos de uso:
   - **Texto:** envía una pregunta sencilla y comprueba que se muestre una respuesta.
   - **Imagen:** selecciona una imagen de prueba de un objeto o una escena y pide una descripción.
   - **JSON:** escribe una incidencia urbana y comprueba que se obtengan categoría, urgencia y resumen.
6. Activa el modo avión o deshabilita la conexión y repite una consulta. La aplicación debe mostrar un error controlado y mantenerse abierta.

### ¿Qué se valida con estas pruebas?


| Prueba | Qué permite comprobar |
|---|---|
| Pregunta de texto | Que la petición se construye y la respuesta se extrae correctamente. |
| Análisis de imagen | Que la imagen se comprime, se codifica y se envía con el MIME correcto. |
| Clasificación JSON | Que la respuesta tiene la estructura esperada y Moshi puede convertirla a `IncidentInfo`. |
| Sin conexión | Que los errores de red se manejan sin cerrar la aplicación. |
| Revisión de Logcat | Que la solicitud llega a la capa HTTP y permite diagnosticar fallos. |

---

## 4. Tabla de referencia de los pasos y archivos

| Paso | Archivo o ubicación | Trabajo que se realiza |
|---|---|---|
| 1 | `local.properties` | Guardar la clave de API para el entorno local de desarrollo. |
| 2 | `gradle/libs.versions.toml` | Declarar versiones y alias de dependencias. |
| 2 | `build.gradle.kts` raíz | Declarar el plugin KSP. |
| 2 | `app/build.gradle.kts` | Aplicar KSP y agregar Retrofit, Moshi y OkHttp. |
| 3 | `app/src/main/AndroidManifest.xml` | Declarar el permiso de Internet. |
| 4 | `model/GeminiModels.kt` | Definir los modelos JSON de solicitud y respuesta. |
| 4 | `model/IncidentInfo.kt` | Definir la estructura del resultado de clasificación. |
| 4 | `data/GeminiApi.kt` | Declarar el método HTTP para invocar Gemini. |
| 5 | `data/GeminiAiRepository.kt` | Implementar generación de texto, análisis de imágenes y clasificación JSON. |
| 6 | `di/AppModule.kt` | Crear Retrofit, OkHttp, Moshi y el repositorio. |
| 7 | `util/Constants.kt` | Configurar URL base y modelo vigente. |
| 8 | Android Studio / Logcat | Compilar, ejecutar y validar los tres casos de uso. |

---

## 5. Tabla de errores frecuentes y soluciones

| Error o síntoma | Causa probable | Cómo solucionarlo |
|---|---|---|
| `API_KEY_INVALID` o respuesta HTTP 400 relacionada con la clave | La clave está mal escrita, no es válida o no se está leyendo desde la configuración. | Revisa `GEMINI_API_KEY` en `local.properties`, elimina espacios sobrantes, confirma que Gradle la esté exponiendo en `BuildConfig` y vuelve a compilar. No publiques la clave. |
| HTTP 400 / `INVALID_ARGUMENT` | El cuerpo JSON no tiene la estructura esperada, un campo tiene un valor no admitido o el formato solicitado no es válido. | Revisa los DTOs, los nombres de las propiedades, `contents` y `parts`. Compara el JSON enviado con el esquema vigente de Gemini. |
| HTTP 401 o 403 | La solicitud no está autorizada, la clave no tiene acceso o existe una restricción de API. | Verifica la clave, sus restricciones, el servicio habilitado y la configuración de acceso de Google AI Studio. Consulta el mensaje devuelto por la API. |
| HTTP 404 / `NOT_FOUND` / modelo no encontrado | El identificador del modelo es incorrecto, fue retirado o no admite la operación solicitada. | Comprueba `Constants.MODEL_NAME` y usa un identificador vigente de la documentación oficial. No agregues prefijos o barras que no formen parte del identificador. |
| HTTP 429 / `RESOURCE_EXHAUSTED` | Se alcanzó un límite de solicitudes, tokens o cuota. | Espera antes de reintentar, reduce la frecuencia de llamadas y revisa los límites aplicables a tu proyecto y modelo. Implementa reintentos controlados cuando corresponda. |
| `UnknownHostException`, `SocketTimeoutException` o fallo de conexión | No hay Internet, el servicio no responde o se agotó el tiempo de espera. | Comprueba la conexión, revisa el estado del servicio y verifica los tiempos de espera de OkHttp. Prueba de nuevo sin modo avión. |
| `SecurityException` o la aplicación no puede conectarse | Falta el permiso de Internet en el manifest o está mal ubicado. | Añade `<uses-permission android:name="android.permission.INTERNET" />` dentro de `<manifest>` y fuera de `<application>`. |
| `Unresolved reference: ksp` | El plugin KSP no está declarado o aplicado en el módulo correcto. | Revisa el alias en `libs.versions.toml`, actívalo en el Gradle raíz y aplícalo en `app/build.gradle.kts`. Sincroniza Gradle. |
| Error de versión de KSP o fallo de sincronización de Gradle | La versión de KSP no es compatible con la versión de Kotlin/Gradle del proyecto o falta un alias. | Comprueba las versiones existentes del proyecto, usa una combinación compatible y elimina declaraciones duplicadas. Vuelve a sincronizar. |
| `Cannot serialize Kotlin type...` o Moshi no encuentra un adaptador | No se generó el adaptador de la clase o la clase no está anotada correctamente. | Activa `ksp(libs.moshi.kotlin.codegen)`, aplica el plugin KSP, añade `@JsonClass(generateAdapter = true)` a los DTOs y ejecuta **Rebuild Project**. |
| `JsonDataException` | El JSON recibido no coincide con la clase Kotlin: falta un campo obligatorio o su tipo es diferente. | Inspecciona la respuesta en Logcat, confirma los nombres y tipos de `categoria`, `urgencia` y `resumen`, y valida la salida antes de convertirla. |
| `JsonEncodingException` o JSON inválido | La respuesta no es JSON válido, contiene texto adicional o delimitadores Markdown. | Solicita `application/json`, limpia delimitadores como medida defensiva y revisa el texto original. No dependas únicamente de eliminar ``` si el contenido está mal formado. |
| `firstText()` devuelve `null` | La respuesta no contiene texto en la primera parte, no hay candidatos o la API devolvió una respuesta distinta a la esperada. | Inspecciona la respuesta completa, contempla candidatos vacíos o bloqueos y mejora el manejo de respuestas sin texto. |
| La imagen se rechaza o no se analiza | El MIME no coincide, la imagen es demasiado grande o la estructura `inline_data` es incorrecta. | Confirma que los bytes sean JPEG si usas `image/jpeg`, revisa `inline_data`/`mime_type` y reduce las dimensiones o el tamaño de la imagen. |
| La aplicación se cierra al fallar una petición | Una excepción no se captura o la interfaz no procesa correctamente el `Result.failure`. | Verifica el manejo del resultado en el ViewModel y la pantalla. Muestra un mensaje de error y evita que una excepción de red termine la aplicación. |
| La clave aparece en Logcat | El registro HTTP está imprimiendo el encabezado de autenticación o la clave se incluyó en otro lugar. | Mantén `redactHeader("x-goog-api-key")`, evita imprimir la clave manualmente y no registres datos sensibles. Recuerda que los cuerpos HTTP también pueden contener información privada. |
| La respuesta tarda demasiado | La conexión es lenta, la imagen es pesada o el modelo tarda en generar. | Reduce el tamaño de la imagen, revisa los tiempos de espera y presenta un indicador de carga. Evita bloquear el hilo principal. |

### Orden recomendado para diagnosticar un fallo

1. Lee el mensaje completo en Logcat, incluida la respuesta HTTP si está disponible.
2. Comprueba primero la conectividad y el permiso de Internet.
3. Verifica la clave de API y el nombre del modelo.
4. Revisa el JSON que se envía y la estructura de la respuesta.
5. Si el problema ocurre al convertir JSON, comprueba los DTOs y los adaptadores generados por KSP.
6. Después de cambiar dependencias o anotaciones, sincroniza Gradle y ejecuta **Rebuild Project**.

---

## 6. Buenas prácticas y consideraciones finales

- **No expongas claves:** excluye `local.properties` del control de versiones y evita incluir claves reales en el código fuente.
- **Protege los datos:** utiliza imágenes y descripciones ficticias o no sensibles durante las pruebas. Revisa las políticas de privacidad y tratamiento de datos del servicio.
- **No dependas solo del modelo:** valida las categorías y los niveles de urgencia en el código si la aplicación necesita valores controlados.
- **Maneja los errores en la interfaz:** la aplicación debe mostrar estados de carga, éxito y error en vez de cerrarse ante un fallo de red.
- **Evita registros excesivos en producción:** el interceptor en nivel `BODY` es útil para aprender, pero puede registrar información que no debería almacenarse.
- **Confirma la documentación vigente:** los nombres de modelos, los límites y algunos detalles de la API cambian con el tiempo. Comprueba la [documentación de modelos de Gemini](https://ai.google.dev/gemini-api/docs/models) antes de ejecutar el proyecto.
- **Separa responsabilidades:** deja la comunicación HTTP en `GeminiApi`, la lógica de los casos de uso en el repositorio y la gestión de estado en los ViewModels.

Al terminar el taller, tendrás una base para conectar una aplicación Android con un servicio de IA, enviar texto e imágenes, solicitar una salida estructurada y diagnosticar los errores más comunes de configuración, red y conversión de datos.
