package com.example.clicknote.domain.event

import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.SharedFlow

interface PurchaseEventBus {
    val purchaseEvents: SharedFlow<Purchase>
    suspend fun emitPurchase(purchase: Purchase)
} 