package com.example.clicknote.service

import com.android.billingclient.api.ProductDetails
import com.example.clicknote.domain.model.SubscriptionState

data class SubscriptionPlan(
    val id: String,
    val title: String,
    val description: String,
    val price: String,
    val subscriptionState: SubscriptionState,
    val period: String,
    val features: List<String>,
    val productDetails: ProductDetails
) 