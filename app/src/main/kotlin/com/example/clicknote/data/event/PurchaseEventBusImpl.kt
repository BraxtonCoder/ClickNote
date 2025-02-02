package com.example.clicknote.data.event

import com.android.billingclient.api.Purchase
import com.example.clicknote.domain.event.PurchaseEventBus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseEventBusImpl @Inject constructor() : PurchaseEventBus {
    private val _purchaseEvents = MutableSharedFlow<Purchase>()
    override val purchaseEvents: SharedFlow<Purchase> = _purchaseEvents.asSharedFlow()

    override suspend fun emitPurchase(purchase: Purchase) {
        _purchaseEvents.emit(purchase)
    }
} 