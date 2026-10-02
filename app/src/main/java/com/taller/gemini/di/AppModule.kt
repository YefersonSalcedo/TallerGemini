package com.taller.gemini.di

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

    // Usa los adaptadores generados por KSP e ignora las claves desconocidas del JSON.
    private val moshi = Moshi.Builder().build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
        // La API key viaja en este header: se oculta para que no aparezca en Logcat.
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