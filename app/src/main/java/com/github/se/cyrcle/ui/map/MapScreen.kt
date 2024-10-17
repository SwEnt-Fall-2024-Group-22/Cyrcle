package com.github.se.cyrcle.ui.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.map.overlay.AddButton
import com.github.se.cyrcle.ui.map.overlay.ZoomControls
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
import com.mapbox.maps.plugin.gestures.gestures

const val defaultZoom = 16.0
const val maxZoom = 18.0
const val minZoom = 8.0

@Composable
fun MapScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    state: MutableState<Double> = remember { mutableDoubleStateOf(defaultZoom) }
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

  Scaffold(bottomBar = { BottomNavigationBar(navigationActions, selectedItem = Route.MAP) }) {
      padding ->
    MapboxMap(
        Modifier.fillMaxSize().padding(padding).testTag("MapScreen"),
        mapViewportState = mapViewportState,
        style = { MapStyle("mapbox://styles/seanprz/cm27wh9ff00jl01r21jz3hcb1") }) {
          DisposableMapEffect { mapView ->

            // Lock rotations of the map
            mapView.gestures.getGesturesManager().rotateGestureDetector.isEnabled = false

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

            var (loadedBottomLeft, loadedTopRight) = getScreenCorners(mapView, useBuffer = true)

            // Load the red marker image and resized it to fit the map
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.red_marker)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 100, 150, false)

            // Get parkings in the current view
            parkingViewModel.getParkingsInRect(loadedBottomLeft, loadedTopRight)

            // Create PointAnnotationOptions for each parking
            var pointAnnotationOptions =
                listOfParkings.value.map { parking ->
                  PointAnnotationOptions()
                      .withPoint(parking.location.center)
                      .withIconImage(resizedBitmap)
                }

            // Add annotations to the annotation manager and display them on the map
            pointAnnotationOptions.forEach { annotationManager.create(it) }

            // Add a camera change listener to detect zoom changes
            val cameraChangeListener = OnCameraChangeListener {
              state.value = mapView.mapboxMap.cameraState.zoom

              // Get the top right and bottom left coordinates of the current view only when
              // what the user sees is outside the screen
              val (currentBottomLeft, currentTopRight) =
                  getScreenCorners(mapView, useBuffer = false)
              if (!inBounds(currentBottomLeft, currentTopRight, loadedBottomLeft, loadedTopRight)) {
                Log.d("MapScreen", "Loading parkings in new view")
                // Get the buffered coordinates for loading parkings

                val loadedCorners = getScreenCorners(mapView, useBuffer = true)
                loadedBottomLeft = loadedCorners.first
                loadedTopRight = loadedCorners.second

                parkingViewModel.getParkingsInRect(loadedBottomLeft, loadedTopRight)

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
            }
            mapView.mapboxMap.addOnCameraChangeListener(cameraChangeListener)

            onDispose {
              annotationManager.deleteAll()
              mapView.mapboxMap.removeOnCameraChangeListener(cameraChangeListener)
            }
          }
        }

    Column(
        Modifier.padding(padding).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
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
 * Check if the current view is within the loaded view.
 *
 * @param currentBottomLeft the bottom left corner of the current view
 * @param currentTopRight the top right corner of the current view
 * @param loadedBottomLeft the bottom left corner of the loaded view
 * @param loadedTopRight the top right corner of the loaded view
 * @return true if the current view is within the loaded view, false otherwise
 */
private fun inBounds(
    currentBottomLeft: Point,
    currentTopRight: Point,
    loadedBottomLeft: Point,
    loadedTopRight: Point
): Boolean {
  return currentBottomLeft.latitude() >= loadedBottomLeft.latitude() &&
      currentBottomLeft.longitude() >= loadedBottomLeft.longitude() &&
      currentTopRight.latitude() <= loadedTopRight.latitude() &&
      currentTopRight.longitude() <= loadedTopRight.longitude()
}

/**
 * Get the bottom left and top right corners of the screen in latitude and longitude coordinates.
 * The corners are calculated based on the center of the screen and the viewport dimensions. If
 * useBuffer is true, the corners are calculated with a buffer of 2x the viewport dimensions. This
 * is useful for loading parkings that are not yet visible on the screen.
 *
 * @param mapView the MapView to get the screen corners from
 * @param useBuffer whether to use a buffer to get the corners
 * @return a pair of the bottom left and top right corners of the screen
 */
private fun getScreenCorners(mapView: MapView, useBuffer: Boolean = true): Pair<Point, Point> {
  // Retrieve viewport dimensions
  val viewportWidth = mapView.width
  val viewportHeight = mapView.height

  val centerPixel = mapView.mapboxMap.pixelForCoordinate(mapView.mapboxMap.cameraState.center)

  // Calculate the multiplier for the buffer
  val multiplier = if (useBuffer) 3.0 else 1.0

  val bottomLeftCorner =
      mapView.mapboxMap.coordinateForPixel(
          ScreenCoordinate(
              centerPixel.x - (viewportWidth * multiplier),
              centerPixel.y + (viewportHeight * multiplier)))

  val topRightCorner =
      mapView.mapboxMap.coordinateForPixel(
          ScreenCoordinate(
              centerPixel.x + (viewportWidth * multiplier),
              centerPixel.y - (viewportHeight * multiplier)))

  return Pair(bottomLeftCorner, topRightCorner)
}
