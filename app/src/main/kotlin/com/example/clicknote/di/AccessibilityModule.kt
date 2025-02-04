package com.example.clicknote.di

import android.content.Context
import android.media.AudioManager
import android.os.Vibrator
import com.example.clicknote.service.VolumeButtonHandler
import com.example.clicknote.service.impl.VolumeButtonHandlerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AccessibilityModule {

    @Provides
    @Singleton
    fun provideVolumeButtonHandler(
        @ApplicationContext context: Context,
        vibrator: Vibrator,
        audioManager: AudioManager
    ): VolumeButtonHandler {
        return VolumeButtonHandlerImpl(context, vibrator, audioManager)
    }
} 