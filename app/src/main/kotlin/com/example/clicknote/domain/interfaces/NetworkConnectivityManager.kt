package com.example.clicknote.domain.interfaces

import kotlinx.coroutines.flow.Flow

interface NetworkConnectivityManager {
    val isNetworkAvailable: Flow<Boolean>
    val isWifiConnected: Flow<Boolean>
    val isCellularConnected: Flow<Boolean>
    val isMeteredConnection: Flow<Boolean>
    val isVpnConnected: Flow<Boolean>
    val networkType: Flow<String>
    val signalStrength: Flow<Int>
    
    fun startMonitoring()
    fun stopMonitoring()
    fun isNetworkAvailableSync(): Boolean
    fun isWifiConnectedSync(): Boolean
    fun isCellularConnectedSync(): Boolean
    fun isMeteredConnectionSync(): Boolean
    fun isVpnConnectedSync(): Boolean
    fun getNetworkTypeSync(): String
    fun getSignalStrengthSync(): Int
} 