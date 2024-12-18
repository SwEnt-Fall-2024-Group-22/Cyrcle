package com.github.se.cyrcle

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.github.se.cyrcle.model.CustomViewModelFactory
import com.github.se.cyrcle.model.address.AddressRepository
import com.github.se.cyrcle.model.address.AddressViewModel
import com.github.se.cyrcle.model.authentication.AuthenticationRepository
import com.github.se.cyrcle.model.image.ImageRepository
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.network.ConnectivityObserver
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.offline.OfflineParkingRepository
import com.github.se.cyrcle.model.parking.online.ParkingRepository
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.report.ReportedObjectViewModel
import com.github.se.cyrcle.model.review.ReviewRepository
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.UserRepository
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.permission.PermissionHandler
import com.github.se.cyrcle.sensor.ShakeDetector
import com.github.se.cyrcle.ui.grigris.SnowfallAnimation
import com.github.se.cyrcle.ui.grigris.snowToast
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

  @Inject lateinit var reportedObjectRepository: ReportedObjectRepository

  @Inject lateinit var permissionsHandler: PermissionHandler

  @Inject lateinit var authenticationRepository: AuthenticationRepository

  @Inject lateinit var offlineParkingRepository: OfflineParkingRepository

  private val reviewViewModel: ReviewViewModel by viewModels {
    CustomViewModelFactory { ReviewViewModel(reviewRepository, reportedObjectRepository) }
  }
  private val userViewModel: UserViewModel by viewModels {
    CustomViewModelFactory {
      UserViewModel(userRepository, parkingRepository, imageRepository, authenticationRepository)
    }
  }
  private val parkingViewModel: ParkingViewModel by viewModels {
    CustomViewModelFactory {
      ParkingViewModel(
          imageRepository,
          userViewModel,
          parkingRepository,
          offlineParkingRepository,
          reportedObjectRepository)
    }
  }
  private val reportedObjectViewModel: ReportedObjectViewModel by viewModels {
    CustomViewModelFactory { ReportedObjectViewModel(reportedObjectRepository) }
  }
  private val mapViewModel: MapViewModel by viewModels { CustomViewModelFactory { MapViewModel() } }
  private val addressViewModel: AddressViewModel by viewModels {
    CustomViewModelFactory { AddressViewModel(addressRepository) }
  }

  private var showSnow by mutableStateOf(false)
  private lateinit var shakeDetector: ShakeDetector

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    shakeDetector =
        ShakeDetector(this) {
          showSnow = !showSnow
          snowToast(showSnow, this)
        }

    permissionsHandler.initHandler(this@MainActivity)

    ConnectivityObserver(userViewModel, parkingViewModel).init(this)

    setContent {
      CyrcleTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          val navController = rememberNavController()
          val navigationActions = NavigationActions(navController)

          CyrcleNavHost(
              navigationActions,
              navController,
              parkingViewModel,
              reviewViewModel,
              userViewModel,
              mapViewModel,
              addressViewModel,
              reportedObjectViewModel,
              permissionsHandler)

          if (showSnow) SnowfallAnimation()
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    shakeDetector.start()
  }

  override fun onPause() {
    super.onPause()
    shakeDetector.stop()
  }
}
