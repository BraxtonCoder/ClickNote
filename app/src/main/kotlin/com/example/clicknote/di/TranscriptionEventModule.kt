package com.example.clicknote.di

import com.example.clicknote.service.TranscriptionEventHandler
import com.example.clicknote.service.impl.DefaultTranscriptionEventHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TranscriptionEventModule {

    @Binds
    @Singleton
    abstract fun bindTranscriptionEventHandler(
        impl: DefaultTranscriptionEventHandler
    ): TranscriptionEventHandler
} 