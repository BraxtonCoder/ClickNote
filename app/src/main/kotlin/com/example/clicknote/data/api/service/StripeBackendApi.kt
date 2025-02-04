package com.example.clicknote.data.api.service

import com.example.clicknote.data.api.model.*
import retrofit2.http.*

interface StripeBackendApi {
    @POST("subscriptions")
    suspend fun createSubscription(@Body request: CreateSubscriptionRequest): CreateSubscriptionResponse

    @DELETE("subscriptions/current")
    suspend fun cancelSubscription()
} 