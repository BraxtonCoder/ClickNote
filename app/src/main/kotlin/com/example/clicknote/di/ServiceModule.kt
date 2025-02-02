package com.example.clicknote.di

import com.example.clicknote.data.event.ServiceEventBusImpl
import com.example.clicknote.data.lifecycle.ServiceLifecycleManagerImpl
import com.example.clicknote.data.mediator.ServiceMediatorImpl
import com.example.clicknote.data.registry.ServiceRegistryImpl
import com.example.clicknote.data.state.ServiceStateManagerImpl
import com.example.clicknote.data.strategy.ServiceStrategyImpl
import com.example.clicknote.data.service.EventMappingServiceImpl
import com.example.clicknote.domain.event.ServiceEventBus
import com.example.clicknote.domain.lifecycle.ServiceLifecycleManager
import com.example.clicknote.domain.mediator.ServiceMediator
import com.example.clicknote.domain.registry.ServiceRegistry
import com.example.clicknote.domain.service.EventMappingService
import com.example.clicknote.domain.service.ServiceStrategy
import com.example.clicknote.domain.state.ServiceStateManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds
    @Singleton
    abstract fun bindServiceStateManager(impl: ServiceStateManagerImpl): ServiceStateManager

    @Binds
    @Singleton
    abstract fun bindServiceRegistry(impl: ServiceRegistryImpl): ServiceRegistry

    @Binds
    @Singleton
    abstract fun bindServiceLifecycleManager(impl: ServiceLifecycleManagerImpl): ServiceLifecycleManager

    @Binds
    @Singleton
    abstract fun bindServiceMediator(impl: ServiceMediatorImpl): ServiceMediator

    @Binds
    @Singleton
    abstract fun bindServiceStrategy(impl: ServiceStrategyImpl): ServiceStrategy

    @Binds
    @Singleton
    abstract fun bindServiceEventBus(impl: ServiceEventBusImpl): ServiceEventBus

    @Binds
    @Singleton
    abstract fun bindEventMappingService(impl: EventMappingServiceImpl): EventMappingService
}