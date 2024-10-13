package com.github.se.cyrcle.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.ui.map.overlay.AddButton
import com.github.se.cyrcle.ui.map.overlay.ZoomControls
import com.github.se.cyrcle.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle

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

  Scaffold(
      bottomBar = {
        BottomNavigationBar(
            onTabSelect = { navigationActions.navigateTo(it) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.MAP)
      }) { padding ->
        MapboxMap(
            Modifier.fillMaxSize().padding(padding).testTag("MapScreen"),
            mapViewportState = mapViewportState,
            style = { MapStyle("mapbox://styles/seanprz/cm27wh9ff00jl01r21jz3hcb1") })

        Column(
            Modifier.padding(padding).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween) {
              Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.End) {
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
              Row(
                  Modifier.padding(top = 16.dp).fillMaxWidth(),
                  horizontalArrangement = Arrangement.Start) {
                    AddButton { println("Add button clicked") }
                  }
            }
      }
}
