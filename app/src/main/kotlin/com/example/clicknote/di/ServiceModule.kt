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
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import android.app.NotificationManager
import android.content.Context
import android.os.PowerManager
import javax.inject.Qualifier
import android.media.AudioManager

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ServicePowerManager

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ServiceNotificationManager

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @ServicePowerManager
    @Singleton
    fun providePowerManager(
        @ApplicationContext context: Context
    ): PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    @Provides
    @ServiceNotificationManager
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Provides
    @Singleton
    fun provideAudioManager(
        @ApplicationContext context: Context
    ): AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    @Provides
    @Singleton
    fun provideServiceRegistry(): ServiceRegistry = ServiceRegistryImpl()

    @Provides
    @Singleton
    fun provideServiceStrategy(): ServiceStrategy = ServiceStrategyImpl()

    @Provides
    @Singleton
    fun provideServiceMediator(
        registry: ServiceRegistry,
        strategy: ServiceStrategy
    ): ServiceMediator = ServiceMediatorImpl(registry, strategy)

    @Provides
    @Singleton
    fun provideServiceLifecycleManager(
        registry: ServiceRegistry,
        mediator: ServiceMediator,
        strategy: ServiceStrategy
    ): ServiceLifecycleManager = ServiceLifecycleManagerImpl(registry, mediator, strategy)

    @Provides
    @Singleton
    fun provideEventMappingService(): EventMappingService = EventMappingServiceImpl()
}