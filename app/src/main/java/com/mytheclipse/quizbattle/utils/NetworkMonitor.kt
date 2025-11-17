package com.mytheclipse.quizbattle.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Network Monitor Utility
 * Monitors network connectivity status
 */
class NetworkMonitor(val context: Context) {
    
    private val connectivityManager = 
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * Flow that emits network connectivity status
     */
    val isConnected: Flow<Boolean> by lazy { observeNetworkStatus() }
    
    /**
     * Check if network is currently available
     * Uses modern NetworkCapabilities API (Android M+)
     */
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Observe network connectivity as Flow
     */
    private fun observeNetworkStatus(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }
            
            override fun onLost(network: Network) {
                trySend(false)
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val isConnected = networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) && networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )
                trySend(isConnected)
            }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(request, callback)
        
        // Send initial state
        trySend(isNetworkAvailable())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
    
    /**
     * Get network type
     */
    fun getNetworkType(): NetworkType {
        if (!isNetworkAvailable()) return NetworkType.NONE
        
        val capabilities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return NetworkType.NONE
            connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.NONE
        } else {
            return NetworkType.UNKNOWN
        }
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.MOBILE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            else -> NetworkType.UNKNOWN
        }
    }
    
    enum class NetworkType {
        NONE,
        WIFI,
        MOBILE,
        ETHERNET,
        UNKNOWN
    }
}

/**
 * Check if using metered connection (mobile data)
 */
fun NetworkMonitor.isMeteredConnection(): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return connectivityManager.isActiveNetworkMetered
}

/**
 * Network speed estimator
 */
fun NetworkMonitor.NetworkType.getEstimatedSpeed(): String {
    return when (this) {
        NetworkMonitor.NetworkType.WIFI -> "Fast"
        NetworkMonitor.NetworkType.MOBILE -> "Moderate"
        NetworkMonitor.NetworkType.ETHERNET -> "Very Fast"
        else -> "Unknown"
    }
}
