package com.example.clicknote.data.mediator

import com.android.billingclient.api.Purchase
import com.example.clicknote.domain.mediator.PurchaseMediator
import com.example.clicknote.domain.service.BillingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseMediatorImpl @Inject constructor(
    private val billingService: BillingService
) : PurchaseMediator {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _purchaseEvents = MutableSharedFlow<Purchase>()
    override val purchaseEvents: SharedFlow<Purchase> = _purchaseEvents

    init {
        billingService.setPurchaseCallback { purchase ->
            coroutineScope.launch {
                _purchaseEvents.emit(purchase)
            }
        }
    }

    override suspend fun emitPurchase(purchase: Purchase) {
        _purchaseEvents.emit(purchase)
    }
} 