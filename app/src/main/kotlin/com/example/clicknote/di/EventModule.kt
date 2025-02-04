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
import dagger.Lazy

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
        @InternalEventFlow eventFlow: MutableSharedFlow<ServiceEvent>
    ): ServiceEventBus {
        return ServiceEventBusImpl(eventFlow)
    }

    @Provides
    @Singleton
    fun provideServiceEventDispatcher(
        eventBus: Lazy<ServiceEventBus>
    ): ServiceEventDispatcher {
        return ServiceEventDispatcherImpl(eventBus)
    }

    @Provides
    @Singleton
    fun provideServiceStateEventMapper(
        eventBus: Lazy<ServiceEventBus>,
        dispatcher: Lazy<ServiceEventDispatcher>
    ): ServiceStateEventMapper {
        return ServiceStateEventMapperImpl(eventBus, dispatcher)
    }

    @Provides
    @Singleton
    fun provideServiceEventHandler(
        eventBus: Lazy<ServiceEventBus>,
        mapper: Lazy<ServiceStateEventMapper>
    ): ServiceEventHandler {
        return ServiceEventHandlerImpl(eventBus, mapper)
    }
} 