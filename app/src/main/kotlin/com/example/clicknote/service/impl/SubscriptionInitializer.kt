package com.example.clicknote.service.impl

import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.service.BillingService
import com.example.clicknote.domain.model.SubscriptionStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class SubscriptionInitializer @Inject constructor(
    private val billingService: Provider<BillingService>,
    private val stateManager: Provider<SubscriptionStateManager>,
    private val coroutineScope: CoroutineScope
) {
    fun initialize() {
        coroutineScope.launch {
            // Initialize with default free state
            stateManager.get().resetSubscriptionState()
            
            // Initial billing client setup will trigger its own state updates
            billingService.get()
        }
    }
} 