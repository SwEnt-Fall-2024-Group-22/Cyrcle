package com.github.se.cyrcle.model.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MapViewModel : ViewModel() {

  private val _cameraPosition =
      MutableStateFlow<CameraState?>(
          CameraState(Point.fromLngLat(0.0, 0.0), EdgeInsets(0.0, 0.0, 0.0, 0.0), 0.0, 0.0, 0.0))
  val cameraPosition: StateFlow<CameraState?> = _cameraPosition

  private val _selectedPoint = MutableStateFlow<Point?>(null)
  val selectedPoint: StateFlow<Point?> = _selectedPoint

  fun updateCameraPosition(cameraPosition: CameraState) {
    _cameraPosition.value = cameraPosition
  }

  fun updateSelectedPoint(point: Point?) {
    _selectedPoint.value = point
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
