package com.github.se.cyrcle.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState

@Composable
fun MapScreen() {

  MapboxMap(
      Modifier.fillMaxSize(),
      mapViewportState =
          rememberMapViewportState {
            setCameraOptions {
              zoom(2.0)
              center(Point.fromLngLat(-98.0, 39.5))
              pitch(0.0)
              bearing(0.0)
            }
          },
  )
}
