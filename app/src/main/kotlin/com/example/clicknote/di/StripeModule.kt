package com.example.clicknote.di

import com.example.clicknote.BuildConfig
import com.example.clicknote.data.service.StripeApiImpl
import com.example.clicknote.domain.service.StripeApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StripeModule {

    @Binds
    @Singleton
    abstract fun bindStripeApi(impl: StripeApiImpl): StripeApi

    companion object {
        private const val STRIPE_API_URL = "https://api.stripe.com/"

        @Provides
        @Singleton
        fun provideStripeOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer ${BuildConfig.STRIPE_PUBLISHABLE_KEY}")
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                })
                .build()
        }

        @Provides
        @Singleton
        fun provideStripeRetrofit(okHttpClient: OkHttpClient): Retrofit {
            return Retrofit.Builder()
                .baseUrl(STRIPE_API_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
} 