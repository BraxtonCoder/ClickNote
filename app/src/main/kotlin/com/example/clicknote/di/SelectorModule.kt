package com.example.clicknote.di

import com.example.clicknote.data.selector.TranscriptionServiceSelectorImpl
import com.example.clicknote.domain.selector.TranscriptionServiceSelector
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SelectorModule {
    @Binds
    @Singleton
    abstract fun bindTranscriptionServiceSelector(
        impl: TranscriptionServiceSelectorImpl
    ): TranscriptionServiceSelector
} 