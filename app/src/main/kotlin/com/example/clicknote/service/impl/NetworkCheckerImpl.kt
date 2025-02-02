package com.example.clicknote.service.impl

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.clicknote.domain.service.NetworkChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkCheckerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkChecker {

    override fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
} 