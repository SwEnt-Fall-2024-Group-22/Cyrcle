package com.github.se.cyrcle.ui.map

import androidx.compose.runtime.Composable
import com.github.se.cyrcle.model.map.MapViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle

object MapConfig {

  @Composable
  fun createMapViewPortStateFromViewModel(mapViewModel: MapViewModel): MapViewportState {
    return rememberMapViewportState {
      setCameraOptions {
        val camPos = mapViewModel.cameraPosition.value!!
        center(camPos.center)
        zoom(camPos.zoom)
        bearing(camPos.bearing)
        pitch(camPos.pitch)
      }
    }
  }

  fun defaultCameraState(): CameraState {
    return CameraState(
        Point.fromLngLat(6.566, 46.519), EdgeInsets(0.0, 0.0, 0.0, 0.0), 16.0, 0.0, 0.0)
  }

  @Composable
  fun DefaultStyle() {
    return MapStyle("mapbox://styles/seanprz/cm27wh9ff00jl01r21jz3hcb1")
  }
}
