package com.example.clicknote.di

import android.content.Context
import com.example.clicknote.BuildConfig
import com.example.clicknote.data.api.StripeBackendApi
import com.example.clicknote.data.api.StripeBackendApiImpl
import com.example.clicknote.data.api.StripeService
import com.stripe.android.Stripe
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.stripe.com/v1/") // Replace with your actual base URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideStripeService(retrofit: Retrofit): StripeService {
        return retrofit.create(StripeService::class.java)
    }

    @Provides
    @Singleton
    fun provideStripe(@ApplicationContext context: Context): Stripe {
        return Stripe(context, BuildConfig.STRIPE_PUBLISHABLE_KEY)
    }

    @Provides
    @Singleton
    fun provideStripeBackendApi(stripeService: StripeService): StripeBackendApi {
        return StripeBackendApiImpl(stripeService)
    }
} 