package com.example.clicknote.data.mediator

import com.example.clicknote.di.qualifiers.ApplicationScope
import com.example.clicknote.domain.mediator.PurchaseMediator
import com.example.clicknote.domain.model.Purchase
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.repository.PurchaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseMediatorImpl @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    @ApplicationScope private val scope: CoroutineScope
) : PurchaseMediator {

    private val _currentPlan = MutableStateFlow<SubscriptionPlan>(SubscriptionPlan.FREE)
    override val currentPlan: Flow<SubscriptionPlan> = _currentPlan.asStateFlow()

    private val _purchases = MutableSharedFlow<Purchase>()
    override val purchases: Flow<Purchase> = _purchases.asSharedFlow()

    init {
        scope.launch {
            purchaseRepository.getCurrentPlan()
                .collect { plan ->
                    _currentPlan.value = plan
                }
        }
    }

    override suspend fun getWeeklyTranscriptionCount(): Int {
        return purchaseRepository.getWeeklyTranscriptionCount()
    }

    override suspend fun incrementWeeklyTranscriptionCount() {
        purchaseRepository.incrementWeeklyTranscriptionCount()
    }

    override suspend fun resetWeeklyTranscriptionCount() {
        purchaseRepository.resetWeeklyTranscriptionCount()
    }

    override suspend fun canMakeTranscription(): Boolean {
        val plan = _currentPlan.value
        val count = getWeeklyTranscriptionCount()
        return when (plan) {
            SubscriptionPlan.FREE -> count < SubscriptionPlan.FREE.weeklyLimit
            else -> true
        }
    }

    override suspend fun processPurchase(purchase: Purchase) {
        scope.launch(Dispatchers.IO) {
            try {
                purchaseRepository.savePurchase(purchase)
                _currentPlan.value = purchase.plan
                _purchases.emit(purchase)
            } catch (e: Exception) {
                handlePurchaseError(e)
            }
        }
    }

    override suspend fun handlePurchaseError(error: Throwable) {
        scope.launch(Dispatchers.IO) {
            _purchases.emit(
                Purchase(
                    id = "error",
                    userId = "",
                    plan = _currentPlan.value,
                    amount = 0.0,
                    status = PurchaseStatus.FAILED
                )
            )
        }
    }
}