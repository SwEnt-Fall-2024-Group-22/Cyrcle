package com.github.se.cyrcle

import CyrcleNavHost
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.github.se.cyrcle.model.address.AddressRepository
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.ImageRepository
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.review.ReviewRepository
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.UserRepository
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.CyrcleTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @Inject lateinit var parkingRepository: ParkingRepository

  @Inject lateinit var reviewRepository: ReviewRepository

  @Inject lateinit var imageRepository: ImageRepository

  @Inject lateinit var userRepository: UserRepository

  @Inject lateinit var addressRepository: AddressRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val reviewViewModel = ReviewViewModel(reviewRepository)
    val parkingViewModel = ParkingViewModel(imageRepository, parkingRepository)
    val mapViewModel = MapViewModel()
    setContent {
      CyrcleTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          val navController = rememberNavController()
          val navigationActions = NavigationActions(navController)

          CyrcleNavHost(
              navigationActions, navController, parkingViewModel, reviewViewModel, mapViewModel)
        }
      }
    }
  }
}
