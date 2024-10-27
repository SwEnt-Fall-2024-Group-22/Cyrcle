package com.github.se.cyrcle.model.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.se.cyrcle.model.parking.Location
import com.github.se.cyrcle.ui.map.MapConfig
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ScreenCoordinate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MapViewModel : ViewModel() {

  private val _cameraPosition = MutableStateFlow<CameraState?>(MapConfig.defaultCameraState())
  val cameraPosition: StateFlow<CameraState?> = _cameraPosition

  private val _selectedLocation = MutableStateFlow<Location?>(null)
  val selectedLocation: StateFlow<Location?> = _selectedLocation

  private val _screenCoordinates = MutableStateFlow<List<ScreenCoordinate>>(listOf())
  val screenCoordinates: StateFlow<List<ScreenCoordinate>> = _screenCoordinates

  private val _locationPickerState = MutableStateFlow(LocationPickerState.NONE_SET)
  val locationPickerState: StateFlow<LocationPickerState> = _locationPickerState

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
