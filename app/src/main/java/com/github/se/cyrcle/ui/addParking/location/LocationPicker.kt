package com.github.se.cyrcle.ui.addParking.location

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.map.MapViewModel.LocationPickerState
import com.github.se.cyrcle.model.parking.Location
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.addParking.location.overlay.Crosshair
import com.github.se.cyrcle.ui.addParking.location.overlay.RectangleSelection
import com.github.se.cyrcle.ui.map.CLUSTER_COLORS
import com.github.se.cyrcle.ui.map.LAYER_ID
import com.github.se.cyrcle.ui.map.MapConfig
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationSourceOptions
import com.mapbox.maps.plugin.annotation.ClusterOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

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

  val bitmap = BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.dot)
  val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, false)

  // Mutable state to store the PointAnnotationManager for markers
  var markerAnnotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }

  // Collect the list of parkings from the ParkingViewModel as a state
  val listOfParkings by parkingViewModel.filteredRectParkings.collectAsState(emptyList())


// Draw the markers on the map when the list of parkings changes
  LaunchedEffect(listOfParkings) {
    mapViewModel.drawMarkers(markerAnnotationManager, listOfParkings, resizedBitmap)
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
            markerAnnotationManager =
                mapView.value!!
                    .annotations
                    .createPointAnnotationManager(
                        AnnotationConfig(
                            annotationSourceOptions =
                                AnnotationSourceOptions(
                                    clusterOptions =
                                        ClusterOptions(
                                            colorLevels =
                                                listOf(
                                                    Pair(
                                                        1,
                                                        android.graphics.Color.parseColor(
                                                            CLUSTER_COLORS))))),
                            layerId = LAYER_ID))
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
