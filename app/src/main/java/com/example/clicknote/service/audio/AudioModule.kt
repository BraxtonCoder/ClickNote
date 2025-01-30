package com.example.clicknote.service.audio

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {
    
    @Binds
    @Singleton
    abstract fun bindAudioService(impl: AudioServiceImpl): AudioService

    @Binds
    @Singleton
    abstract fun bindAudioEnhancer(impl: AudioEnhancerImpl): AudioEnhancer

    @Binds
    @Singleton
    abstract fun bindAudioConverter(impl: AudioConverterImpl): AudioConverter

    @Binds
    @Singleton
    abstract fun bindAudioPlayer(impl: AudioPlayerImpl): AudioPlayer
} 