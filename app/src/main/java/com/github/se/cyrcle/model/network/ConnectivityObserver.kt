package com.github.se.cyrcle.model.network

import android.content.Context
import android.util.Log
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This class has two roles in our app :
 * - Listen for network changes and update the user viewmodel accordingly
 * - Listen for changes in the mode the user chooses (online/offline) and update the other
 *   viewmodels accordingly
 *
 * In that sense in the MVVM architecture, this class is behind the viewmodel layer.
 */
class ConnectivityObserver(
    private val userViewModel: UserViewModel,
    private val parkingViewModel: ParkingViewModel
) {
  private lateinit var networkReceiver: NetworkReceiver
  /** Initializes the Connectivity observer. */
  fun init(context: Context) {
    initNetworkReceiver(context)
    initUserConnectionModeObserver()
  }
  /**
   * Initializes the network receiver and registers it It Listen for network changes and update the
   * state accordingly
   */
  private fun initNetworkReceiver(context: Context) {
    networkReceiver = NetworkReceiver(context)
    networkReceiver.register()
    CoroutineScope(Dispatchers.Default).launch {
      networkReceiver.isConnected.collect {
        userViewModel.setHasConnection(it)
        Log.d("ConnectivityObserver", "User has connection : ${networkReceiver.isConnected.value}")
      }
    }
  }

  /**
   * Initializes the user connection mode observer This will listen for changes in the user
   * connection mode and update the viewmodels accordingly
   */
  private fun initUserConnectionModeObserver() {
    CoroutineScope(Dispatchers.Default).launch {
      userViewModel.isOnlineMode.collect { isOnlineMode ->
        if (isOnlineMode) onOnlineMode() else onOfflineMode()
      }
    }
  }

  /**
   * Switches the app to online mode by
   * - signing in the user anonymously
   * - switching the map and parking viewmodels to online mode (TODO)
   */
  private fun onOnlineMode() {
    Log.d("ConnectivityObserver", "Switching to online mode")
    userViewModel.signInAnonymously(
        onComplete = { Log.d("ConnectivityObserver", "User signed in anonymously") },
        onFailure = {
          Log.e(
              "ConnectivityObserver",
              "Failed to sign in user anonymously, going back to offline mode")
          userViewModel.setIsOnlineMode(false)
        })
    // mapviewmodel.switchToOnlineMode()
    parkingViewModel.switchToOnlineMode()
  }

  /**
   * Switches the app to offline mode by
   * - setting the current user to null
   * - switching the map and parking viewmodels to offline mode (Soon)
   */
  private fun onOfflineMode() {
    Log.d("ConnectivityObserver", "Switching to offline mode")
    userViewModel.setCurrentUser(null)
    // mapviewmodel.switchToOfflineMode()
    parkingViewModel.switchToOfflineMode()
  }
}
