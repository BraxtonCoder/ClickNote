package com.example.clicknote.service.impl

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import com.example.clicknote.domain.interfaces.NetworkConnectivityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkConnectivityManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkConnectivityManager {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private val _isNetworkAvailable = MutableStateFlow(false)
    override val isNetworkAvailable: Flow<Boolean> = _isNetworkAvailable.asStateFlow()

    private val _isWifiConnected = MutableStateFlow(false)
    override val isWifiConnected: Flow<Boolean> = _isWifiConnected.asStateFlow()

    private val _isCellularConnected = MutableStateFlow(false)
    override val isCellularConnected: Flow<Boolean> = _isCellularConnected.asStateFlow()

    private val _isMeteredConnection = MutableStateFlow(false)
    override val isMeteredConnection: Flow<Boolean> = _isMeteredConnection.asStateFlow()

    private val _isVpnConnected = MutableStateFlow(false)
    override val isVpnConnected: Flow<Boolean> = _isVpnConnected.asStateFlow()

    private val _networkType = MutableStateFlow("")
    override val networkType: Flow<String> = _networkType.asStateFlow()

    private val _signalStrength = MutableStateFlow(0)
    override val signalStrength: Flow<Int> = _signalStrength.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateNetworkState(network)
        }

        override fun onLost(network: Network) {
            updateNetworkState(null)
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            updateNetworkState(network)
        }
    }

    private val phoneStateListener = object : PhoneStateListener() {
        @Deprecated("Deprecated in Java")
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            super.onSignalStrengthsChanged(signalStrength)
            signalStrength?.let {
                _signalStrength.value = it.level
            }
        }
    }

    override fun startMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)

        // Initial state update
        updateNetworkState(connectivityManager.activeNetwork)
    }

    override fun stopMonitoring() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
    }

    override fun isNetworkAvailableSync(): Boolean {
        return connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)?.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
        } ?: false
    }

    override fun isWifiConnectedSync(): Boolean {
        return connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)?.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            )
        } ?: false
    }

    override fun isCellularConnectedSync(): Boolean {
        return connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)?.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            )
        } ?: false
    }

    override fun isMeteredConnectionSync(): Boolean {
        return connectivityManager.activeNetwork?.let { network ->
            connectivityManager.isActiveNetworkMetered
        } ?: false
    }

    override fun isVpnConnectedSync(): Boolean {
        return connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)?.hasTransport(
                NetworkCapabilities.TRANSPORT_VPN
            )
        } ?: false
    }

    override fun getNetworkTypeSync(): String {
        return when {
            isWifiConnectedSync() -> "WiFi"
            isCellularConnectedSync() -> "Cellular"
            isVpnConnectedSync() -> "VPN"
            else -> "Unknown"
        }
    }

    override fun getSignalStrengthSync(): Int {
        return _signalStrength.value
    }

    private fun updateNetworkState(network: Network?) {
        network?.let { activeNetwork ->
            connectivityManager.getNetworkCapabilities(activeNetwork)?.let { capabilities ->
                _isNetworkAvailable.value = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                _isWifiConnected.value = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                _isCellularConnected.value = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                _isMeteredConnection.value = connectivityManager.isActiveNetworkMetered
                _isVpnConnected.value = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                _networkType.value = when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
                    else -> "Unknown"
                }
            }
        } ?: run {
            _isNetworkAvailable.value = false
            _isWifiConnected.value = false
            _isCellularConnected.value = false
            _isMeteredConnection.value = false
            _isVpnConnected.value = false
            _networkType.value = "None"
        }
    }
} 