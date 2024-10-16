package com.github.se.cyrcle

import CyrcleNavHost
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.github.se.cyrcle.model.parking.ImageRepositoryCloudStorage
import com.github.se.cyrcle.model.parking.ParkingRepositoryFirestore
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
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
    val db = Firebase.firestore
    db.disableNetwork()
    val parkingRepository = ParkingRepositoryFirestore(db)
    val imageRepository = ImageRepositoryCloudStorage(auth)
    val parkingViewModel = ParkingViewModel(imageRepository, parkingRepository)
    CyrcleNavHost(navigationActions, navController, parkingViewModel)
  }
}
