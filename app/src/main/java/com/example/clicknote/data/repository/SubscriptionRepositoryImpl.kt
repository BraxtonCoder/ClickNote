package com.example.clicknote.data.repository

import com.example.clicknote.data.api.StripeApi
import com.example.clicknote.data.local.SubscriptionDao
import com.example.clicknote.data.model.SubscriptionStatus
import com.example.clicknote.data.model.SubscriptionTier
import com.example.clicknote.domain.repository.SubscriptionRepository
import com.example.clicknote.data.preferences.UserPreferences
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val stripeApi: StripeApi,
    private val subscriptionDao: SubscriptionDao,
    private val userPreferences: UserPreferences
) : SubscriptionRepository {

    override suspend fun subscribe(tier: SubscriptionTier, paymentMethodId: String) {
        val priceId = when (tier) {
            is SubscriptionTier.Free -> return // Free tier doesn't need Stripe subscription
            is SubscriptionTier.Monthly -> MONTHLY_PRICE_ID
            is SubscriptionTier.Annual -> ANNUAL_PRICE_ID
        }
        
        val subscription = stripeApi.createSubscription(
            priceId = priceId,
            paymentMethodId = paymentMethodId
        )
        
        // Update local subscription status
        updateSubscriptionStatus(SubscriptionStatus(
            tier = tier,
            isActive = true,
            weeklyUsageCount = 0,
            expiryDate = subscription.currentPeriodEnd
        ))
    }

    override suspend fun updateSubscriptionStatus(status: SubscriptionStatus) {
        subscriptionDao.updateSubscriptionStatus(status)
    }

    override suspend fun getSubscriptionStatus(): SubscriptionStatus {
        return subscriptionDao.getSubscriptionStatus() ?: SubscriptionStatus(
            tier = SubscriptionTier.Free(),
            isActive = false,
            weeklyUsageCount = 0,
            expiryDate = null
        )
    }

    override suspend fun cancelSubscription() {
        val currentStatus = getSubscriptionStatus()
        if (currentStatus.tier is SubscriptionTier.Free) return
        
        stripeApi.cancelSubscription()
        
        // Update to free tier after cancellation
        updateSubscriptionStatus(SubscriptionStatus(
            tier = SubscriptionTier.Free(),
            isActive = false,
            weeklyUsageCount = 0,
            expiryDate = null
        ))
    }

    companion object {
        private const val MONTHLY_PRICE_ID = "price_monthly"
        private const val ANNUAL_PRICE_ID = "price_annual"
    }
} 