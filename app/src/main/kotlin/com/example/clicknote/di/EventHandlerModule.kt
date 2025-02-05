package com.example.clicknote.di

import com.example.clicknote.data.handler.ServiceEventHandlerImpl
import com.example.clicknote.domain.event.ServiceEvent
import com.example.clicknote.domain.event.ServiceEventHandler
import com.example.clicknote.domain.registry.ServiceRegistry
import com.example.clicknote.domain.state.ServiceStateManager
import com.example.clicknote.di.qualifiers.ApplicationScope
import com.example.clicknote.di.qualifiers.InternalEventFlow
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EventHandlerModule {

    @Provides
    @Singleton
    fun provideServiceEventHandler(
        stateManager: ServiceStateManager,
        serviceRegistry: ServiceRegistry,
        @InternalEventFlow eventFlow: SharedFlow<ServiceEvent>,
        @ApplicationScope scope: CoroutineScope
    ): ServiceEventHandler = ServiceEventHandlerImpl(
        stateManager = stateManager,
        serviceRegistry = serviceRegistry,
        eventFlow = eventFlow,
        scope = scope
    )
} 