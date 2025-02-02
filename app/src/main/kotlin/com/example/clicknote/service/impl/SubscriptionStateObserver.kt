package com.example.clicknote.service.impl

import android.content.Context
import com.example.clicknote.domain.model.SubscriptionState
import com.example.clicknote.service.AuthService
import com.example.clicknote.service.SubscriptionStateManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import dagger.Lazy

@Singleton
class SubscriptionStateObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authService: Lazy<AuthService>,
    private val subscriptionStateManager: Provider<SubscriptionStateManager>
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        scope.launch {
            authService.get().isSignedIn.collectLatest { isSignedIn ->
                if (!isSignedIn) {
                    subscriptionStateManager.get().updateSubscriptionState(SubscriptionState.FREE)
                }
            }
        }
    }
} 