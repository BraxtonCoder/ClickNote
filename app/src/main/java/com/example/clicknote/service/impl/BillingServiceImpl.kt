package com.example.clicknote.service.impl

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.example.clicknote.domain.model.SubscriptionState
import com.example.clicknote.domain.model.SubscriptionStateManager
import com.example.clicknote.service.BillingService
import com.example.clicknote.service.SubscriptionPlan
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stateManager: SubscriptionStateManager
) : BillingService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var billingClient: BillingClient? = null
    private var currentActivity: Activity? = null
    private var cachedProductDetails: Map<String, ProductDetails> = emptyMap()

    private val _subscriptionPlans = MutableStateFlow<List<SubscriptionPlan>>(emptyList())
    override val subscriptionPlans: Flow<List<SubscriptionPlan>> = _subscriptionPlans.asStateFlow()

    override val canTranscribe: Flow<Boolean> = combine(
        stateManager.subscriptionState,
        stateManager.weeklyRecordingsCount
    ) { state, recordings ->
        when (state) {
            SubscriptionState.MONTHLY, SubscriptionState.ANNUAL -> true
            SubscriptionState.FREE -> recordings > 0
            SubscriptionState.ERROR -> false
        }
    }.stateIn(scope, SharingStarted.Eagerly, false)

    override val remainingTranscriptions: Flow<Int> = stateManager.weeklyRecordingsCount
        .stateIn(scope, SharingStarted.Eagerly, SubscriptionState.FREE.weeklyLimit)

    override val freeRecordingsRemaining: Flow<Int> = stateManager.weeklyRecordingsCount
        .stateIn(scope, SharingStarted.Eagerly, SubscriptionState.FREE.weeklyLimit)

    override val subscriptionState: StateFlow<SubscriptionState> = stateManager.subscriptionState
    override val isPremium: StateFlow<Boolean> = stateManager.isPremium

    init {
        setupBillingClient()
    }

    override fun initializeBillingClient(activity: Activity) {
        currentActivity = activity
        setupBillingClient()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch {
                        querySubscriptions()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry connection
                setupBillingClient()
            }
        })
    }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            scope.launch {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            }
        } else {
            scope.launch {
                stateManager.updateSubscriptionState(SubscriptionState.ERROR)
            }
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val newState = when {
                purchase.products.contains(MONTHLY_SUBSCRIPTION_ID) -> SubscriptionState.MONTHLY
                purchase.products.contains(ANNUAL_SUBSCRIPTION_ID) -> SubscriptionState.ANNUAL
                else -> SubscriptionState.FREE
            }
            
            stateManager.updateSubscriptionState(newState)
            
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                withContext(Dispatchers.IO) {
                    billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { }
                }
            }
        }
    }

    private suspend fun launchBillingFlow(productId: String): Result<Unit> {
        val productDetails = cachedProductDetails[productId] ?: return Result.failure(
            IllegalStateException("Product details not available")
        )

        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull()?.offerToken
            ?: return Result.failure(IllegalStateException("No offer token available"))

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            )
            .build()

        return currentActivity?.let { activity ->
            val responseCode = billingClient?.launchBillingFlow(activity, billingFlowParams)?.responseCode
            if (responseCode == BillingClient.BillingResponseCode.OK) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("Failed to launch billing flow: $responseCode"))
            }
        } ?: Result.failure(IllegalStateException("Activity not available"))
    }

    override suspend fun purchaseMonthlySubscription(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            launchBillingFlow(MONTHLY_SUBSCRIPTION_ID)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun purchaseAnnualSubscription(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            launchBillingFlow(ANNUAL_SUBSCRIPTION_ID)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restorePurchases(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            billingClient?.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )?.purchasesList?.forEach { purchase ->
                handlePurchase(purchase)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun hasActiveSubscription(): Boolean = stateManager.isPremium.value

    override suspend fun checkTranscriptionAvailability(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val canTranscribe = canTranscribe.first()
            Result.success(canTranscribe)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun incrementTranscriptionCount(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            stateManager.consumeFreeRecording()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetTranscriptionCount(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            stateManager.resetFreeRecordingsCount()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun showSubscriptionOptions() {
        throw UnsupportedOperationException("Subscription options must be shown from the UI layer")
    }

    override suspend fun manageSubscription() {
        throw UnsupportedOperationException("Subscription management must be handled from the UI layer")
    }

    override fun endBillingConnection() {
        scope.launch {
            stateManager.resetSubscriptionState()
        }
        billingClient?.endConnection()
        billingClient = null
        currentActivity = null
        scope.cancel()
    }

    override fun isPremiumUser(): Boolean = stateManager.isPremium.value

    override suspend fun getRemainingFreeRecordings(): Int = withContext(Dispatchers.IO) {
        stateManager.weeklyRecordingsCount.first()
    }

    override suspend fun consumeFreeRecording() {
        stateManager.consumeFreeRecording()
    }

    override fun resetFreeRecordingsCount() {
        scope.launch {
            stateManager.resetFreeRecordingsCount()
        }
    }

    override suspend fun openPremiumPurchase() {
        currentActivity?.let { activity ->
            val productDetails = cachedProductDetails[MONTHLY_SUBSCRIPTION_ID]
            if (productDetails != null) {
                launchBillingFlow(MONTHLY_SUBSCRIPTION_ID)
            }
        }
    }

    override suspend fun openManageSubscriptions() {
        throw UnsupportedOperationException("Subscription management must be handled from the UI layer")
    }

    override suspend fun checkPremiumStatus(): Boolean = withContext(Dispatchers.IO) {
        stateManager.isPremium.first()
    }

    private fun querySubscriptions() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(MONTHLY_SUBSCRIPTION_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(ANNUAL_SUBSCRIPTION_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                cachedProductDetails = productDetailsList.associateBy { it.productId }
                _subscriptionPlans.value = productDetailsList.map { details ->
                    when (details.productId) {
                        MONTHLY_SUBSCRIPTION_ID -> SubscriptionPlan.MONTHLY
                        ANNUAL_SUBSCRIPTION_ID -> SubscriptionPlan.ANNUAL
                        else -> SubscriptionPlan.FREE
                    }
                }
            }
        }
    }

    companion object {
        private const val MONTHLY_SUBSCRIPTION_ID = "clicknote_premium_monthly"
        private const val ANNUAL_SUBSCRIPTION_ID = "clicknote_premium_annual"
    }
} 