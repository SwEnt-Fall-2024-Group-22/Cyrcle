package com.github.se.cyrcle.model.map

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.github.se.cyrcle.model.parking.Location
import com.github.se.cyrcle.model.parking.PARKING_MAX_AREA
import com.github.se.cyrcle.model.parking.PARKING_MAX_SIDE_LENGTH
import com.github.se.cyrcle.model.parking.PARKING_MIN_AREA
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.ui.map.MapConfig
import com.github.se.cyrcle.ui.map.maxZoom
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.theme.molecules.DropDownableEnum
import com.google.gson.Gson
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.turf.TurfMeasurement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

const val DEFAULT_ZOOM_FOR_ZONE_SELECTION = 10.0

class MapViewModel : ViewModel() {

  private val _cameraPosition = MutableStateFlow<CameraState?>(MapConfig.defaultCameraState())
  val cameraPosition: StateFlow<CameraState?> = _cameraPosition

  private val _selectedLocation = MutableStateFlow<Location?>(null)
  val selectedLocation: StateFlow<Location?> = _selectedLocation
  val isLocationValid: Flow<Boolean>
    get() =
        selectedLocation.map {
          it?.let { location ->
            val area = location.computeArea()
            val heightAndWidth = location.computeHeightAndWidth()
            heightAndWidth.first <= PARKING_MAX_SIDE_LENGTH &&
                heightAndWidth.second <= PARKING_MAX_SIDE_LENGTH &&
                area >= PARKING_MIN_AREA &&
                area <= PARKING_MAX_AREA
          } ?: false
        }

  private val _mapScreenCoordinates =
      MutableStateFlow<Pair<Point, Point>>(
          Pair(TestInstancesParking.EPFLCenter, TestInstancesParking.EPFLCenter))
  val mapScreenCoordinates: StateFlow<Pair<Point, Point>> = _mapScreenCoordinates

  private val _screenCoordinates = MutableStateFlow<List<ScreenCoordinate>>(listOf())
  val screenCoordinates: StateFlow<List<ScreenCoordinate>> = _screenCoordinates

  private val _locationPickerState = MutableStateFlow(LocationPickerState.NONE_SET)
  val locationPickerState: StateFlow<LocationPickerState> = _locationPickerState

  private val _isTrackingModeEnable = MutableStateFlow(true)
  val isTrackingModeEnable: StateFlow<Boolean> = _isTrackingModeEnable

  private val _userPosition = MutableStateFlow<Point>(TestInstancesParking.EPFLCenter)
  val userPosition: StateFlow<Point> = _userPosition

  private val _userMapMode = MutableStateFlow(MapMode.MARKERS)
  val userMapMode: StateFlow<MapMode> = _userMapMode

  private val _mapMode = MutableStateFlow(MapMode.MARKERS)
  val mapMode: StateFlow<MapMode> = _mapMode

  private val _cameraRecentering = MutableStateFlow(false)
  val cameraRecentering: StateFlow<Boolean> = _cameraRecentering

  /**
   * Update the map recentered state
   *
   * @param recentered the new state of the map recentered
   */
  fun updateMapRecentering(recentered: Boolean) {
    _cameraRecentering.value = recentered
  }

  /**
   * Update the map mode
   *
   * @param mapMode the new map mode to set
   */
  fun updateMapMode(mapMode: MapMode) {
    _mapMode.value = mapMode
  }

  /**
   * Update the user map mode.
   *
   * @param mapMode the new map mode to set
   */
  fun updateUserMapMode(mapMode: MapMode) {
    _userMapMode.value = mapMode
  }

  /**
   * Update the state of the location picker, This state is used to determine which steps of the
   * process to set the new location are completed
   *
   * @param state the new state of the location picker (NONE_SET, TOP_LEFT_SET, BOTTOM_RIGHT_SET,
   *   RECTANGLE_SET)
   */
  fun updateLocationPickerState(state: LocationPickerState) {
    _locationPickerState.value = state
  }

  /**
   * Update the camera position
   *
   * @param cameraState the new camera state This function is used to preserve the map position when
   *   the user navigates between screens
   */
  fun updateCameraPosition(cameraState: CameraState) {
    _cameraPosition.value = cameraState
  }

  /**
   * Set the camera to go to a specific location, with default padding, bearing and pitch. The zoom
   * is set to the max zoom defined in the UI. The tracking mode and the map recentering will be
   * disabled (call [updateTrackingMode] and [updateMapRecentering] with false).
   *
   * @param navigationActions the navigation actions to navigate to the map screen
   * @param location the location to zoom on.
   * @param zoom the zoom level to set.
   */
  fun zoomOnLocation(
      navigationActions: NavigationActions?,
      location: Location,
      zoom: Double = maxZoom
  ) {
    _cameraPosition.value =
        CameraState(
            location.center,
            MapConfig.defaultCameraState().padding,
            zoom,
            MapConfig.defaultCameraState().bearing,
            MapConfig.defaultCameraState().pitch)

    updateTrackingMode(false)
    updateMapRecentering(true)
    navigationActions?.navigateTo(Route.MAP)
  }

  /**
   * Update the focus mode
   *
   * @param focusMode the new focus mode This function is used to update the focus mode of the map
   *   screen. The focus mode is used to center the map on the user's position
   */
  fun updateTrackingMode(focusMode: Boolean) {
    _isTrackingModeEnable.value = focusMode
  }

  /**
   * Update the camera position without bearing
   *
   * @param cameraState the new camera state This function is used to preserve the map position when
   *   the user navigates between screens, but without the bearing. As the add parking screen does
   *   support rotation, but the map screen does not
   */
  fun updateCameraPositionWithoutBearing(cameraState: CameraState) {
    val newCameraState =
        CameraState(cameraState.center, EdgeInsets(0.0, 0.0, 0.0, 0.0), cameraState.zoom, 0.0, 0.0)
    _cameraPosition.value = newCameraState
  }

  /**
   * Unzooms the camera, to show a large are of the map to the user selecting a zone to download
   * offline.
   */
  fun setCameraForZoneSelection() {
    val actualCamera = _cameraPosition.value!!
    _cameraPosition.value =
        CameraState(
            actualCamera.center,
            EdgeInsets(0.0, 0.0, 0.0, 0.0),
            DEFAULT_ZOOM_FOR_ZONE_SELECTION,
            0.0,
            0.0)
  }

  /**
   * Update the screen coordinates states
   *
   * @param screenCoordinates the new screen coordinates The ScreenCoordinates are used to draw a
   *   rectangle on the map when the user is selecting a parking area
   */
  fun updateScreenCoordinates(screenCoordinates: List<ScreenCoordinate>) {
    _screenCoordinates.value = screenCoordinates
  }

  /**
   * Update the selected location
   *
   * @param location the new location This function is used to store the selected location when the
   *   user is selecting a parking area
   */
  fun updateLocation(location: Location?) {
    _selectedLocation.value = location
  }

  /**
   * Update the user position
   *
   * @param position the new user position
   */
  fun updateUserPosition(position: Point) {
    _userPosition.value = position
  }

  /**
   * Updates the bottom left and top right corners of the screen in latitude and longitude
   * coordinates. The corners are calculated based on the center of the screen and the viewport
   * dimensions.
   *
   * @param mapView the MapView to get the screen corners from
   */
  fun updateScreenCoordinates(mapView: MapView) {
    // Retrieve viewport dimensions
    val viewportWidth = mapView.width
    val viewportHeight = mapView.height

    val centerPixel = mapView.mapboxMap.pixelForCoordinate(mapView.mapboxMap.cameraState.center)

    // Calculate the multiplier for the buffer
    val multiplier = 3.0

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

    _mapScreenCoordinates.value = Pair(bottomLeftCorner, topRightCorner)
  }

  private val handler = Handler(Looper.getMainLooper())
  private var lastUpdateTime = 0L
  private val updateInterval = 500L // 0.5 seconds

  /**
   * Initialize the location component of the map and add a listener to update the user position
   *
   * @param mapView the MapView to initialize the location component on
   */
  fun initLocationComponent(mapView: MapView) {
    val locationComponentPlugin = mapView.location
    locationComponentPlugin.updateSettings {
      this.enabled = true
      this.locationPuck = createDefault2DPuck(true)
    }

    locationComponentPlugin.addOnIndicatorPositionChangedListener { point ->
      val currentTime = System.currentTimeMillis()
      if (currentTime - lastUpdateTime >= updateInterval) {
        handler.post {
          updateUserPosition(point)
          lastUpdateTime = currentTime
        }
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
  fun drawMarkers(
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

  /**
   * Draw the rectangles on the map
   *
   * @param polygonAnnotationManager the polygon annotation manager
   * @param parkingsList the list of locations to draw
   */
  fun drawRectangles(
      polygonAnnotationManager: PolygonAnnotationManager?,
      plabelAnnotationManager: PointAnnotationManager?,
      parkingsList: List<Parking>
  ) {
    // Create a list of annotations to draw
    val annotations: MutableList<PolygonAnnotationOptions> = mutableListOf()

    // For each parking, add the polygon and the label to their respective Annotation Manager and
    // draw them.
    parkingsList.map { parking ->
      val location = parking.location
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
                .withData(Gson().toJsonTree(parking))
        annotations.add(polygonAnnotationOptions)
        val area = TurfMeasurement.area(polygon)
        val labelAnnotationOption =
            PointAnnotationOptions()
                .withPoint(location.center)
                .withTextField("P")
                .withTextSize(if (area < 30) 5.0 else 10.0)
                .withTextColor("#FFFFFF")
                .withTextHaloColor("#FFFFFF")
                .withTextHaloWidth(0.2)

        // draw the labels via the labelAnnotationManager
        plabelAnnotationManager?.create(labelAnnotationOption)
      }
    }

    // draw the polygons via the polygonAnnotationManager
    polygonAnnotationManager?.create(annotations)
  }

  /**
   * Enum class to represent the state of the location picker, This state is used to determine which
   * steps of the process to set the new location are completed NONE_SET: No location is set
   * TOP_LEFT_SET: The top left corner of the rectangle is set BOTTOM_RIGHT_SET: The bottom right
   * corner of the rectangle is set RECTANGLE_SET: All corners of the rectangle are set
   */
  enum class LocationPickerState {
    NONE_SET,
    TOP_LEFT_SET,
    BOTTOM_RIGHT_SET,
    RECTANGLE_SET
  }

  /**
   * Enum class to represent the different modes of the map
   *
   * @param description the description of the mode
   * @param isAdvancedMode true if the mode is advanced, false otherwise The advanced mode is the
   *   mode where the rectangles are displayed The simple mode is the mode where the markers are
   *   displayed
   */
  enum class MapMode(override val description: String, val isAdvancedMode: Boolean) :
      DropDownableEnum {
    MARKERS("Simple", false),
    RECTANGLES("Advanced", true)
  }
}
