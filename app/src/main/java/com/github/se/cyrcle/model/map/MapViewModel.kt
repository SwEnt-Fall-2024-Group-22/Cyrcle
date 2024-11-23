package com.github.se.cyrcle.model.map

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.cyrcle.model.parking.Location
import com.github.se.cyrcle.model.parking.PARKING_MAX_AREA
import com.github.se.cyrcle.model.parking.PARKING_MAX_SIDE_LENGTH
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.ui.map.MapConfig
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class MapViewModel : ViewModel() {

  private val _cameraPosition = MutableStateFlow<CameraState?>(MapConfig.defaultCameraState())
  val cameraPosition: StateFlow<CameraState?> = _cameraPosition

  private val _selectedLocation = MutableStateFlow<Location?>(null)
  val selectedLocation: StateFlow<Location?> = _selectedLocation
  val isLocationValid: Flow<Boolean>
    get() =
        selectedLocation.map {
          it?.let { location ->
            val area = location.computeArea()
            val heightAndWidth = location.computeHeightAndWidth()
            heightAndWidth.first <= PARKING_MAX_SIDE_LENGTH &&
                heightAndWidth.second <= PARKING_MAX_SIDE_LENGTH &&
                area <= PARKING_MAX_AREA
          } ?: false
        }

  private val _screenCoordinates = MutableStateFlow<List<ScreenCoordinate>>(listOf())
  val screenCoordinates: StateFlow<List<ScreenCoordinate>> = _screenCoordinates

  private val _locationPickerState = MutableStateFlow(LocationPickerState.NONE_SET)
  val locationPickerState: StateFlow<LocationPickerState> = _locationPickerState

  private val _isTrackingModeEnable = MutableStateFlow(true)
  val isTrackingModeEnable: StateFlow<Boolean> = _isTrackingModeEnable

  private val _userPosition = MutableStateFlow<Point>(TestInstancesParking.EPFLCenter)
  val userPosition: StateFlow<Point> = _userPosition

  /**
   * Update the state of the location picker, This state is used to determine which steps of the
   * process to set the new location are completed
   *
   * @param state the new state of the location picker (NONE_SET, TOP_LEFT_SET, BOTTOM_RIGHT_SET,
   *   RECTANGLE_SET)
   */
  fun updateLocationPickerState(state: LocationPickerState) {
    _locationPickerState.value = state
  }

  /**
   * Update the camera position
   *
   * @param cameraState the new camera state This function is used to preserve the map position when
   *   the user navigates between screens
   */
  fun updateCameraPosition(cameraState: CameraState) {
    _cameraPosition.value = cameraState
  }

  /**
   * Update the focus mode
   *
   * @param focusMode the new focus mode This function is used to update the focus mode of the map
   *   screen. The focus mode is used to center the map on the user's position
   */
  fun updateTrackingMode(focusMode: Boolean) {
    _isTrackingModeEnable.value = focusMode
  }

  /**
   * Update the camera position without bearing
   *
   * @param cameraState the new camera state This function is used to preserve the map position when
   *   the user navigates between screens, but without the bearing. As the add parking screen does
   *   support rotation, but the map screen does not
   */
  fun updateCameraPositionWithoutBearing(cameraState: CameraState) {
    val newCameraState =
        CameraState(cameraState.center, EdgeInsets(0.0, 0.0, 0.0, 0.0), cameraState.zoom, 0.0, 0.0)
    _cameraPosition.value = newCameraState
  }

  /**
   * Update the screen coordinates states
   *
   * @param screenCoordinates the new screen coordinates The ScreenCoordinates are used to draw a
   *   rectangle on the map when the user is selecting a parking area
   */
  fun updateScreenCoordinates(screenCoordinates: List<ScreenCoordinate>) {
    _screenCoordinates.value = screenCoordinates
  }

  /**
   * Update the selected location
   *
   * @param location the new location This function is used to store the selected location when the
   *   user is selecting a parking area
   */
  fun updateLocation(location: Location?) {
    _selectedLocation.value = location
  }

  /**
   * Update the user position
   *
   * @param position the new user position
   */
  fun updateUserPosition(position: Point) {
    _userPosition.value = position
  }

  /**
   * Get the bottom left and top right corners of the screen in latitude and longitude coordinates.
   * The corners are calculated based on the center of the screen and the viewport dimensions. If
   * useBuffer is true, the corners are calculated with a buffer of 2x the viewport dimensions. This
   * is useful for loading parkings that are not yet visible on the screen.
   *
   * @param mapView the MapView to get the screen corners from
   * @return a pair of the bottom left and top right corners of the screen
   */
  fun getScreenCorners(mapView: MapView): Pair<Point, Point> {
    // Retrieve viewport dimensions
    val viewportWidth = mapView.width
    val viewportHeight = mapView.height

    val centerPixel = mapView.mapboxMap.pixelForCoordinate(mapView.mapboxMap.cameraState.center)

    // Calculate the multiplier for the buffer
    val multiplier = 3.0

    val bottomLeftCorner =
        mapView.mapboxMap.coordinateForPixel(
            ScreenCoordinate(
                centerPixel.x - (viewportWidth * multiplier),
                centerPixel.y + (viewportHeight * multiplier)))

    val topRightCorner =
        mapView.mapboxMap.coordinateForPixel(
            ScreenCoordinate(
                centerPixel.x + (viewportWidth * multiplier),
                centerPixel.y - (viewportHeight * multiplier)))

    return Pair(bottomLeftCorner, topRightCorner)
  }

  private val handler = Handler(Looper.getMainLooper())
  private var lastUpdateTime = 0L
  private val updateInterval = 500L // 0.5 seconds

  /**
   * Initialize the location component of the map and add a listener to update the user position
   *
   * @param mapView the MapView to initialize the location component on
   */
  fun initLocationComponent(mapView: MapView) {
    val locationComponentPlugin = mapView.location
    locationComponentPlugin.updateSettings {
      this.enabled = true
      this.locationPuck = createDefault2DPuck(true)
    }

    locationComponentPlugin.addOnIndicatorPositionChangedListener { point ->
      val currentTime = System.currentTimeMillis()
      if (currentTime - lastUpdateTime >= updateInterval) {
        handler.post {
          updateUserPosition(point)
          lastUpdateTime = currentTime
        }
      }
    }
  }

  /**
   * Enum class to represent the state of the location picker, This state is used to determine which
   * steps of the process to set the new location are completed NONE_SET: No location is set
   * TOP_LEFT_SET: The top left corner of the rectangle is set BOTTOM_RIGHT_SET: The bottom right
   * corner of the rectangle is set RECTANGLE_SET: All corners of the rectangle are set
   */
  enum class LocationPickerState {
    NONE_SET,
    TOP_LEFT_SET,
    BOTTOM_RIGHT_SET,
    RECTANGLE_SET
  }

  // create factory (imported from bootcamp)
  // create factory (imported from bootcamp)
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MapViewModel() as T
          }
        }
  }
}
