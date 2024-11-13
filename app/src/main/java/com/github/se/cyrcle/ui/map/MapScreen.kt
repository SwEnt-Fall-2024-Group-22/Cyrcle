package com.github.se.cyrcle.ui.map

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.databinding.ItemCalloutViewBinding
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.Location
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.map.overlay.ZoomControls
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.IconButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar
import com.github.se.cyrcle.ui.theme.molecules.DropDownableEnum
import com.google.gson.Gson
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.common.Cancelable
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapIdleCallback
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
import com.mapbox.maps.plugin.annotation.generated.createPolygonAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.data.OverviewViewportStateOptions
import com.mapbox.maps.viewannotation.annotatedLayerFeature
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.turf.TurfMeasurement

const val defaultZoom = 16.0
const val maxZoom = 18.0
const val minZoom = 8.0
const val thresholdDisplayZoom = 13.0
const val LAYER_ID = "0128"
const val LABEL_THRESHOLD = 15.5
const val CLUSTER_COLORS = "#1A4988"

/**
 * Enum class to represent the different modes of the map
 *
 * @param description the description of the mode
 * @param isAdvancedMode true if the mode is advanced, false otherwise The advanced mode is the mode
 *   where the rectangles are displayed The simple mode is the mode where the markers are displayed
 */
enum class MapMode(override val description: String, val isAdvancedMode: Boolean) :
    DropDownableEnum {
  MARKERS("Simple", false),
  RECTANGLES("Advanced", true)
}

@Composable
fun MapScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    userViewModel: UserViewModel,
    mapViewModel: MapViewModel,
    zoomState: MutableState<Double> = remember { mutableDoubleStateOf(defaultZoom) },
    activity: Activity = LocalContext.current as Activity
) {

  val listOfParkings by parkingViewModel.rectParkings.collectAsState()
  val enableParkingAddition by userViewModel.isSignedIn.collectAsState(false)
  // create a remember  state to store if the markers or the rectangles are displayed
  val mapMode = remember { mutableStateOf(MapMode.MARKERS) }
  val mapViewportState = MapConfig.createMapViewPortStateFromViewModel(mapViewModel)
  var removeViewAnnotation = remember { true }
  var cancelables = remember { Cancelable {} }
  var listener: MapIdleCallback?
  var markerAnnotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }
  var rectangleAnnotationManager by remember { mutableStateOf<PolygonAnnotationManager?>(null) }
  var pLabelAnnotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }
  val selectedParking by parkingViewModel.selectedParking.collectAsState()
  val locationEnabled = PermissionsManager.areLocationPermissionsGranted(activity)
  // initialize the Gson object that will deserialize and serialize the parking to bind it to the
  // marker
  val gson = Gson()
  val screenCapacityString = stringResource(R.string.map_screen_capacity)
  val bitmap = BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.dot)
  val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, false)
  val alpha by
      animateFloatAsState(
          targetValue = if (zoomState.value > LABEL_THRESHOLD) 1f else 0f, label = "zoomAlpha")

  // Draw markers on the map when the list of parkings changes
  LaunchedEffect(
      listOfParkings, markerAnnotationManager, selectedParking?.nbReviews, mapMode.value) {
        pLabelAnnotationManager?.deleteAll()
        markerAnnotationManager?.deleteAll()
        rectangleAnnotationManager?.deleteAll()
        if (mapMode.value.isAdvancedMode)
            drawRectangles(
                rectangleAnnotationManager,
                pLabelAnnotationManager,
                listOfParkings.map { it.location })
        else drawMarkers(markerAnnotationManager, listOfParkings, resizedBitmap)
      }

  // Center the camera on th puck and transition to the follow puck state
  LaunchedEffect(PermissionsManager.areLocationPermissionsGranted(activity)) {
    mapViewportState.transitionToFollowPuckState(
        FollowPuckViewportStateOptions.Builder()
            .pitch(0.0)
            .zoom(maxZoom)
            .padding(EdgeInsets(100.0, 100.0, 100.0, 100.0))
            .build())
  }

  Scaffold(bottomBar = { BottomNavigationBar(navigationActions, selectedItem = Route.MAP) }) {
      padding ->
    MapboxMap(
        Modifier.fillMaxSize().padding(padding).testTag("MapScreen"),
        mapViewportState = mapViewportState,
        style = { MapConfig.DefaultStyle() }) {
          DisposableMapEffect { mapView ->

            // ======================= GLOBAL SETTINGS =======================
            // Disable the rotation gesture
            mapView.gestures.getGesturesManager().rotateGestureDetector.isEnabled = false

            // Set camera bounds options
            val cameraBoundsOptions =
                CameraBoundsOptions.Builder().minZoom(minZoom).maxZoom(maxZoom).build()
            mapView.mapboxMap.setBounds(cameraBoundsOptions)

            // Get parkings in the current view
            val (loadedBottomLeft, loadedTopRight) = mapViewModel.getScreenCorners(mapView)
            parkingViewModel.getParkingsInRect(loadedBottomLeft, loadedTopRight)

            // When map is loaded, check if the location permission is granted and initialize the
            // location component
            if (locationEnabled) mapViewModel.initLocationComponent(mapView)
            // ======================= GLOBAL SETTINGS =======================

            // ======================= ANNOTATIONS =======================
            // Create annotation manager to draw markers
            markerAnnotationManager =
                mapView.annotations.createPointAnnotationManager(
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
            // Create polygon annotation manager to draw rectangles
            rectangleAnnotationManager = mapView.annotations.createPolygonAnnotationManager()
            // Create point annotation manager to draw parking labels
            pLabelAnnotationManager =
                mapView.annotations.createPointAnnotationManager(AnnotationConfig())
            // ======================= ANNOTATIONS =======================

            // ======================= MOVE LISTENER =======================
            // Add a move listener to the map to deactivate tracking mode when the user moves the
            // map
            val moveListener =
                object : OnMoveListener {
                  override fun onMoveBegin(detector: MoveGestureDetector) {
                    if (mapViewModel.isTrackingModeEnable.value) {
                      mapViewportState.transitionToOverviewState(
                          OverviewViewportStateOptions.Builder()
                              .geometry(mapViewportState.cameraState!!.center)
                              .padding(EdgeInsets(100.0, 100.0, 100.0, 100.0))
                              .build())
                      mapViewModel.updateTrackingMode(false)
                    }
                  }

                  override fun onMove(detector: MoveGestureDetector): Boolean {
                    return false
                  }

                  override fun onMoveEnd(detector: MoveGestureDetector) {}
                }

            mapView.gestures.addOnMoveListener(moveListener)
            // ======================= MOVE LISTENER =======================

            // ======================= MARKERS CLICK =======================
            val viewAnnotationManager = mapView.viewAnnotationManager

            // Define the click  on the markers
            markerAnnotationManager?.addClickListener(
                OnPointAnnotationClickListener {
                  removeViewAnnotation = false
                  viewAnnotationManager.removeAllViewAnnotations()

                  // recenter the camera on the marker if it is not the case already
                  mapViewportState.setCameraOptions { center(it.point) }

                  // get the data from the PointAnnotation and deserialize it
                  val parkingData = it.getData()?.asJsonObject
                  val parkingDeserialized = gson.fromJson(parkingData, Parking::class.java)

                  val pointAnnotation = it

                  listener = MapIdleCallback {

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
                          screenCapacityString.format(parkingDeserialized.capacity.description)
                      selectButton.setOnClickListener {
                        parkingViewModel.selectParking(parkingDeserialized)
                        navigationActions.navigateTo(Screen.PARKING_DETAILS)
                      }
                    }
                    removeViewAnnotation = true
                  }
                  cancelables.cancel()
                  cancelables = mapView.mapboxMap.subscribeMapIdle(listener!!)
                  true
                })
            // ======================= MARKERS CLICK =======================

            // =======================  CAMERA  LISTENER  =======================
            // Add a camera change listener to detect zoom changes
            val cameraCancelable =
                mapView.mapboxMap.subscribeCameraChanged {

                  // Remove the view annotation if the user moves the map
                  if (removeViewAnnotation) {
                    viewAnnotationManager.removeAllViewAnnotations()
                    cancelables.cancel()
                  }

                  // Get the top right and bottom left coordinates of the current view only when
                  // what the user sees is outside the screen
                  val (currentBottomLeft, currentTopRight) = mapViewModel.getScreenCorners(mapView)

                  // Temporary fix to avoid loading too much parkings when zoomed out
                  if (mapView.mapboxMap.cameraState.zoom > thresholdDisplayZoom) {
                    parkingViewModel.getParkingsInRect(currentBottomLeft, currentTopRight)
                  }
                  if (mapView.mapboxMap.cameraState.zoom < LABEL_THRESHOLD &&
                      zoomState.value >= LABEL_THRESHOLD) {
                    mapMode.value = MapMode.MARKERS
                  }

                  // store the zoom level
                  zoomState.value = mapView.mapboxMap.cameraState.zoom
                }
            // =======================  CAMERA  LISTENER  =======================

            onDispose {
              markerAnnotationManager?.deleteAll()
              cancelables.cancel()
              cameraCancelable.cancel()
            }
          }
        }

    // ======================= OVERLAY =======================
    Box(modifier = Modifier.padding(padding).fillMaxSize()) {
      // A switch to change the display mode
      Column(
          modifier =
              Modifier.align(Alignment.TopStart).padding(start = 8.dp, top = 32.dp).alpha(alpha),
          horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Advanced Mode",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = mapMode.value.isAdvancedMode,
                onCheckedChange = {
                  mapMode.value = if (it) MapMode.RECTANGLES else MapMode.MARKERS
                },
                colors =
                    SwitchDefaults.colors()
                        .copy(
                            uncheckedTrackColor =
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        ))
          }

      ZoomControls(
          modifier = Modifier.align(Alignment.TopEnd),
          onZoomIn = {
            mapViewportState.setCameraOptions { zoom(mapViewportState.cameraState!!.zoom + 1.0) }
          },
          onZoomOut = {
            mapViewportState.setCameraOptions { zoom(mapViewportState.cameraState!!.zoom - 1.0) }
          })

      IconButton(
          icon = Icons.Default.MyLocation,
          contentDescription = "Recenter on Location",
          modifier =
              Modifier.align(if (locationEnabled) Alignment.BottomEnd else Alignment.TopEnd)
                  .alpha(if (locationEnabled) 1f else 0f)
                  .padding(bottom = 25.dp, end = 16.dp)
                  .scale(if (locationEnabled) 1.2f else 0.01f)
                  .testTag("recenterButton"),
          onClick = {
            mapViewModel.updateTrackingMode(true)
            mapViewportState.transitionToFollowPuckState(
                FollowPuckViewportStateOptions.Builder()
                    .pitch(0.0)
                    .zoom(maxZoom)
                    .padding(EdgeInsets(100.0, 100.0, 100.0, 100.0))
                    .build())
          },
          colorLevel =
              if (mapViewModel.isTrackingModeEnable.collectAsState().value) ColorLevel.SECONDARY
              else ColorLevel.PRIMARY)

      if (enableParkingAddition) {
        IconButton(
            icon = Icons.Default.Add,
            contentDescription = "Add parking spots",
            modifier =
                Modifier.align(Alignment.BottomStart)
                    .scale(1.2f)
                    .padding(bottom = 25.dp, start = 16.dp),
            onClick = {
              mapViewModel.updateCameraPosition(mapViewportState.cameraState!!)
              navigationActions.navigateTo(Route.ADD_SPOTS)
            },
            colorLevel = ColorLevel.PRIMARY,
            testTag = "addButton")
      }
    }
  }
}

/**
 * Draw the rectangles on the map
 *
 * @param polygonAnnotationManager the polygon annotation manager
 * @param locationList the list of locations to draw
 */
fun drawRectangles(
    polygonAnnotationManager: PolygonAnnotationManager?,
    plabelAnnotationManager: PointAnnotationManager?,
    locationList: List<Location>
) {
  val annotations: MutableList<PolygonAnnotationOptions> = mutableListOf()
  locationList.map { location ->
    val topLeft = location.topLeft
    val topRight = location.topRight
    val bottomLeft = location.bottomLeft
    val bottomRight = location.bottomRight
    if (topLeft != null && topRight != null && bottomLeft != null && bottomRight != null) {
      val polygon = location.toPolygon()

      val polygonAnnotationOptions =
          PolygonAnnotationOptions()
              .withGeometry(polygon)
              .withFillColor("#1A4988")
              .withFillOpacity(0.7)
      annotations.add(polygonAnnotationOptions)
      val area = TurfMeasurement.area(polygon)
      Log.d("MapScreen", "Area: $area")
      val labelAnnotationOption =
          PointAnnotationOptions()
              .withPoint(location.center)
              .withTextField("P")
              .withTextSize(if (area < 30) 5.0 else 10.0)
              .withTextColor("#FFFFFF")
              .withTextHaloColor("#FFFFFF")
              .withTextHaloWidth(0.2)
      plabelAnnotationManager?.create(labelAnnotationOption)
    }
  }
  polygonAnnotationManager?.create(annotations)
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
