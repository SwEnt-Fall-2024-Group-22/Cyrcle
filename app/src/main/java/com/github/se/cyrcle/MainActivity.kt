package com.github.se.cyrcle

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
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
import com.github.se.cyrcle.ui.grigris.SnowfallAnimation
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.CyrcleTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.sqrt

// Constants for the shake detection
const val SHAKE_THRESHOLD = 650
const val SHAKE_SLOP_TIME_MS = 100
const val SHAKE_COOLDOWN_MS = 2000

@AndroidEntryPoint
class MainActivity : ComponentActivity(), SensorEventListener {

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

  // Variables for the accelerometer sensor
  private lateinit var sensorManager: SensorManager
  private var accelerometer: Sensor? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    // Initialize the sensor manager and accelerometer sensor
    sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    permissionsHandler.initHandler(this@MainActivity)

    ConnectivityObserver(userViewModel, mapViewModel, parkingViewModel).init(this)

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

          // Show the snowfall animation if the `showSnow` flag is true
          if (showSnow) {
            SnowfallAnimation()
          }
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    // Register the accelerometer sensor listener
    accelerometer?.let {
      sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
    }
  }

  override fun onPause() {
    super.onPause()
    // Unregister the accelerometer sensor listener
    sensorManager.unregisterListener(this)
  }

  // Variables to keep track of the last x, y, z values and the last update time
  private var lastUpdate: Long = 0
  private var lastShakeTime: Long = 0
  private var lastX = 0f
  private var lastY = 0f
  private var lastZ = 0f

  override fun onSensorChanged(event: SensorEvent) {
    // Check if the sensor type is accelerometer
    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
      val curTime = System.currentTimeMillis()
      // Only allow one update after a certain time interval
      if ((curTime - lastUpdate) > SHAKE_SLOP_TIME_MS) {
        val diffTime = curTime - lastUpdate
        lastUpdate = curTime

        // Get the current x, y, z values from the accelerometer
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Calculate the speed of the shake
        val speed =
            sqrt(
                (x - lastX) * (x - lastX) + (y - lastY) * (y - lastY) + (z - lastZ) * (z - lastZ)) /
                diffTime * 10000

        // If the speed is greater than the threshold and cooldown has passed, consider it a shake
        if ((speed > SHAKE_THRESHOLD) && (curTime - lastShakeTime > SHAKE_COOLDOWN_MS)) {
          showSnow = !showSnow

          // Update toggle time after changing snow state
          lastShakeTime = curTime

          // Can't use string resources because we're not in a Composable
          if (showSnow) {
            Toast.makeText(this, "It's snowing ❄️! Shake again to stop", Toast.LENGTH_SHORT).show()
          } else {
            Toast.makeText(this, "The snow has stopped", Toast.LENGTH_SHORT).show()
          }
        }

        // Update the last x, y, z values
        lastX = x
        lastY = y
        lastZ = z
      }
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    // Not needed for this implementation. Still need to override it.
  }
}
