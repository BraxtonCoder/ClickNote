package com.example.clicknote.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object StateModule {
    // ServiceStateManager binding moved to ServiceStateModule
} 