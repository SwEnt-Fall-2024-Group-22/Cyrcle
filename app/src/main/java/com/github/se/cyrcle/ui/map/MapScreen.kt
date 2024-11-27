package com.github.se.cyrcle.ui.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.databinding.ItemCalloutViewBinding
import com.github.se.cyrcle.model.address.AddressViewModel
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.permission.PermissionHandler
import com.github.se.cyrcle.ui.list.ActionCard
import com.github.se.cyrcle.ui.map.overlay.ZoomControls
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.Black
import com.github.se.cyrcle.ui.theme.Cerulean
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.White
import com.github.se.cyrcle.ui.theme.atoms.FloatingActionButton
import com.github.se.cyrcle.ui.theme.atoms.IconButton
import com.github.se.cyrcle.ui.theme.atoms.SmallFloatingActionButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.disabledColor
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar
import com.google.gson.Gson
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.StyleLoadedCallback
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationSourceOptions
import com.mapbox.maps.plugin.annotation.ClusterOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.OnPolygonAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolygonAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.scalebar.ScaleBarPlugin
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.data.OverviewViewportStateOptions
import com.mapbox.maps.viewannotation.annotatedLayerFeature
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import kotlinx.coroutines.runBlocking

const val defaultZoom = 16.0
const val maxZoom = 18.0
const val minZoom = 8.0
const val thresholdDisplayZoom = 13.0
const val LAYER_ID = "0128"
const val LAYER_ID_RECT = "0129"
const val ADVANCED_MODE_ZOOM_THRESHOLD = 15.5
const val CLUSTER_COLORS = "#1A4988"



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navigationActions: NavigationActions,
    parkingViewModel: ParkingViewModel,
    userViewModel: UserViewModel,
    mapViewModel: MapViewModel,
    permissionHandler: PermissionHandler,
    addressViewModel: AddressViewModel,
    zoomState: MutableState<Double> = remember { mutableDoubleStateOf(defaultZoom) }
) {

  // Collect the list of parkings from the ParkingViewModel as a state
  val listOfParkings by parkingViewModel.rectParkings.collectAsState()

  // Create a remember state to store the search query of the search bar as a mutable state
  val searchQuery = remember { mutableStateOf("") }

  // Collect the boolean to enable the parking addition from the UserViewModel as a state
  val enableParkingAddition by userViewModel.isSignedIn.collectAsState(false)

  // create a remember  state to store if the markers or the rectangles are displayed
  val mapMode: State<MapViewModel.MapMode> = mapViewModel.mapMode.collectAsState()

  // this is the Map mode the user selected
  val userMapMode: State<MapViewModel.MapMode> = mapViewModel.userMapMode.collectAsState()

  // Create the viewport state from the MapViewModel
  val mapViewportState = MapConfig.createMapViewPortStateFromViewModel(mapViewModel)

  // Mutable state to store the PointAnnotationManager for markers
  var markerAnnotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }

  // Mutable state to store the PolygonAnnotationManager for rectangles
  var rectangleAnnotationManager by remember { mutableStateOf<PolygonAnnotationManager?>(null) }

  // Mutable state to store the PointAnnotationManager for parking labels
  var pLabelAnnotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }

  // Collect the selected parking from the ParkingViewModel as a state
  val selectedParking by parkingViewModel.selectedParking.collectAsState()

  // Collect the location permission status as a state from the PermissionHandler
  val locationEnabled by permissionHandler.getLocalisationPerm().collectAsState()

  // Prevent the camera from recentering when the user exit and come back to the map by disabling
  // the related launchedEffect
  val cameraRecentering = mapViewModel.cameraRecentering.collectAsState()

    // Mutable state to store the visibility of the settings menu
  val showSettings = remember { mutableStateOf(false) }

    // Initialize FocusManager
    val focusManager = LocalFocusManager.current

    // Initialize the keyboard controller
    val virtualKeyboardManager = LocalSoftwareKeyboardController.current

    // List of suggestions from NominatimAPI
    val listOfSuggestions = addressViewModel.addressList.collectAsState()


    // Show suggestions screen
    val showSuggestions = remember { mutableStateOf(false) }

    // Chosen location by the user
    val chosenLocation = addressViewModel.address.collectAsState()


//  // Get the virtual keyboard controller
//  val virtualKeyboard : SoftwareKeyboardController = LocalSoftwareKeyboardController.current

  // initialize the Gson object that will deserialize and serialize the parking to bind it to the
  // marker
  val gson = Gson()
  val screenCapacityString = stringResource(R.string.map_screen_capacity)
  val bitmap = BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.dot)
  val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, false)
  val alpha by
      animateFloatAsState(
          targetValue = if (zoomState.value > ADVANCED_MODE_ZOOM_THRESHOLD) 1f else 0f,
          label = "zoomAlpha")

  // Draw markers on the map when the list of parkings changes
  LaunchedEffect(
      listOfParkings, markerAnnotationManager, selectedParking?.nbReviews, mapMode.value) {
        pLabelAnnotationManager?.deleteAll()
        markerAnnotationManager?.deleteAll()
        rectangleAnnotationManager?.deleteAll()
        if (mapMode.value.isAdvancedMode)
            mapViewModel.drawRectangles(
                rectangleAnnotationManager, pLabelAnnotationManager, listOfParkings)
        else mapViewModel.drawMarkers(markerAnnotationManager, listOfParkings, resizedBitmap)
      }

  // Center the camera on th puck and transition to the follow puck state. Update the user
  // position to the center of the camera
  LaunchedEffect(locationEnabled) {
    if (!cameraRecentering.value) {
      mapViewportState.transitionToFollowPuckState(
          FollowPuckViewportStateOptions.Builder()
              .pitch(0.0)
              .zoom(maxZoom)
              .padding(EdgeInsets(100.0, 100.0, 100.0, 100.0))
              .build()) {
            mapViewportState.cameraState?.let { mapViewModel.updateUserPosition(it.center) }
            mapViewModel.updateMapRecentering(true)
          }
    }
  }

  Scaffold(
      bottomBar = {
        BottomNavigationBar(
            navigationActions,
            selectedItem = Route.MAP,
            onTabSelect = {
              mapViewModel.updateCameraPosition(mapViewportState.cameraState!!)
              navigationActions.navigateTo(it)
            })
      }) { padding ->
        MapboxMap(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("MapScreen"),
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

                // When map is loaded, check if the location permission is granted and initialize
                // the
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
                rectangleAnnotationManager =
                    mapView.annotations.createPolygonAnnotationManager(
                        annotationConfig = AnnotationConfig().copy(layerId = LAYER_ID_RECT))
                // Create point annotation manager to draw parking labels
                pLabelAnnotationManager =
                    mapView.annotations.createPointAnnotationManager(AnnotationConfig())
                // ======================= ANNOTATIONS =======================

                val viewAnnotationManager = mapView.viewAnnotationManager
                // upload the view annotation manager to the view model
                mapViewModel.updateViewAnnotationManager(viewAnnotationManager)
                // ======================= MOVE LISTENER =======================
                // Add a move listener to the map to deactivate tracking mode when the user moves
                // the
                // map
                val moveListener =
                    object : OnMoveListener {
                      override fun onMoveBegin(detector: MoveGestureDetector) {
                        // Remove any preview card displayed
                        mapViewModel.removePreviewCard()

                        // remove focus on the Search Bar
                        focusManager.clearFocus()
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
                markerAnnotationManager?.addClickListener(
                    OnPointAnnotationClickListener { pointAnnotation ->
                      mapViewModel.removePreviewCard()
                      // get the data from the PointAnnotation and deserialize it
                      val parkingData = pointAnnotation.getData()?.asJsonObject
                      val parkingDeserialized = gson.fromJson(parkingData, Parking::class.java)
                      // move the camera to the selected parking
                      mapViewportState.setCameraOptions {
                        center(parkingDeserialized.location.center)
                      }

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
                      true
                    })
                // ======================= MARKERS CLICK =======================

                // ======================= RECTANGLES CLICK =======================
                rectangleAnnotationManager?.addClickListener(
                    OnPolygonAnnotationClickListener {
                      // Remove any preview card displayed
                      mapViewModel.removePreviewCard()
                      // get the data from the PointAnnotation and deserialize it
                      val parkingData = it.getData()?.asJsonObject
                      val parkingDeserialized = gson.fromJson(parkingData, Parking::class.java)
                      // move the camera to the selected parking
                      mapViewportState.setCameraOptions {
                        center(parkingDeserialized.location.center)
                      }

                      // Add the new view annotation
                      val viewAnnotation =
                          viewAnnotationManager.addViewAnnotation(
                              resId = R.layout.item_callout_view,
                              options =
                                  viewAnnotationOptions {
                                    annotatedLayerFeature(LAYER_ID_RECT) {
                                      featureId(it.id)
                                      geometry(it.geometry)
                                      annotationAnchor {
                                        anchor(ViewAnnotationAnchor.BOTTOM)
                                        offsetY(0.0)
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
                      true
                    })
                // ======================= RECTANGLES CLICK =======================

                // =======================  CAMERA  LISTENER  =======================
                mapView.mapboxMap.subscribeCameraChanged {

                  // Get the top right and bottom left coordinates of the current view only when
                  // what the user sees is outside the screen
                  val (currentBottomLeft, currentTopRight) = mapViewModel.getScreenCorners(mapView)

                  // Temporary fix to avoid loading too much parkings when zoomed out
                  if (mapView.mapboxMap.cameraState.zoom > thresholdDisplayZoom) {
                    parkingViewModel.getParkingsInRect(currentBottomLeft, currentTopRight)
                  }
                  // On zoom-out past the threshold, switch to the markers mode
                  if (mapView.mapboxMap.cameraState.zoom < ADVANCED_MODE_ZOOM_THRESHOLD &&
                      zoomState.value >= ADVANCED_MODE_ZOOM_THRESHOLD) {
                    mapViewModel.updateMapMode(MapViewModel.MapMode.MARKERS)
                  }
                  // On zoomin in past the threshold, switch to the user's selected mode
                  if (mapView.mapboxMap.cameraState.zoom >= ADVANCED_MODE_ZOOM_THRESHOLD &&
                      zoomState.value < ADVANCED_MODE_ZOOM_THRESHOLD) {
                    mapViewModel.updateMapMode(userMapMode.value)
                  }

                  // store the zoom level
                  // This must stay at the end of the listener.
                  zoomState.value = mapView.mapboxMap.cameraState.zoom
                }
                // =======================  CAMERA  LISTENER  =======================

                onDispose {
                  pLabelAnnotationManager?.deleteAll()
                  rectangleAnnotationManager?.deleteAll()
                  markerAnnotationManager?.deleteAll()
                  mapViewModel.removePreviewCard()
                  mapViewModel.updateViewAnnotationManager(null)
                }
              }
            }

        // ======================= OVERLAY =======================
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
          // A switch to change the display mode

          if (locationEnabled) {
            IconButton(
                icon = Icons.Default.MyLocation,
                contentDescription = "Recenter on Location",
                modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 25.dp, end = 16.dp)
                    .scale(1.2f)
                    .testTag("recenterButton"),
                onClick = {
                  mapViewModel.removePreviewCard()
                  mapViewModel.updateTrackingMode(true)
                  mapViewportState.transitionToFollowPuckState(
                      FollowPuckViewportStateOptions.Builder()
                          .pitch(0.0)
                          .zoom(maxZoom)
                          .padding(EdgeInsets(100.0, 100.0, 100.0, 100.0))
                          .build())
                },
                colorLevel =
                    if (mapViewModel.isTrackingModeEnable.collectAsState().value)
                        ColorLevel.SECONDARY
                    else ColorLevel.PRIMARY)
          }

          if (enableParkingAddition) {
            IconButton(
                icon = Icons.Default.Add,
                contentDescription = "Add parking spots",
                modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .scale(1.2f)
                    .padding(bottom = 25.dp, start = 16.dp),
                onClick = {
                  mapViewModel.updateCameraPosition(mapViewportState.cameraState!!)
                  navigationActions.navigateTo(Route.ADD_SPOTS)
                },
                colorLevel = ColorLevel.PRIMARY,
                testTag = "addButton")
          }

            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { it:String -> searchQuery.value = it },
                placeholder = { Text(text = "Search for a location", color = Black) },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(end = 30.dp, start = 5.dp,top = 5.dp)
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Black, focusedLabelColor = Black,
                    focusedBorderColor = Cerulean, unfocusedBorderColor = Cerulean, cursorColor = Black, unfocusedContainerColor = White,
                    focusedContainerColor = White, unfocusedPlaceholderColor = Black),
                trailingIcon = {
                    if (searchQuery.value.isNotEmpty()) {
                        IconButton(onClick = { searchQuery.value = "" }, icon = Icons.Default.Clear, contentDescription = "Clear search",)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    virtualKeyboardManager?.hide()

                    runBlocking {
                    addressViewModel.search(searchQuery.value)
                    }

                    showSuggestions.value = true
                })
            )

            SmallFloatingActionButton(modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 5.dp, end = 5.dp),
                onClick = {
                    if (showSettings.value) showSettings.value = false else showSettings.value = true
                },
                icon = Icons.Filled.Settings,
                contentDescription = "Settings",
            )
        }



      // ======================= SETTINGS MENU=======================

      if(showSettings.value) {
          Box(
              modifier = Modifier
                  .fillMaxSize()
                  .padding(16.dp),
              contentAlignment = Alignment.TopEnd
          ) {
              Card(
                  modifier = Modifier
                      .width(300.dp)
                      .height(400.dp)
                      .align(Alignment.Center)
              ) {
                  Column(
                      modifier = Modifier.padding(16.dp)
                  ) {
                      Text(text = "Settings")
                      Spacer(modifier = Modifier.height(16.dp))

                      // Advanced Mode
                      Row(
                          verticalAlignment = Alignment.CenterVertically
                      ) {
                          Text(text = "Advanced Mode")
                          Spacer(modifier = Modifier.weight(1f))
                          Switch(
                              modifier = Modifier.padding(start = 8.dp),
                              checked = mapMode.value.isAdvancedMode,
                              onCheckedChange = {
                                  val futurMapMode =
                                      if (it) MapViewModel.MapMode.RECTANGLES else MapViewModel.MapMode.MARKERS
                                  mapViewModel.updateUserMapMode(futurMapMode)
                                  mapViewModel.updateMapMode(futurMapMode)
                              },
                              colors =
                              SwitchDefaults.colors()
                                  .copy(
                                      uncheckedTrackColor = disabledColor(),
                                  )
                          )
                      }
                  }
              }
          }
      }

      // ======================= SUGGESTION MENU=======================

        if(showSuggestions.value && listOfSuggestions.value.size != 1 && listOfSuggestions.value.isNotEmpty()) {
            ModalBottomSheet(onDismissRequest = { showSuggestions.value = false })
            {
                LazyColumn {
                    items(listOfSuggestions.value) { suggestion ->
                        Card(
                            onClick = {
                                showSuggestions.value = false

                                mapViewportState.transitionToOverviewState(
                                    OverviewViewportStateOptions.Builder()
                                        .geometry(mapViewportState.cameraState!!.center)
                                        .padding(EdgeInsets(100.0, 100.0, 100.0, 100.0))
                                        .build())
                                mapViewModel.updateTrackingMode(false)

                                    mapViewportState.setCameraOptions {
                                        center(Point.fromLngLat(suggestion.longitude.toDouble(), suggestion.latitude.toDouble()))
                                    }
                                    Log.e("Selected Location",Point.fromLngLat(suggestion.longitude.toDouble(), suggestion.latitude.toDouble()).toString())

                            },
                            colors = CardColors(containerColor = White, contentColor = Black, disabledContainerColor = White, disabledContentColor = Black),
                            modifier = Modifier.fillMaxSize()

                        ) {
                            Text(text = "${suggestion.road},${suggestion.city},${suggestion.country}")
                        }

                        androidx.compose.material.Divider(Modifier.fillMaxWidth(),color= Black)
                    }
                }
            }
        }
      else if (showSuggestions.value && listOfSuggestions.value.size == 1) {
            chosenLocation.value.let {

                    mapViewportState.transitionToOverviewState(
                        OverviewViewportStateOptions.Builder()
                            .geometry(mapViewportState.cameraState!!.center)
                            .padding(EdgeInsets(100.0, 100.0, 100.0, 100.0))
                            .build())
                    mapViewModel.updateTrackingMode(false)
                mapViewportState.setCameraOptions {
                    center(Point.fromLngLat(it.longitude.toDouble(), it.latitude.toDouble()))
                }
            }
          showSuggestions.value = false
      }
  }
}
