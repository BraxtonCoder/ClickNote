package com.example.clicknote.service

import kotlinx.coroutines.flow.Flow

interface NetworkConnectivityManager {
    /**
     * Check if network is currently available
     */
    fun isNetworkAvailable(): Boolean

    /**
     * Observe network availability changes
     */
    fun observeNetworkAvailability(): Flow<Boolean>

    /**
     * Clean up resources
     */
    fun cleanup()
} 