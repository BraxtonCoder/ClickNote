package com.example.clicknote.di.qualifiers

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WhisperOnline

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WhisperOffline

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OnlineCapable

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OfflineCapable 