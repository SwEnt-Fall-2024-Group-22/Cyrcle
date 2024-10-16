package com.github.se.cyrcle.ui.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.map.overlay.AddButton
import com.github.se.cyrcle.ui.map.overlay.ZoomControls
import com.github.se.cyrcle.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationSourceOptions
import com.mapbox.maps.plugin.annotation.ClusterOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener

const val defaultZoom = 16.0
const val maxZoom = 18.0
const val minZoom = 8.0

@Composable
fun MapScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    state: MutableState<Double> = remember { mutableStateOf(defaultZoom) }
) {

  val listOfParkings = parkingViewModel.rectParkings.collectAsState()

  val mapViewportState = rememberMapViewportState {
    setCameraOptions {
      zoom(defaultZoom)
      center(Point.fromLngLat(6.566, 46.519))
      pitch(0.0)
      bearing(0.0)
    }
  }

  val context = LocalContext.current

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
            style = { MapStyle("mapbox://styles/seanprz/cm27wh9ff00jl01r21jz3hcb1") }) {
              DisposableMapEffect { mapView ->

                // Set camera bounds options
                val cameraBoundsOptions =
                    CameraBoundsOptions.Builder().minZoom(minZoom).maxZoom(maxZoom).build()
                mapView.mapboxMap.setBounds(cameraBoundsOptions)

                // Create annotation manager
                val annotationManager =
                    mapView.annotations.createPointAnnotationManager(
                        AnnotationConfig(
                            annotationSourceOptions =
                                AnnotationSourceOptions(clusterOptions = ClusterOptions())))

                var PairScreenCoordinate = getScreenCoordinateFromPoint(mapView)

                // Load the red marker image and resized it to fit the map
                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.red_marker)
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 100, 150, false)

                // Get parkings in the current view
                parkingViewModel.getParkingsInRect(
                    PairScreenCoordinate.second, PairScreenCoordinate.first)

                // Create PointAnnotationOptions for each parking
                var pointAnnotationOptions =
                    listOfParkings.value.map { parking ->
                      PointAnnotationOptions()
                          .withPoint(parking.location.center)
                          .withIconImage(resizedBitmap)
                    }

                // This is a test code snippet that can uncommented for testing purposes as per the
                // PR description

                //                val parking1 = Point.fromLngLat(6.566, 46.519)
                //                val parking2 = Point.fromLngLat(6.500,46.500)
                //                val parking3 = Point.fromLngLat(6.600,46.530)
                //
                //                val listTest = listOf(parking1,parking2,parking3)
                //
                //                // Create PointAnnotationOptions for each parking with test tags
                //                val pointAnnotationOptions = listTest.mapIndexed { index, point ->
                //
                //                    PointAnnotationOptions()
                //                        .withPoint(point)
                //                        .withIconImage(resizedBitmap)
                //                }

                // Add annotations to the annotation manager and display them on the map
                pointAnnotationOptions.forEach { annotationManager.create(it) }

                // Add a camera change listener to detect zoom changes
                val cameraChangeListener = OnCameraChangeListener {
                  state.value = mapView.mapboxMap.cameraState.zoom

                  PairScreenCoordinate = getScreenCoordinateFromPoint(mapView)

                  parkingViewModel.getParkingsInRect(
                      PairScreenCoordinate.second, PairScreenCoordinate.first)

                  // Create PointAnnotationOptions for each parking
                  pointAnnotationOptions =
                      listOfParkings.value.map { parking ->
                        PointAnnotationOptions()
                            .withPoint(parking.location.center)
                            .withIconImage(resizedBitmap)
                      }

                  // Clear all annotations from the annotation manager to avoid duplicates
                  annotationManager.deleteAll()

                  // Add annotations to the annotation manager and display them on the map
                  pointAnnotationOptions.forEach { annotationManager.create(it) }
                }
                mapView.mapboxMap.addOnCameraChangeListener(cameraChangeListener)

                onDispose {
                  annotationManager.deleteAll()
                  mapView.mapboxMap.removeOnCameraChangeListener(cameraChangeListener)
                }
              }
            }

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
                    AddButton { navigationActions.navigateTo(Route.ADD_SPOTS) }
                  }
            }
      }
}

/**
 * Get the top right and bottom left coordinates of the current view
 *
 * @param mapView The MapView
 * @return A pair of the top right and bottom left coordinates
 */
fun getScreenCoordinateFromPoint(mapView: MapView): Pair<Point, Point> {

  // Retrieve viewport dimensions
  var viewportWidth = mapView.width
  var viewportHeight = mapView.height

  var centerPixel = mapView.mapboxMap.pixelForCoordinate(mapView.mapboxMap.cameraState.center)

  var topRightCorner =
      mapView.mapboxMap.coordinateForPixel(
          ScreenCoordinate(centerPixel.x + viewportWidth / 2, centerPixel.y - viewportHeight / 2))

  var bottomLeftCorner =
      mapView.mapboxMap.coordinateForPixel(
          ScreenCoordinate(centerPixel.x - viewportWidth / 2, centerPixel.y + viewportHeight / 2))

  return Pair(topRightCorner, bottomLeftCorner)
}
