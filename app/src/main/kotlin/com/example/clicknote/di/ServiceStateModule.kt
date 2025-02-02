package com.example.clicknote.di

import com.example.clicknote.domain.state.ServiceStateManager
import com.example.clicknote.data.state.ServiceStateManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceStateModule {
    @Binds
    @Singleton
    abstract fun bindServiceStateManager(
        impl: ServiceStateManagerImpl
    ): ServiceStateManager
} 