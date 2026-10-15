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

## 2. Arquitectura de la Aplicación

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

## 3. Cómo realizar el taller

1. Abre el proyecto en Android Studio y espera a que termine la sincronización inicial de Gradle.
2. Obtén una clave de API en [Google AI Studio](https://aistudio.google.com/app/apikey) y guárdala en `local.properties` como `GEMINI_API_KEY=TuClaveRealDeGeminiAqui`. Este archivo ya está en `.gitignore`.
3. Sigue la **[guía paso a paso](app/src/main/java/com/taller/gemini/GUIA_PASOS.md)**, que explica cada cambio y el código que hay que completar.

### Resumen de los pasos y archivos

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

Si algo falla durante la ejecución, consulta la sección de [errores frecuentes y soluciones](app/src/main/java/com/taller/gemini/GUIA_PASOS.md#errores-frecuentes-y-soluciones) de la guía.

---

## 4. Buenas prácticas y consideraciones finales

- **No expongas claves:** excluye `local.properties` del control de versiones y evita incluir claves reales en el código fuente.
- **Protege los datos:** utiliza imágenes y descripciones ficticias o no sensibles durante las pruebas. Revisa las políticas de privacidad y tratamiento de datos del servicio.
- **No dependas solo del modelo:** valida las categorías y los niveles de urgencia en el código si la aplicación necesita valores controlados.
- **Maneja los errores en la interfaz:** la aplicación debe mostrar estados de carga, éxito y error en vez de cerrarse ante un fallo de red.
- **Evita registros excesivos en producción:** el interceptor en nivel `BODY` es útil para aprender, pero puede registrar información que no debería almacenarse.
- **Confirma la documentación vigente:** los nombres de modelos, los límites y algunos detalles de la API cambian con el tiempo. Comprueba la [documentación de modelos de Gemini](https://ai.google.dev/gemini-api/docs/models) antes de ejecutar el proyecto.
- **Separa responsabilidades:** deja la comunicación HTTP en `GeminiApi`, la lógica de los casos de uso en el repositorio y la gestión de estado en los ViewModels.

Al terminar el taller, tendrás una base para conectar una aplicación Android con un servicio de IA, enviar texto e imágenes, solicitar una salida estructurada y diagnosticar los errores más comunes de configuración, red y conversión de datos.
