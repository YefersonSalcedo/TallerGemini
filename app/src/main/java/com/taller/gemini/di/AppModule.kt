package com.taller.gemini.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
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

/**
 * Módulo de inyección de dependencias simple (Service Locator).
 * Este es el único punto de la aplicación donde se decide qué implementación
 * de [AiRepository] se utiliza.
 */
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
