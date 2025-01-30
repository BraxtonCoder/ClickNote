package com.example.clicknote.di

import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(impl: FirebaseAuthImpl): FirebaseAuth {
        return impl
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(impl: FirebaseStorageImpl): FirebaseStorage {
        return impl
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(impl: FirebaseFirestoreImpl): FirebaseFirestore {
        return impl
    }

    @Provides
    @Singleton
    fun provideFirebaseApp(): FirebaseApp {
        return FirebaseApp.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }
}