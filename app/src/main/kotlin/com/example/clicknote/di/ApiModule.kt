package com.example.clicknote.di

import com.example.clicknote.service.api.*
import com.example.clicknote.service.api.impl.*
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.Provides
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideClaudeApi(impl: ClaudeApiImpl): ClaudeApi {
        return impl
    }

    @Provides
    @Singleton
    fun provideOpenAiApi(impl: OpenAiApiImpl): OpenAiApi {
        return impl
    }

    @Provides
    @Singleton
    fun provideWhisperApi(impl: WhisperApiImpl): WhisperApi {
        return impl
    }

    @Provides
    @Singleton
    fun provideStorageApi(impl: StorageApiImpl): StorageApi {
        return impl
    }
} 