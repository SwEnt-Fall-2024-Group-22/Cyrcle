package com.github.se.cyrcle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.github.se.cyrcle.model.parking.ImageRepositoryCloudStorage
import com.github.se.cyrcle.model.parking.ParkingRepositoryFirestore
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.authentication.SignInScreen
import com.github.se.cyrcle.ui.card.CardScreen
import com.github.se.cyrcle.ui.list.SpotListScreen
import com.github.se.cyrcle.ui.map.MapScreen
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.CyrcleTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {
  private lateinit var auth: FirebaseAuth

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize Firebase Auth
    auth = FirebaseAuth.getInstance()
    auth.currentUser?.let {
      // Sign out the user if they are already signed in
      auth.signOut()
    }

    setContent { CyrcleTheme { Surface(modifier = Modifier.fillMaxSize()) { CyrcleApp() } } }
  }

  @Composable
  fun CyrcleApp() {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)
    val firebase = ParkingRepositoryFirestore(Firebase.firestore)
    val firebase2 = ImageRepositoryCloudStorage(auth)
    val parkingViewModel = ParkingViewModel(firebase2, firebase)

    val db = Firebase.firestore
    val parkingRepository = ParkingRepositoryFirestore(db)
    val imageRepository = ImageRepositoryCloudStorage(auth)
    val parkingViewModel = ParkingViewModel(imageRepository, parkingRepository)

    NavHost(navController = navController, startDestination = Route.AUTH) {
      navigation(
          startDestination = Screen.AUTH,
          route = Route.AUTH,
      ) {
        composable(Screen.AUTH) { SignInScreen(navigationActions) }
      }

      navigation(
          startDestination = Screen.LIST,
          route = Route.LIST,
      ) {
        composable(Screen.LIST) { SpotListScreen(navigationActions, parkingViewModel) }
        composable(Screen.CARD) { CardScreen(navigationActions, parkingViewModel) }
      }

      navigation(
          startDestination = Screen.MAP,
          route = Route.MAP,
      ) {
        composable(Screen.MAP) { MapScreen(navigationActions, parkingViewModel) }
      }
    }
  }
}
