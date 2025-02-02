package com.example.clicknote.di

import android.content.Context
import com.example.clicknote.service.AuthService
import com.example.clicknote.service.impl.AuthServiceImpl
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindAuthService(
        impl: AuthServiceImpl
    ): AuthService

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

        @Provides
        @Singleton
        fun provideGoogleSignInOptions(): GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("87090099580-c8fmpmpuvggr0ihngkkds4u8ej7n4ct2.apps.googleusercontent.com") // Web client ID from google-services.json
            .requestEmail()
            .build()

        @Provides
        @Singleton
        fun provideGoogleSignInClient(
            @ApplicationContext context: Context,
            options: GoogleSignInOptions
        ): GoogleSignInClient = GoogleSignIn.getClient(context, options)

        @Provides
        fun providePhoneAuthOptions(
            auth: FirebaseAuth,
            @ApplicationContext context: Context
        ): (String, PhoneAuthProvider.OnVerificationStateChangedCallbacks) -> PhoneAuthOptions = { phoneNumber, callbacks ->
            PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(context as android.app.Activity)
                .setCallbacks(callbacks)
                .build()
        }
    }
} 