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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.databinding.ItemCalloutViewBinding
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.Location
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.map.overlay.AddButton
import com.github.se.cyrcle.ui.map.overlay.ZoomControls
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar
import com.google.gson.Gson
import com.mapbox.common.Cancelable
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.MapIdleCallback
import com.mapbox.maps.MapView
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationSourceOptions
import com.mapbox.maps.plugin.annotation.ClusterOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.viewannotation.annotatedLayerFeature
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions

const val defaultZoom = 16.0
const val maxZoom = 18.0
const val minZoom = 8.0
const val LAYER_ID = "0128"

@Composable
fun MapScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    mapViewModel: MapViewModel,
    zoomState: MutableState<Double> = remember { mutableDoubleStateOf(defaultZoom) }
) {

  val listOfParkings by parkingViewModel.rectParkings.collectAsState()
  val mapViewportState = MapConfig.createMapViewPortStateFromViewModel(mapViewModel)
  var removeViewAnnotation = remember { true }
  var cancelables = remember<List<Cancelable>> { mutableListOf() }
  var pointAnnotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }

  val bitmap = BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.red_marker)
  val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 100, 150, false)
  // Draw markers on the map when the list of parkings changes
  LaunchedEffect(listOfParkings, pointAnnotationManager) {
    drawMarkers(pointAnnotationManager, listOfParkings, resizedBitmap)
  }

  Scaffold(bottomBar = { BottomNavigationBar(navigationActions, selectedItem = Route.MAP) }) {
      padding ->
    MapboxMap(
        Modifier.fillMaxSize().padding(padding).testTag("MapScreen"),
        mapViewportState = mapViewportState,
        style = { MapConfig.DefaultStyle() }) {
          DisposableMapEffect { mapView ->
            val viewAnnotationManager = mapView.viewAnnotationManager

            // initialize the Gson object that will deserialize and serialize the parking to bind it
            // to the marker
            val gson = Gson()

            // Lock rotations of the map
            mapView.gestures.getGesturesManager().rotateGestureDetector.isEnabled = false

            // Set camera bounds options
            val cameraBoundsOptions =
                CameraBoundsOptions.Builder().minZoom(minZoom).maxZoom(maxZoom).build()
            mapView.mapboxMap.setBounds(cameraBoundsOptions)

            // Create annotation manager
            pointAnnotationManager =
                mapView.annotations.createPointAnnotationManager(
                    AnnotationConfig(
                        annotationSourceOptions =
                            AnnotationSourceOptions(clusterOptions = ClusterOptions()),
                        layerId = LAYER_ID))

            pointAnnotationManager?.addClickListener(
                OnPointAnnotationClickListener {
                  removeViewAnnotation = false
                  viewAnnotationManager.removeAllViewAnnotations()

                  // recenter the camera on the marker if it is not the case already
                  mapViewportState.setCameraOptions { center(it.point) }

                  // get the data from the PointAnnotation and deserialize it
                  val parkingData = it.getData()?.asJsonObject
                  val parking_deserialized = gson.fromJson(parkingData, Parking::class.java)

                  val pointAnnotation = it

                  val listener = MapIdleCallback {

                    // Add the new view annotation
                    val viewAnnotation =
                        viewAnnotationManager.addViewAnnotation(
                            resId = R.layout.item_callout_view,
                            options =
                                viewAnnotationOptions {
                                  annotatedLayerFeature(LAYER_ID) {
                                    featureId(pointAnnotation.id)
                                    geometry(pointAnnotation.geometry)
                                    annotationAnchor {
                                      anchor(ViewAnnotationAnchor.BOTTOM)
                                      offsetY(
                                          (pointAnnotation.iconImageBitmap?.height!!.toDouble()))
                                    }
                                  }
                                })

                    // Set the text and the button of the view annotation
                    ItemCalloutViewBinding.bind(viewAnnotation).apply {
                      textNativeView.text =
                          "Capacity is ${parking_deserialized.capacity.description}"
                      selectButton.setOnClickListener {
                        parkingViewModel.selectParking(parking_deserialized)
                        navigationActions.navigateTo(Screen.CARD)
                      }
                    }
                    removeViewAnnotation = true
                  }
                  cancelables.forEach(Cancelable::cancel)
                  cancelables = mutableListOf(mapView.mapboxMap.subscribeMapIdle(listener))
                  true
                })

            var (loadedBottomLeft, loadedTopRight) = getScreenCorners(mapView, useBuffer = true)

            // Get parkings in the current view
            parkingViewModel.getParkingsInRect(loadedBottomLeft, loadedTopRight)
            // Add a camera change listener to detect zoom changes
            val cameraCancelable =
                mapView.mapboxMap.subscribeCameraChanged {
                  // store the zoom level
                  zoomState.value = mapView.mapboxMap.cameraState.zoom

                  // Remove the view annotation if the user moves the map
                  if (removeViewAnnotation) {
                    viewAnnotationManager.removeAllViewAnnotations()
                    cancelables.forEach(Cancelable::cancel)
                  }

                  // Get the top right and bottom left coordinates of the current view only when
                  // what the user sees is outside the screen
                  val (currentBottomLeft, currentTopRight) =
                      getScreenCorners(mapView, useBuffer = false)
                  if (!inBounds(
                      currentBottomLeft, currentTopRight, loadedBottomLeft, loadedTopRight)) {
                    Log.d("MapScreen", "Loading parkings in new view")
                    // Get the buffered coordinates for loading parkings

                    val loadedCorners = getScreenCorners(mapView, useBuffer = true)
                    loadedBottomLeft = loadedCorners.first
                    loadedTopRight = loadedCorners.second

                    parkingViewModel.getParkingsInRect(loadedBottomLeft, loadedTopRight)

                    // Create PointAnnotationOptions for each parking
                    drawMarkers(pointAnnotationManager, listOfParkings, resizedBitmap)
                  }
                }

            onDispose {
              pointAnnotationManager?.deleteAll()
              cancelables.forEach(Cancelable::cancel)
              cameraCancelable.cancel()
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
                AddButton {
                  mapViewModel.updateCameraPosition(mapViewportState.cameraState!!)
                  navigationActions.navigateTo(Route.ADD_SPOTS)
                }
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

/**
 * Draw the rectangles on the map
 *
 * @param polygonAnnotationManager the polygon annotation manager
 * @param locationList the list of locations to draw
 */
fun drawRectangles(
    polygonAnnotationManager: PolygonAnnotationManager?,
    locationList: List<Location>
) {
  polygonAnnotationManager?.deleteAll()
  locationList.map { location ->
    val topLeft = location.topLeft
    val topRight = location.topRight
    val bottomLeft = location.bottomLeft
    val bottomRight = location.bottomRight
    if (topLeft != null && topRight != null && bottomLeft != null && bottomRight != null) {
      val polygon =
          Polygon.fromLngLats(listOf(listOf(topLeft, topRight, bottomRight, bottomLeft, topLeft)))
      val polygonAnnotationOptions =
          PolygonAnnotationOptions()
              .withGeometry(polygon)
              .withFillColor("#22799B")
              .withFillOpacity(0.7)
      polygonAnnotationManager?.create(polygonAnnotationOptions)
    }
  }
}
/**
 * Draw markers on the map.
 *
 * @param pointAnnotationManager the PointAnnotationManager to draw the markers on
 * @param parkingList the list of parkings to draw
 * @param bitmap the bitmap to use for the markers
 */
private fun drawMarkers(
    pointAnnotationManager: PointAnnotationManager?,
    parkingList: List<Parking>,
    bitmap: Bitmap,
) {
  pointAnnotationManager?.deleteAll()
  parkingList.forEach {
    pointAnnotationManager?.create(
        PointAnnotationOptions()
            .withPoint(it.location.center)
            .withIconImage(bitmap)
            .withIconAnchor(IconAnchor.BOTTOM)
            .withIconOffset(listOf(0.0, bitmap.height / 12.0))
            .withData(Gson().toJsonTree(it)))
  }
}
