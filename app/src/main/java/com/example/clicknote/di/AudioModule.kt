package com.example.clicknote.di

import android.content.Context
import android.media.AudioManager
import com.example.clicknote.service.AudioRecorder
import com.example.clicknote.service.impl.AudioRecorderImpl
import com.example.clicknote.service.audio.AudioService
import com.example.clicknote.service.audio.AudioServiceImpl
import com.example.clicknote.service.AudioEnhancer
import com.example.clicknote.service.impl.AudioEnhancerImpl
import com.example.clicknote.service.AudioConverter
import com.example.clicknote.service.impl.AudioConverterImpl
import com.example.clicknote.service.AudioPlayer
import com.example.clicknote.service.impl.AudioPlayerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {
    
    @Binds
    @Singleton
    abstract fun bindAudioRecorder(impl: AudioRecorderImpl): AudioRecorder

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

    companion object {
        @Provides
        @Singleton
        fun provideAudioManager(@ApplicationContext context: Context): AudioManager {
            return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }
    }
}