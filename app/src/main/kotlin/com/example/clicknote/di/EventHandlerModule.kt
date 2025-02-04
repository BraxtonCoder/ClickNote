package com.example.clicknote.di

import com.example.clicknote.data.handler.ServiceEventHandlerImpl
import com.example.clicknote.domain.event.ServiceEvent
import com.example.clicknote.domain.event.ServiceEventHandler
import com.example.clicknote.domain.registry.ServiceRegistry
import com.example.clicknote.domain.state.ServiceStateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Singleton
import javax.inject.Provider
import dagger.Lazy
import com.example.clicknote.di.ApplicationScope
import com.example.clicknote.di.InternalEventFlow

@Module
@InstallIn(SingletonComponent::class)
object EventHandlerModule {
    @Provides
    @Singleton
    fun provideServiceEventHandler(
        stateManager: Provider<ServiceStateManager>,
        registry: Provider<ServiceRegistry>,
        @InternalEventFlow eventFlow: Provider<SharedFlow<ServiceEvent>>,
        @ApplicationScope coroutineScope: Provider<CoroutineScope>
    ): ServiceEventHandler = ServiceEventHandlerImpl(
        stateManager = stateManager,
        registry = registry,
        events = eventFlow,
        coroutineScope = coroutineScope.get()
    )
} 