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
import com.github.se.cyrcle.model.review.ReviewRepositoryFirestore
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.CyrcleTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.firestore.persistentCacheSettings

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

    val settings = firestoreSettings {
      // Use memory cache
      setLocalCacheSettings(memoryCacheSettings {})
      // Use persistent disk cache (default)
      setLocalCacheSettings(persistentCacheSettings {})
    }
    val db = Firebase.firestore
    db.firestoreSettings = settings

    setContent { CyrcleTheme { Surface(modifier = Modifier.fillMaxSize()) { CyrcleApp(db) } } }
  }

  @Composable
  fun CyrcleApp(db: FirebaseFirestore = Firebase.firestore) {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)
    val parkingRepository = ParkingRepositoryFirestore(db)
    val reviewRepository = ReviewRepositoryFirestore(db)
    val imageRepository = ImageRepositoryCloudStorage(auth)
    val parkingViewModel = ParkingViewModel(imageRepository, parkingRepository)
    val reviewViewModel = ReviewViewModel(reviewRepository)
    CyrcleNavHost(navigationActions, navController, parkingViewModel, reviewViewModel)
  }
}
