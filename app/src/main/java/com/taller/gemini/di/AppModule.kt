package com.taller.gemini.di

import com.taller.gemini.data.AiRepository

/**
 * =============================================================================
 * TODO(PASO 6): Conectar GeminiAiRepository
 * =============================================================================
 *
 * Construye, en este orden:
 *   1. moshi: Moshi (usa los adaptadores generados por KSP)
 *   2. loggingInterceptor: HttpLoggingInterceptor con level = BODY y
 *      redactHeader("x-goog-api-key") para ocultar la clave en Logcat
 *   3. okHttpClient: OkHttpClient con el interceptor y timeouts de 30 s
 *   4. retrofit: Retrofit con Constants.GEMINI_BASE_URL, el cliente y MoshiConverterFactory
 *   5. geminiApi: retrofit.create(GeminiApi::class.java)
 *
 * Y expón:
 *   val aiRepository: AiRepository = GeminiAiRepository(geminiApi, moshi = moshi)
 */
object AppModule {

    val aiRepository: AiRepository by lazy {
        TODO("PASO 6: construir Moshi, OkHttp, Retrofit y devolver GeminiAiRepository")
    }
}