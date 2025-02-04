package com.example.clicknote.di

import com.example.clicknote.data.state.ServiceStateManagerImpl
import com.example.clicknote.domain.state.ServiceStateManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.Lazy

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceStateModule {
    @Binds
    @Singleton
    abstract fun bindServiceStateManager(
        impl: ServiceStateManagerImpl
    ): ServiceStateManager
} 