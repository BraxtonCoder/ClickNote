package com.example.clicknote.di

import com.example.clicknote.domain.service.NetworkChecker
import com.example.clicknote.service.impl.NetworkCheckerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {
    @Binds
    @Singleton
    abstract fun bindNetworkChecker(
        impl: NetworkCheckerImpl
    ): NetworkChecker
}