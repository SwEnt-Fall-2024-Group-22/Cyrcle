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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.Location
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.map.overlay.AddButton
import com.github.se.cyrcle.ui.map.overlay.ZoomControls
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.CameraChangedCallback
import com.mapbox.maps.MapView
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPolygonAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures

const val maxZoom = 18.0
const val minZoom = 8.0

@Composable
fun MapScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    mapViewModel: MapViewModel = MapViewModel(),
) {

    val listOfParkings by parkingViewModel.rectParkings.collectAsState()
    var polygonAnnotationManager by remember { mutableStateOf<PolygonAnnotationManager?>(null) }
    val mapViewportState = MapConfig.createMapViewPortStateFromViewModel(mapViewModel)

    Scaffold(bottomBar = { BottomNavigationBar(navigationActions, selectedItem = Route.MAP) }) {
            padding ->
        MapboxMap(
            Modifier.fillMaxSize().padding(padding).testTag("MapScreen"),
            mapViewportState = mapViewportState,
            style = { MapConfig.DefaultStyle() }) {
            DisposableMapEffect { mapView ->

                // Lock rotations of the map
                mapView.gestures.getGesturesManager().rotateGestureDetector.isEnabled = false
                // Set camera bounds options
                val cameraBoundsOptions =
                    CameraBoundsOptions.Builder().minZoom(minZoom).maxZoom(maxZoom).build()
                mapView.mapboxMap.setBounds(cameraBoundsOptions)

                // Create polygon annotation manager
                polygonAnnotationManager = mapView.annotations.createPolygonAnnotationManager()
                drawRectangles(polygonAnnotationManager, listOfParkings.map { it.location })
                var (loadedBottomLeft, loadedTopRight) = getScreenCorners(mapView, useBuffer = true)

                // Get parkings in the current view
                parkingViewModel.getParkingsInRect(loadedBottomLeft, loadedTopRight)

                // Add a camera change listener to detect zoom changes
                val cameraChangeListener = CameraChangedCallback {
                    // Get the top right and bottom left coordinates of the current view only when
                    // what the user sees is outside the screen
                    val (currentBottomLeft, currentTopRight) =
                        getScreenCorners(mapView, useBuffer = false)
                    if (!inBounds(currentBottomLeft, currentTopRight, loadedBottomLeft, loadedTopRight)) {
                        // Get the buffered coordinates for loading parkings
                        val loadedCorners = getScreenCorners(mapView, useBuffer = true)
                        loadedBottomLeft = loadedCorners.first
                        loadedTopRight = loadedCorners.second
                        parkingViewModel.getParkingsInRect(loadedBottomLeft, loadedTopRight)
                    }
                }
                mapView.mapboxMap.subscribeCameraChanged(cameraChangeListener)
                onDispose { polygonAnnotationManager?.deleteAll() }
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
                    navigationActions.navigateTo(Route.ADD_SPOTS)
                    mapViewModel.updateCameraPosition(mapViewportState.cameraState!!)
                }
            }
        }
    }
    LaunchedEffect(listOfParkings) {
        drawRectangles(polygonAnnotationManager, listOfParkings.map { it.location })
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
 * @param locationList the list of parkings location to draw
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
