package com.example.clicknote.domain.service

interface NetworkConnectivityManager {
    /**
     * Check if network is available
     * @return true if network is available, false otherwise
     */
    fun isNetworkAvailable(): Boolean
} 