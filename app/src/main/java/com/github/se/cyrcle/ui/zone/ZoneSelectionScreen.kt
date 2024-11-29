package com.github.se.cyrcle.ui.zone

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.map.MapViewModel.LocationPickerState
import com.github.se.cyrcle.ui.addParking.location.overlay.Crosshair
import com.github.se.cyrcle.ui.addParking.location.overlay.RectangleSelection
import com.github.se.cyrcle.ui.map.MapConfig
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.molecules.TopAppBar
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.plugin.gestures.gestures

/** Screen where users can select a new zone to download. */
@Composable
fun ZoneSelectionScreen(navigationActions: NavigationActions, mapViewModel: MapViewModel) {
  val mapView = remember { mutableStateOf<MapView?>(null) }
  val locationPickerState by mapViewModel.locationPickerState.collectAsState() // state representing where the user is in the location selection process
  mapViewModel.unZoomActualCamera() // automatically zoom out and reset bearing, because no one want to download a zone that is too zoomed in.
  val mapViewportState = MapConfig.createMapViewPortStateFromViewModel(mapViewModel)
  mapViewModel.updateLocation(null) // This is not related to the camera position.
  mapViewModel.updateLocationPickerState(LocationPickerState.NONE_SET) // reset the location picker state

  LaunchedEffect(locationPickerState) {
    if (locationPickerState == LocationPickerState.RECTANGLE_SET) {
      val screenCoordsList = mapViewModel.screenCoordinates.value
      val pointsList = mapView.value?.mapboxMap?.coordinatesForPixels(screenCoordsList)!!
      // handle the location submitted then navigate somewhere else?
        // FIXME
    }
  }
  Scaffold(
      topBar = {
        TopAppBar(navigationActions, title = stringResource(R.string.zone_selection_screen_title))
      }) { padding ->
        MapboxMap(
            mapViewportState = mapViewportState,
            style = { MapConfig.DefaultStyle() },
            modifier = Modifier.testTag("LocationPickerScreen").padding(padding),
        ) {
          DisposableMapEffect { mapViewInstance ->
            mapView.value = mapViewInstance
            mapViewInstance.gestures.getGesturesManager().rotateGestureDetector.isEnabled = false
            onDispose {}
          }
        }
        RectangleSelection(mapViewModel, padding, mapView.value, restrictSelectionSize = false)
        Crosshair(mapViewModel, padding)
      }
}

// Make a button that first say "Set top left corner" and then "Set bottom right corner" when clicked, then navigate to the next screen (or alert dialog), where the user name the zone. then he will finally click on download.


