package com.github.se.cyrcle.model.map

import android.annotation.SuppressLint
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import org.junit.Test

class MapViewModelTest {

  @SuppressLint("StateFlowValueCalledInComposition")
  @Test
  fun MapViewModelTestCameraState() {
    val mapViewModel = MapViewModel()

    val cameraState =
        CameraState(Point.fromLngLat(6.566, 46.519), EdgeInsets(0.0, 0.0, 0.0, 0.0), 17.0, 0.0, 0.0)

    mapViewModel.updateCameraPosition(cameraState)
    val returnedCamera = mapViewModel.cameraPosition.value
    assert(returnedCamera?.zoom == 17.0)
    assert(returnedCamera?.center?.latitude() == 46.519)
    assert(returnedCamera?.center?.longitude() == 6.566)
    assert(returnedCamera?.pitch == 0.0)
    assert(returnedCamera?.bearing == 0.0)
  }
}
