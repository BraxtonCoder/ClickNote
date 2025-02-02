package com.example.clicknote.di

import android.content.Context
import com.example.clicknote.BuildConfig
import com.example.clicknote.data.api.*
import com.example.clicknote.service.api.*
import com.example.clicknote.service.api.impl.*
import com.stripe.android.Stripe
import com.example.clicknote.domain.service.NetworkChecker
import com.example.clicknote.service.impl.DefaultNetworkChecker
import dagger.Binds
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
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindNetworkChecker(
        impl: DefaultNetworkChecker
    ): NetworkChecker

    companion object {
        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        }

        @Provides
        @Singleton
        fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
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
        fun provideStripeApi(stripeService: StripeService): StripeApi {
            return StripeApiImpl(stripeService)
        }

        @Provides
        @Singleton
        fun provideClaudeApi(retrofit: Retrofit): ClaudeApi {
            return retrofit.create(ClaudeApi::class.java)
        }

        @Provides
        @Singleton
        fun provideOpenAiApi(retrofit: Retrofit): OpenAiApi {
            return retrofit.create(OpenAiApi::class.java)
        }

        @Provides
        @Singleton
        fun provideWhisperApi(retrofit: Retrofit): WhisperApi {
            return retrofit.create(WhisperApi::class.java)
        }

        @Provides
        @Singleton
        fun provideStorageApi(retrofit: Retrofit): StorageApi {
            return retrofit.create(StorageApi::class.java)
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
} 