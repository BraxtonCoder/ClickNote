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
import com.example.clicknote.di.qualifiers.ApplicationScope
import com.example.clicknote.di.qualifiers.InternalEventFlow
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EventModule {
    @Provides
    @Singleton
    @InternalEventFlow
    fun provideMutableEventFlow(): MutableSharedFlow<ServiceEvent> {
        return MutableSharedFlow()
    }

    @Provides
    @Singleton
    fun provideServiceEventBus(
        @InternalEventFlow eventFlow: MutableSharedFlow<ServiceEvent>
    ): ServiceEventBus {
        return ServiceEventBusImpl(eventFlow)
    }

    @Provides
    @Singleton
    fun provideServiceEventDispatcher(
        eventBus: ServiceEventBus
    ): ServiceEventDispatcher {
        return ServiceEventDispatcherImpl(eventBus)
    }

    @Provides
    @Singleton
    fun provideServiceStateEventMapper(): ServiceStateEventMapper {
        return ServiceStateEventMapperImpl()
    }

    @Provides
    @Singleton
    fun provideServiceEventHandler(
        stateManager: ServiceStateManager,
        serviceRegistry: ServiceRegistry,
        @InternalEventFlow eventFlow: SharedFlow<ServiceEvent>,
        @ApplicationScope scope: CoroutineScope
    ): ServiceEventHandler {
        return ServiceEventHandlerImpl(
            stateManager = stateManager,
            serviceRegistry = serviceRegistry,
            eventFlow = eventFlow,
            scope = scope
        )
    }
} 