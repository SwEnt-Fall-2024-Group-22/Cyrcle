package com.github.se.cyrcle.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.End
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.theme.Cerulean
import com.github.se.cyrcle.ui.theme.CyrcleTheme
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.GenericStyle

@Composable
fun ZoomControls(onZoomIn: () -> Unit, onZoomOut: () -> Unit) {
  CyrcleTheme {
    Row(horizontalArrangement = End, modifier = Modifier.fillMaxWidth()) {
      Column(
          modifier =
              Modifier.padding(top = 35.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                  .background(Cerulean.copy(alpha = 0.75f), shape = RoundedCornerShape(8.dp))
                  .sizeIn(maxWidth = 35.dp)) {
            Box {
              IconButton(onClick = onZoomIn) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White)
              }
            }
            HorizontalDivider(thickness = 1.dp, color = Color.Gray)
            Box {
              IconButton(onClick = onZoomOut) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White)
              }
            }
          }
    }
  }
}

@Composable
fun MapScreen(navigationActions: NavigationActions) {
  val mapViewportState = rememberMapViewportState {
    setCameraOptions {
      zoom(16.0)
      center(Point.fromLngLat(6.566, 46.519))
      pitch(0.0)
      bearing(0.0)
    }
  }
  LaunchedEffect(mapViewportState.cameraState) {
    snapshotFlow { mapViewportState.cameraState?.center }
        .collect { centerPoint ->
          centerPoint?.let {
            println("Center Point: Latitude = ${it.latitude()}, Longitude = ${it.longitude()}")
          }
        }
  }
  Scaffold(
      bottomBar = {
        BottomNavigationBar(
            onTabSelect = { navigationActions.navigateTo(it) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.MAP)
      }) { padding ->
        MapboxMap(
            Modifier.fillMaxSize().padding(padding),
            mapViewportState = mapViewportState,
            style = { GenericStyle(style = "mapbox://styles/mapbox/light-v11") })

        Box {
          ZoomControls(
              onZoomIn = {
                mapViewportState.setCameraOptions {
                  zoom(mapViewportState.cameraState!!.zoom + 1.0)
                }
              },
              onZoomOut = {
                mapViewportState.setCameraOptions {
                  zoom(mapViewportState.cameraState!!.zoom - 1.0)
                }
              })
        }
      }
}
// Zoom controls preview
@Preview
@Composable
fun ZoomControlsPreview() {
  ZoomControls(onZoomIn = {}, onZoomOut = {})
}
