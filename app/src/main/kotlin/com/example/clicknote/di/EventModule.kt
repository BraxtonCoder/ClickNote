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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.CoroutineScope
import dagger.Lazy
import dagger.Provider

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class InternalEventFlow

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
        @InternalEventFlow eventFlow: Provider<MutableSharedFlow<ServiceEvent>>
    ): ServiceEventBus {
        return ServiceEventBusImpl(eventFlow.get())
    }

    @Provides
    @Singleton
    fun provideServiceEventDispatcher(
        eventBus: Provider<ServiceEventBus>
    ): ServiceEventDispatcher {
        return ServiceEventDispatcherImpl(eventBus)
    }

    @Provides
    @Singleton
    fun provideServiceStateEventMapper(
        stateManager: Provider<ServiceStateManager>,
        eventBus: Provider<ServiceEventBus>,
        @ApplicationScope coroutineScope: CoroutineScope
    ): ServiceStateEventMapper {
        return ServiceStateEventMapperImpl(stateManager, eventBus, coroutineScope)
    }

    @Provides
    @Singleton
    fun provideServiceEventHandler(
        stateManager: Provider<ServiceStateManager>,
        registry: Provider<ServiceRegistry>,
        @InternalEventFlow eventFlow: Provider<SharedFlow<ServiceEvent>>,
        @ApplicationScope coroutineScope: CoroutineScope
    ): ServiceEventHandler {
        return ServiceEventHandlerImpl(stateManager, registry, eventFlow, coroutineScope)
    }
} 