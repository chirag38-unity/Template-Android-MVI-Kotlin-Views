package com.myapp.core.network.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class NetworkState {
    CONNECTED,
    DISCONNECTED,
    METERED,
}

interface NetworkMonitor {
    val isOnline: Boolean
    val networkState: Flow<NetworkState>
}

@Singleton
class AndroidNetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) : NetworkMonitor {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override val isOnline: Boolean
        get() {
            val network = connectivityManager.activeNetwork ?: return false
            val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

    override val networkState: Flow<NetworkState> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val caps = connectivityManager.getNetworkCapabilities(network)
                val state = when {
                    caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) == false ->
                        NetworkState.METERED
                    else -> NetworkState.CONNECTED
                }
                trySend(state)
            }

            override fun onLost(network: Network) {
                trySend(NetworkState.DISCONNECTED)
            }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
        trySend(if (isOnline) NetworkState.CONNECTED else NetworkState.DISCONNECTED)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
}
