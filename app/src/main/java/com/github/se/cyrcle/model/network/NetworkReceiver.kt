package com.github.se.cyrcle.model.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class NetworkReceiver(context: Context) {
    // MutableStateFlow to keep track of the network connection status
  private val _isConnected = MutableStateFlow(false)
  val isConnected: MutableStateFlow<Boolean> = _isConnected

  private val connectivityManager =
      context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  private val networkCallback =
      object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
          Log.d("NetworkReceiver", "Network isConnected: ${isConnected.value}")
          _isConnected.value = true
        }

        override fun onLost(network: Network) {
          Log.d("NetworkReceiver", "Network lost")
          // Set isConnected to false when the network is lost
          _isConnected.value = false
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
          val isConnected =
              networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
          Log.d("NetworkReceiver", "Network capabilities changed, isConnected: $isConnected")
          _isConnected.value = isConnected
        }
      }

  fun register() {
    val networkRequest =
        NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
    connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
  }
}
