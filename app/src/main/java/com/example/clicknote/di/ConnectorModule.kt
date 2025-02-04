package com.example.clicknote.di

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import connectors.default.DefaultConnector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConnectorModule {
    
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
            prettyPrint = true
            coerceInputValues = true
        }
    }
    
    @Provides
    @Singleton
    fun provideDefaultConnector(
        firebaseApp: FirebaseApp,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        json: Json
    ): DefaultConnector {
        return DefaultConnector(
            firebaseApp = firebaseApp,
            firestore = firestore,
            storage = storage,
            json = json
        )
    }
} 