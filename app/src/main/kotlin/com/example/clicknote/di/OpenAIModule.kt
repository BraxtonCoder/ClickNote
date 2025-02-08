package com.example.clicknote.di

import android.content.Context
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.client.OpenAI
import com.example.clicknote.BuildConfig
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.service.OpenAiService
import com.example.clicknote.service.impl.OpenAiServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Module
@InstallIn(SingletonComponent::class)
object OpenAIModule {
    @Provides
    @Singleton
    fun provideOpenAIConfig(): OpenAI {
        return OpenAI(
            token = BuildConfig.OPENAI_API_KEY,
            timeout = Timeout(socket = 60.seconds)
        )
    }

    @Provides
    @Singleton
    fun provideOpenAiService(impl: OpenAiServiceImpl): OpenAiService {
        return impl
    }
} 