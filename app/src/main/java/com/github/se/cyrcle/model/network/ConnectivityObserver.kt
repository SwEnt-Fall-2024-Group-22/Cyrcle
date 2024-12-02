package com.github.se.cyrcle.model.network

import android.content.Context
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.github.se.cyrcle.MainActivity
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ConnectivityObserver(
    private val userViewModel: UserViewModel,
    private val mapViewModel: MapViewModel,
    private val parkingViewModel: ParkingViewModel)
{
    private lateinit var networkReceiver: NetworkReceiver

    fun init(context: Context)  {
        initNetworkReceiver(context)
        initUserConnectionModeObserver()
    }
    /**
     * Initializes the network receiver and registers it
     * This will listen for network changes and update the state accordingly
     */
    private fun initNetworkReceiver(context: Context) {
        networkReceiver = NetworkReceiver(context)
        networkReceiver.register()
    }

    /**
     * Initializes the user connection mode observer
     * This will listen for changes in the user connection mode and update the viewmodels accordingly
     */
    private fun initUserConnectionModeObserver()  {
        CoroutineScope(Dispatchers.Default).launch {
            userViewModel.isOnlineMode.collect { isOnlineMode ->
                if (isOnlineMode)  onConnection()
                else  onDisconnection()
            }
        }
    }

    private fun onConnection()  {
        Log.d("ConnectivityObserver", "Switching to online mode")
        // mapviewmode.switchToOnlineMode()
        // parkingViewModel.switchToOnlineMode()
    }
    private fun onDisconnection()  {
        Log.d("ConnectivityObserver", "Switching to offline mode")
        // mapviewmode.switchToOfflineMode()
        // parkingViewModel.switchToOfflineMode()
    }
}