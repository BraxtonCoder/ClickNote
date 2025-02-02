package com.example.clicknote.di

import com.example.clicknote.data.event.ServiceEventBusImpl
import com.example.clicknote.data.event.ServiceEventDispatcherImpl
import com.example.clicknote.data.handler.ServiceEventHandlerImpl
import com.example.clicknote.data.mapper.ServiceStateEventMapperImpl
import com.example.clicknote.domain.event.ServiceEventBus
import com.example.clicknote.domain.event.ServiceEventDispatcher
import com.example.clicknote.domain.event.ServiceEventHandler
import com.example.clicknote.domain.event.ServiceEvent
import com.example.clicknote.domain.mapper.ServiceStateEventMapper
import com.example.clicknote.domain.registry.ServiceRegistry
import com.example.clicknote.domain.state.ServiceStateManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow

@Module
@InstallIn(SingletonComponent::class)
abstract class EventModule {
    @Binds
    @Singleton
    abstract fun bindServiceEventDispatcher(impl: ServiceEventDispatcherImpl): ServiceEventDispatcher

    @Binds
    @Singleton
    abstract fun bindServiceStateEventMapper(impl: ServiceStateEventMapperImpl): ServiceStateEventMapper

    companion object {
        @Provides
        @Singleton
        fun provideEventFlow(): MutableSharedFlow<ServiceEvent> = MutableSharedFlow()

        @Provides
        @Singleton
        fun provideServiceEventBus(eventFlow: MutableSharedFlow<ServiceEvent>): ServiceEventBus =
            ServiceEventBusImpl(eventFlow)

        @Provides
        @Singleton
        fun provideServiceEventHandler(
            stateManager: Provider<ServiceStateManager>,
            registry: Provider<ServiceRegistry>,
            eventFlow: MutableSharedFlow<ServiceEvent>,
            @ApplicationScope coroutineScope: CoroutineScope
        ): ServiceEventHandler = ServiceEventHandlerImpl(
            stateManager = stateManager,
            registry = registry,
            events = eventFlow,
            coroutineScope = coroutineScope
        )
    }
} 