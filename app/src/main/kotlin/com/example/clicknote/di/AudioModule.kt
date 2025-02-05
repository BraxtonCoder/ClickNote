package com.example.clicknote.di

import android.content.Context
import android.media.AudioManager
import com.example.clicknote.domain.audio.AudioRecorder
import com.example.clicknote.data.audio.AudioRecorderImpl
import com.example.clicknote.domain.audio.AudioConverter
import com.example.clicknote.data.audio.AudioConverterImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {
    
    @Binds
    @Singleton
    abstract fun bindAudioRecorder(impl: AudioRecorderImpl): AudioRecorder

    @Binds
    @Singleton
    abstract fun bindAudioConverter(impl: AudioConverterImpl): AudioConverter

    companion object {
        @Provides
        @Singleton
        fun provideAudioManager(@ApplicationContext context: Context): AudioManager {
            return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }
    }
}