# Taller: Integración de APIs de IA con Google Gemini en Android (rama `solucion`)

Esta rama contiene el proyecto **completamente resuelto**. Todos los pasos del taller (1 al 8) ya están implementados, salvo uno:

> ## Lo único que falta es tu API Key de Gemini
>
> Por seguridad, la clave **no está incluida** en el repositorio. Agrégala en `local.properties` y la app queda lista para ejecutarse.

---

## Cómo ejecutarla

### 1. Obtén tu API Key

1. Ingresa a [Google AI Studio](https://aistudio.google.com/app/apikey) con tu cuenta de Google.
2. Haz clic en **"Get API key"** y crea una clave gratuita.

### 2. Agrégala en `local.properties`

En la raíz del proyecto, crea (o edita) el archivo `local.properties` y agrega:

```
GEMINI_API_KEY=TuClaveRealDeGeminiAqui
```

- No dejes espacios antes ni después de la clave.
- `local.properties` ya está en el `.gitignore`, así que no se subirá a GitHub.
- No compartas tu clave ni la muestres en capturas de pantalla.

### 3. Compila y ejecuta

1. En Android Studio: **Build > Rebuild Project** (KSP genera los adaptadores de Moshi en este paso).
2. Presiona **Run 'app'** (`Shift + F10`).
3. Prueba las 3 pestañas: **Texto**, **Imagen** y **JSON**.

---

## Arquitectura

```
app/src/main/java/com/taller/gemini/
├── MainActivity.kt                # Única Activity
├── di/AppModule.kt                # Contenedor de dependencias (Retrofit + Moshi)
├── util/Constants.kt              # URL base y modelo Gemini
├── model/                         # IncidentInfo y DTOs de la API
├── data/                          # AiRepository, GeminiApi, GeminiAiRepository
├── ui/                            # UiState, components, navigation, screens, theme
└── viewmodel/                     # TextViewModel, ImageViewModel, JsonViewModel
```

---