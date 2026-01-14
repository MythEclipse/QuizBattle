package com.mytheclipse.quizbattle.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Sealed class representing network connectivity states
 */
sealed class NetworkState {
    data object Available : NetworkState()
    data object Unavailable : NetworkState()
    data object Losing : NetworkState()
    data object Lost : NetworkState()
    
    val isConnected: Boolean get() = this is Available
}

/**
 * Monitors network connectivity changes
 * Provides reactive network state using Flow
 */
class NetworkStateMonitor(context: Context) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
        as ConnectivityManager
    
    private val _networkState = MutableStateFlow(checkInitialState())
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _networkState.value = NetworkState.Available
        }
        
        override fun onLosing(network: Network, maxMsToLive: Int) {
            _networkState.value = NetworkState.Losing
        }
        
        override fun onLost(network: Network) {
            _networkState.value = NetworkState.Lost
        }
        
        override fun onUnavailable() {
            _networkState.value = NetworkState.Unavailable
        }
    }
    
    /**
     * Start monitoring network changes
     */
    fun startMonitoring() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }
    
    /**
     * Stop monitoring network changes
     */
    fun stopMonitoring() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Already unregistered
        }
    }
    
    /**
     * Check current network state
     */
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    private fun checkInitialState(): NetworkState {
        return if (isNetworkAvailable()) NetworkState.Available else NetworkState.Unavailable
    }
    
    companion object {
        @Volatile
        private var INSTANCE: NetworkStateMonitor? = null
        
        fun getInstance(context: Context): NetworkStateMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkStateMonitor(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}

/**
 * Extension function to observe network state as Flow
 */
fun Context.observeNetworkState(): Flow<NetworkState> = callbackFlow {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            trySend(NetworkState.Available)
        }
        
        override fun onLost(network: Network) {
            trySend(NetworkState.Lost)
        }
        
        override fun onUnavailable() {
            trySend(NetworkState.Unavailable)
        }
    }
    
    val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()
    
    connectivityManager.registerNetworkCallback(request, callback)
    
    // Emit initial state
    val initialState = if (connectivityManager.activeNetwork != null) {
        NetworkState.Available
    } else {
        NetworkState.Unavailable
    }
    trySend(initialState)
    
    awaitClose {
        connectivityManager.unregisterNetworkCallback(callback)
    }
}.distinctUntilChanged()
