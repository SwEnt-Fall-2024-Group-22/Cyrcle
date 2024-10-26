package com.github.se.cyrcle.ui.add.location

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
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.map.MapViewModel.LocationPickerState
import com.github.se.cyrcle.model.parking.Location
import com.github.se.cyrcle.ui.add.location.overlay.Crosshair
import com.github.se.cyrcle.ui.add.location.overlay.RectangleSelection
import com.github.se.cyrcle.ui.map.MapConfig
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.plugin.gestures.gestures

@Composable
fun LocationPicker(
    navigationActions: NavigationActions,
    mapViewModel: MapViewModel = MapViewModel()
) {
  LaunchedEffect(Unit) {
    mapViewModel.updateLocation(null)
    mapViewModel.updateLocationPickerState(LocationPickerState.NONE_SET)
  }
  val mapViewportState = MapConfig.createMapViewPortStateFromViewModel(mapViewModel)
  val mapView = remember { mutableStateOf<MapView?>(null) }
  val locationPickerState by mapViewModel.locationPickerState.collectAsState()
  Scaffold(
      bottomBar = { LocationPickerBottomBar(navigationActions, mapViewModel, mapView) },
      topBar = { LocationPickerTopBar(mapViewModel) }) { padding ->
        MapboxMap(
            mapViewportState = mapViewportState,
            style = { MapConfig.DefaultStyle() },
            modifier = Modifier.testTag("LocationPickerScreen").padding(padding),
        ) {
          DisposableMapEffect { mapViewInstance ->
            mapView.value = mapViewInstance
            onDispose {}
          }
        }
        RectangleSelection(mapViewModel, padding)
        Crosshair(mapViewModel, padding)
      }

  LaunchedEffect(locationPickerState) {
    val gestureEnabled = locationPickerState == LocationPickerState.NONE_SET
    mapView.value?.gestures?.scrollEnabled = gestureEnabled
    mapView.value?.gestures?.pinchToZoomEnabled = gestureEnabled
    mapView.value?.gestures?.pitchEnabled = gestureEnabled

    if (locationPickerState == LocationPickerState.RECTANGLE_SET) {
      val screenCoordsList = mapViewModel.screenCoordinates.value
      val pointsList = mapView.value?.mapboxMap?.coordinatesForPixels(screenCoordsList)!!
      val location = Location(pointsList)
      mapViewModel.updateLocation(location)
      navigationActions.navigateTo(Screen.ATTRIBUTES_PICKER)
    }
  }
}

fun onTopLeftSelected(mapViewModel: MapViewModel) {
  // freeze the map camera and create a rectangle composable on top of the map view
  mapViewModel.updateLocationPickerState(LocationPickerState.TOP_LEFT_SET)
}

fun onBottomRightSelected(
    mapViewModel: MapViewModel,
) {
  mapViewModel.updateLocationPickerState(LocationPickerState.BOTTOM_RIGHT_SET)
}
