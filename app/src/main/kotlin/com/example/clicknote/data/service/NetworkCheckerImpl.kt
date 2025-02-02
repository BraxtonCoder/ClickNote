package com.example.clicknote.data.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.clicknote.domain.service.NetworkChecker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkCheckerImpl @Inject constructor(
    private val context: Context
) : NetworkChecker {

    override fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
} 