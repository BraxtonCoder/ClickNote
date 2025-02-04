package com.example.clicknote.di

import com.example.clicknote.data.lifecycle.ServiceLifecycleManagerImpl
import com.example.clicknote.data.mediator.ServiceMediatorImpl
import com.example.clicknote.data.registry.ServiceRegistryImpl
import com.example.clicknote.data.strategy.ServiceStrategyImpl
import com.example.clicknote.data.service.EventMappingServiceImpl
import com.example.clicknote.domain.lifecycle.ServiceLifecycleManager
import com.example.clicknote.domain.mediator.ServiceMediator
import com.example.clicknote.domain.registry.ServiceRegistry
import com.example.clicknote.domain.service.EventMappingService
import com.example.clicknote.domain.service.ServiceStrategy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.Lazy

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun provideServiceRegistry(): ServiceRegistry {
        return ServiceRegistryImpl()
    }

    @Provides
    @Singleton
    fun provideServiceStrategy(): ServiceStrategy {
        return ServiceStrategyImpl()
    }

    @Provides
    @Singleton
    fun provideServiceMediator(
        registry: Lazy<ServiceRegistry>,
        strategy: Lazy<ServiceStrategy>
    ): ServiceMediator {
        return ServiceMediatorImpl(registry, strategy)
    }

    @Provides
    @Singleton
    fun provideServiceLifecycleManager(
        registry: Lazy<ServiceRegistry>,
        mediator: Lazy<ServiceMediator>,
        strategy: Lazy<ServiceStrategy>
    ): ServiceLifecycleManager {
        return ServiceLifecycleManagerImpl(registry, mediator, strategy)
    }

    @Provides
    @Singleton
    fun provideEventMappingService(): EventMappingService {
        return EventMappingServiceImpl()
    }
}