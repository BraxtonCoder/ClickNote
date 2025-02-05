package com.example.clicknote.di

import com.example.clicknote.data.audio.AmplitudeProcessorImpl
import com.example.clicknote.domain.audio.AmplitudeProcessor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProcessorModule {

    @Binds
    @Singleton
    abstract fun bindAmplitudeProcessor(
        impl: AmplitudeProcessorImpl
    ): AmplitudeProcessor
}