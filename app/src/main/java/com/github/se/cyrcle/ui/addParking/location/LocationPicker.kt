package com.github.se.cyrcle.ui.addParking.location

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.map.MapViewModel.LocationPickerState
import com.github.se.cyrcle.model.parking.Location
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.addParking.location.overlay.Crosshair
import com.github.se.cyrcle.ui.addParking.location.overlay.RectangleSelection
import com.github.se.cyrcle.ui.map.MapConfig
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolygonAnnotationManager

@Composable
fun LocationPicker(
    navigationActions: NavigationActions,
    mapViewModel: MapViewModel = MapViewModel(),
    parkingViewModel: ParkingViewModel,
) {
  // Reset the location and location picker state when the screen is launched
  LaunchedEffect(Unit) {
    mapViewModel.updateLocation(null)
    mapViewModel.updateLocationPickerState(LocationPickerState.NONE_SET)
  }
  val mapViewportState = MapConfig.createMapViewPortStateFromViewModel(mapViewModel)
  val mapView = remember { mutableStateOf<MapView?>(null) }
  val locationPickerState by mapViewModel.locationPickerState.collectAsState()

  // Mutable state to store the PolygonAnnotationManager for rectangles
  var rectangleAnnotationManager by remember { mutableStateOf<PolygonAnnotationManager?>(null) }

  // Mutable state to store the PointAnnotationManager for parking labels
  var pLabelAnnotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }

  // Collect the list of parkings from the ParkingViewModel as a state
  val listOfParkings by parkingViewModel.rectParkings.collectAsState(emptyList())

  // Draw the markers on the map when the list of parkings changes
  LaunchedEffect(listOfParkings) {
    mapViewModel.drawRectangles(rectangleAnnotationManager, pLabelAnnotationManager, listOfParkings)
  }

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

            // Create annotation manager to draw markers
            rectangleAnnotationManager =
                mapView.value!!
                    .annotations
                    .createPolygonAnnotationManager(annotationConfig = AnnotationConfig())

            // Create point annotation manager to draw parking labels
            pLabelAnnotationManager =
                mapView.value!!.annotations.createPointAnnotationManager(AnnotationConfig())

            onDispose {}
          }
        }
        RectangleSelection(mapViewModel, padding, mapView.value)
        Crosshair(mapViewModel, padding)
      }
  LaunchedEffect(locationPickerState) {
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
