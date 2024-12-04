package com.github.se.cyrcle.ui.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.R
import com.github.se.cyrcle.model.address.Address
import com.github.se.cyrcle.model.address.AddressViewModel
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.permission.PermissionHandler
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.theme.Black
import com.github.se.cyrcle.ui.theme.ColorLevel
import com.github.se.cyrcle.ui.theme.atoms.Button
import com.github.se.cyrcle.ui.theme.atoms.IconButton
import com.github.se.cyrcle.ui.theme.atoms.ScoreStars
import com.github.se.cyrcle.ui.theme.atoms.SmallFloatingActionButton
import com.github.se.cyrcle.ui.theme.atoms.Text
import com.github.se.cyrcle.ui.theme.defaultOnColor
import com.github.se.cyrcle.ui.theme.getOutlinedTextFieldColorsSearchBar
import com.github.se.cyrcle.ui.theme.invertColor
import com.github.se.cyrcle.ui.theme.molecules.BottomNavigationBar
import com.github.se.cyrcle.ui.theme.molecules.FilterPanel
import com.google.gson.Gson
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationSourceOptions
import com.mapbox.maps.plugin.annotation.ClusterOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.OnPolygonAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolygonAnnotationManager
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.scalebar.generated.ScaleBarSettings
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.data.OverviewViewportStateOptions
import kotlinx.coroutines.runBlocking

const val defaultZoom = 16.0
const val maxZoom = 18.0
const val minZoom = 8.0
const val thresholdDisplayZoom = 13.0
const val LAYER_ID = "0128"
const val LAYER_ID_RECT = "0129"
const val ADVANCED_MODE_ZOOM_THRESHOLD = 15.5
const val CLUSTER_COLORS = "#1A4988"
const val MAX_SUGGESTION_DISPLAY_NAME_LENGTH_MAP = 100

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
  val listOfParkings by parkingViewModel.filteredRectParkings.collectAsState(emptyList())

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

  var showPreviewCard by remember { mutableStateOf(false) }

  Log.d("MapScreen", "showPreviewCard: $showPreviewCard")

  // Collect the selected parking from the ParkingViewModel as a state
  val selectedParking by parkingViewModel.selectedParking.collectAsState()

  // Collect the location permission status as a state from the PermissionHandler
  val locationEnabled by permissionHandler.getLocalisationPerm().collectAsState()

  // Prevent the camera from recentering when the user exit and come back to the map by disabling
  // the related launchedEffect
  val cameraRecentering = mapViewModel.cameraRecentering.collectAsState()

  // Mutable state to store the visibility of the settings menu
  val showSettings = remember { mutableStateOf(false) }

  // Mutable state to store the visibility of the filter dialog
  val showFilter = remember { mutableStateOf(false) }

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

  val bitmap = BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.dot)
  val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, false)

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
            compass = {
              // height of setting icon is hardcoded to 56dp so we put the compass down by more than
              // 56dp.
              Compass(alignment = Alignment.TopEnd, modifier = Modifier.padding(top = 60.dp))
            },
            // disable the scale bar that is anyways under the search bar.
            scaleBar = { ScaleBarSettings.Builder().setEnabled(false).build() },
            modifier = Modifier.fillMaxSize().padding(padding).testTag("MapScreen"),
            mapViewportState = mapViewportState,
            style = { MapConfig.DefaultStyle() }) {
              DisposableMapEffect { mapView ->

                // ======================= GLOBAL SETTINGS =======================
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

                // ======================= MOVE LISTENER =======================
                // Add a move listener to the map to deactivate tracking mode when the user moves
                // the map
                val moveListener =
                    object : OnMoveListener {
                      override fun onMoveBegin(detector: MoveGestureDetector) {
                        // Remove any preview card displayed
                        showPreviewCard = false

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

                // ======================= ANNOTATION CLICK LISTENER =======================
                // Define a common click handler function
                fun handleAnnotationClick(
                    annotation: Any,
                    mapViewportState: MapViewportState,
                    parkingViewModel: ParkingViewModel
                ) {
                  val parkingData =
                      when (annotation) {
                        is PointAnnotation -> annotation.getData()?.asJsonObject
                        is PolygonAnnotation -> annotation.getData()?.asJsonObject
                        else -> null
                      }

                  // If the data is not null, deserialize it to a Parking object, select it in the
                  // ViewModel to show the preview card and center the camera on it
                  parkingData?.let {
                    val parking = Gson().fromJson(it, Parking::class.java)

                    parkingViewModel.selectParking(parking)
                    showPreviewCard = true

                    mapViewportState.setCameraOptions { center(parking.location.center) }
                  }
                }

                // Use the common handler for markers
                markerAnnotationManager?.addClickListener(
                    OnPointAnnotationClickListener { pointAnnotation ->
                      handleAnnotationClick(pointAnnotation, mapViewportState, parkingViewModel)
                      true
                    })

                // Use the common handler for rectangles
                rectangleAnnotationManager?.addClickListener(
                    OnPolygonAnnotationClickListener { polygonAnnotation ->
                      handleAnnotationClick(polygonAnnotation, mapViewportState, parkingViewModel)
                      true
                    })
                // ======================= ANNOTATION CLICK LISTENER =======================

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
                  // On zoom-in in past the threshold, switch to the user's selected mode
                  if (mapView.mapboxMap.cameraState.zoom >= ADVANCED_MODE_ZOOM_THRESHOLD &&
                      zoomState.value < ADVANCED_MODE_ZOOM_THRESHOLD) {
                    mapViewModel.updateMapMode(userMapMode.value)
                  }

                  // Store the zoom level
                  // This must stay at the end of the listener.
                  zoomState.value = mapView.mapboxMap.cameraState.zoom
                }
                // =======================  CAMERA  LISTENER  =======================

                onDispose {
                  pLabelAnnotationManager?.deleteAll()
                  rectangleAnnotationManager?.deleteAll()
                  markerAnnotationManager?.deleteAll()
                  showPreviewCard = false
                  mapViewModel.updateCameraPosition(mapViewportState.cameraState!!)
                }
              }
            }

        // ======================= OVERLAY =======================
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
          if (showPreviewCard) {
            PreviewCard(navigationActions, parkingViewModel)
          }
          // Center button if location is enabled
          if (locationEnabled) {
            IconButton(
                icon = Icons.Default.MyLocation,
                contentDescription = "Recenter on Location",
                modifier =
                    Modifier.align(Alignment.BottomEnd)
                        .padding(bottom = 25.dp, end = 16.dp)
                        .scale(1.2f)
                        .testTag("recenterButton"),
                onClick = {
                  showPreviewCard = false
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

          // Add button to add parking spots if the user is signed in
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

          // Search bar and Settings button row
          Row(
              modifier = Modifier.fillMaxWidth().padding(5.dp).align(Alignment.TopStart),
              verticalAlignment = Alignment.CenterVertically) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery.value,
                    onValueChange = { searchQuery.value = it },
                    placeholder = { Text(text = stringResource(R.string.search_bar_placeholder)) },
                    modifier = Modifier.weight(1f).height(56.dp).testTag("SearchBar"),
                    shape = RoundedCornerShape(16.dp),
                    colors = getOutlinedTextFieldColorsSearchBar(ColorLevel.PRIMARY),
                    trailingIcon = {
                      if (searchQuery.value.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear search",
                            tint = defaultOnColor(),
                            modifier = Modifier.clickable { searchQuery.value = "" })
                      }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions =
                        KeyboardActions(
                            onSearch = {
                              virtualKeyboardManager?.hide()
                              runBlocking { addressViewModel.search(searchQuery.value) }
                              showSuggestions.value = true
                            }))

                // Filter button
                Box(modifier = Modifier.size(56.dp).padding(start = 5.dp)) {
                  SmallFloatingActionButton(
                      modifier = Modifier.matchParentSize().testTag("FilterButton"),
                      onClick = { showFilter.value = true },
                      icon = Icons.Default.FilterList,
                      contentDescription = "Filter")
                }

                // Settings button
                Box(modifier = Modifier.size(56.dp).padding(start = 5.dp)) {
                  SmallFloatingActionButton(
                      modifier =
                          Modifier.matchParentSize() // Fill the parent Box
                              .testTag("SettingsMenuButton"),
                      onClick = { showSettings.value = true },
                      icon = Icons.Filled.Settings,
                      contentDescription = "Settings",
                  )
                }
              }
        }

        // Settings alert dialog is shown when the showSettings state is true, until the user
        // dismisses it by clicking the close button or outside the dialog.
        if (showSettings.value) {
          SettingsDialog(
              mapMode, mapViewModel, navigationActions, onDismiss = { showSettings.value = false })
        }

        // Filter dialog is shown when the showFilter state is true, until the user dismisses it by
        // clicking the close button or outside the dialog.
        if (showFilter.value) {
          FilterDialog(
              parkingViewModel,
              addressViewModel,
              permissionHandler,
              onDismiss = { showFilter.value = false })
        }

        if (showSuggestions.value &&
            listOfSuggestions.value.size != 1 &&
            listOfSuggestions.value.isNotEmpty()) {

          SuggestionMenu(showSuggestions, listOfSuggestions, mapViewportState, mapViewModel)
        } else if (showSuggestions.value && listOfSuggestions.value.size == 1) {
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

/**
 * Composable function to display the suggestion menu.
 *
 * @param showSuggestions The state of the suggestion menu.
 * @param listOfSuggestions The list of suggestions extracted from the NominatimAPI.
 * @param mapViewportState The viewport state of the map.
 * @param mapViewModel The ViewModel for managing map-related data and actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionMenu(
    showSuggestions: MutableState<Boolean>,
    listOfSuggestions: State<List<Address>>,
    mapViewportState: MapViewportState,
    mapViewModel: MapViewModel
) {

  val uniqueSuggestions = remember { mutableStateOf(listOf<Address>()) }

  ModalBottomSheet(
      onDismissRequest = { showSuggestions.value = false },
      modifier = Modifier.testTag("SuggestionsMenu")) {
        LazyColumn {
          val seenNames = mutableSetOf<String>()
          uniqueSuggestions.value =
              listOfSuggestions.value.filter { suggestion ->
                val displayName =
                    suggestion.suggestionFormatDisplayName(
                        MAX_SUGGESTION_DISPLAY_NAME_LENGTH_MAP, Address.Mode.MAP)
                if (displayName in seenNames) {
                  false
                } else {
                  seenNames.add(displayName)
                  true
                }
              }
          items(uniqueSuggestions.value) { suggestion ->
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
                    center(
                        Point.fromLngLat(
                            suggestion.longitude.toDouble(), suggestion.latitude.toDouble()))
                  }

                  Log.e(
                      "Selected Location",
                      Point.fromLngLat(
                              suggestion.longitude.toDouble(), suggestion.latitude.toDouble())
                          .toString())
                },
                colors =
                    CardColors(
                        containerColor = invertColor(defaultOnColor()),
                        contentColor = defaultOnColor(),
                        disabledContainerColor = invertColor(defaultOnColor()),
                        disabledContentColor = defaultOnColor()),
                modifier = Modifier.fillMaxSize().testTag("suggestionCard${suggestion.city}")) {
                  Text(
                      text =
                          suggestion.suggestionFormatDisplayName(
                              MAX_SUGGESTION_DISPLAY_NAME_LENGTH_MAP, Address.Mode.MAP),
                      Modifier.padding(5.dp),
                      textAlign = TextAlign.Start)
                }

            androidx.compose.material.Divider(Modifier.fillMaxWidth(), color = Black)
          }
        }
      }
}

/**
 * Composable function to display the settings menu.
 *
 * @param mapMode The state of the map mode.
 * @param mapViewModel The ViewModel for managing map-related data and actions.
 * @param navigationActions The actions to navigate to different screens.
 * @param onDismiss The function to dismiss the dialog.
 */
@Composable
fun SettingsDialog(
    mapMode: State<MapViewModel.MapMode>,
    mapViewModel: MapViewModel,
    navigationActions: NavigationActions,
    onDismiss: () -> Unit
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      title = {
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp))
      },
      text = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          // Advanced Mode
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(64.dp), contentAlignment = Alignment.CenterStart) {
              Switch(
                  modifier = Modifier.testTag("advancedModeSwitch"),
                  checked = mapMode.value.isAdvancedMode,
                  onCheckedChange = {
                    val futureMapMode =
                        if (it) MapViewModel.MapMode.RECTANGLES else MapViewModel.MapMode.MARKERS
                    mapViewModel.updateUserMapMode(futureMapMode)
                    mapViewModel.updateMapMode(futureMapMode)
                  },
                  colors =
                      SwitchDefaults.colors(
                          uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant))
            }
            Text(
                text = stringResource(R.string.map_screen_mode_switch_label),
                style = MaterialTheme.typography.bodyLarge)
          }

          HorizontalDivider(
              thickness = 1.dp,
              modifier = Modifier.fillMaxWidth(),
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

          // Offline Map Management
          Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier =
                  Modifier.fillMaxWidth()
                      .clickable {
                        navigationActions.navigateTo(Route.ZONE)
                        onDismiss()
                      }
                      .testTag("SettingsToZoneRow")) {
                Box(modifier = Modifier.width(64.dp), contentAlignment = Alignment.CenterStart) {
                  Icon(
                      imageVector = Icons.Filled.CloudDownload,
                      contentDescription = "Offline Maps",
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(46.dp))
                }
                Text(
                    text = stringResource(R.string.map_screen_settings_to_zone),
                    style = MaterialTheme.typography.bodyLarge)
              }
        }
      },
      confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
      containerColor = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp,
      modifier = Modifier.testTag("SettingsMenu"),
      shape = RoundedCornerShape(24.dp))
}

/**
 * Composable function to display the filter dialog.
 *
 * @param parkingViewModel The ViewModel for managing parking-related data and actions.
 * @param onDismiss The function to dismiss the dialog.
 */
@Composable
fun FilterDialog(
    parkingViewModel: ParkingViewModel,
    addressViewModel: AddressViewModel,
    permissionHandler: PermissionHandler,
    onDismiss: () -> Unit
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      title = {
        Text(
            text = stringResource(R.string.filter),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp))
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
          FilterPanel(
              parkingViewModel,
              displayHeader = false,
              addressViewModel,
              permissionHandler = permissionHandler)
        }
      },
      confirmButton = {
        TextButton(onClick = onDismiss, modifier = Modifier.testTag("FilterMenuClose")) {
          Text(stringResource(R.string.close))
        }
      },
      containerColor = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp,
      modifier = Modifier.testTag("FilterMenu"),
      shape = RoundedCornerShape(24.dp))
}

@Composable
fun PreviewCard(navigationActions: NavigationActions, parkingViewModel: ParkingViewModel) {
  Box(modifier = Modifier.fillMaxSize()) {
    // Get the selected parking from the ViewModel
    val parking = parkingViewModel.selectedParking.collectAsState().value ?: return
    // Card displaying the parking overview information
    Card(
        modifier =
            Modifier.width(250.dp)
                .align(Alignment.Center)
                // Note: The .layout modifier was generated by a LLM
                .layout { measurable, constraints ->
                  val placeable = measurable.measure(constraints)
                  layout(placeable.width, placeable.height) {
                    val yOffset = -placeable.height / 2 - 50.dp.roundToPx()
                    placeable.placeRelative(0, yOffset)
                  }
                },
        colors =
            cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)) {
          Column(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = parking.optName ?: stringResource(R.string.default_parking_name),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Left)
                ScoreStars(
                    score = parking.avgScore,
                    text =
                        pluralStringResource(R.plurals.reviews_count, count = parking.nbReviews)
                            .format(parking.nbReviews),
                    scale = 0.6f)
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                Spacer(modifier = Modifier.height(8.dp))
                // Text for rack type
                Text(
                    text =
                        stringResource(R.string.map_screen_rack_type, parking.rackType.description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Left)

                // Text for protection
                Text(
                    text =
                        stringResource(
                            R.string.map_screen_protection, parking.protection.description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Left)

                // Text for description
                Text(
                    text =
                        stringResource(R.string.map_screen_capacity, parking.capacity.description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Left)

                // Go to parking details button
                Button(
                    onClick = { navigationActions.navigateTo(Screen.PARKING_DETAILS) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    text = "See details")
              }
        }
  }
}
