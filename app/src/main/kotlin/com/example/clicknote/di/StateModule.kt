package com.example.clicknote.di

import com.example.clicknote.domain.state.ServiceStateManager
import com.example.clicknote.data.state.ServiceStateManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StateModule {
    @Provides
    @Singleton
    fun provideServiceStateManager(): ServiceStateManager = 
        ServiceStateManagerImpl()
} 