package com.hieupnd.wordflash.di

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.hieupnd.wordflash.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GeminiModule {

    @Provides
    @Singleton
    @Named("gemini_models")
    fun provideGenerativeModels(): List<GenerativeModel> {
        val config = generationConfig {
            responseMimeType = "application/json"
            temperature = 0.2f
        }
        val modelNames = listOf(
            "gemini-3.5-flash",
            "gemini-3.1-flash-Lite",
            "gemini-2.5-flash",
            "gemini-2.5-flash-lite"
        )
        return modelNames.map { name ->
            GenerativeModel(
                modelName = name,
                apiKey = BuildConfig.GEMINI_API_KEY,
                generationConfig = config
            )
        }
    }
}
