package com.github.se.cyrcle.model.map

import com.github.se.cyrcle.model.map.MapViewModel.LocationPickerState
import com.github.se.cyrcle.model.parking.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ScreenCoordinate
import org.junit.Before
import org.junit.Test

class MapViewModelTest {
  private lateinit var mapViewModel: MapViewModel

  @Before
  fun setUp() {
    mapViewModel = MapViewModel()
  }

  @Test
  fun mapViewModelTestCameraState() {
    val cameraState =
        CameraState(
            Point.fromLngLat(6.566, 46.519), EdgeInsets(0.0, 0.0, 0.0, 0.0), 17.0, 10.0, 10.0)

    mapViewModel.updateCameraPosition(cameraState)
    val returnedCamera = mapViewModel.cameraPosition.value!!
    assert(returnedCamera.zoom == 17.0)
    assert(returnedCamera.center.latitude() == 46.519)
    assert(returnedCamera.center.longitude() == 6.566)
    assert(returnedCamera.pitch == 10.0)
    assert(returnedCamera.bearing == 10.0)

    mapViewModel.updateCameraPositionWithoutBearing(cameraState)
    val returnedCamera2 = mapViewModel.cameraPosition.value!!
    assert(returnedCamera2.zoom == 17.0)
    assert(returnedCamera2.center.latitude() == 46.519)
    assert(returnedCamera2.center.longitude() == 6.566)
    assert(returnedCamera2.pitch == 0.0)
    assert(returnedCamera2.bearing == 0.0)
  }

  @Test
  fun mapViewModelTestLocationPickerState() {
    assert(mapViewModel.locationPickerState.value == LocationPickerState.NONE_SET)

    mapViewModel.updateLocationPickerState(LocationPickerState.TOP_LEFT_SET)
    assert(mapViewModel.locationPickerState.value == LocationPickerState.TOP_LEFT_SET)

    mapViewModel.updateLocationPickerState(LocationPickerState.BOTTOM_RIGHT_SET)
    assert(mapViewModel.locationPickerState.value == LocationPickerState.BOTTOM_RIGHT_SET)

    mapViewModel.updateLocationPickerState(LocationPickerState.RECTANGLE_SET)
    assert(mapViewModel.locationPickerState.value == LocationPickerState.RECTANGLE_SET)
  }

  @Test
  fun mapViewModelTestSelectedLocation() {
    assert(mapViewModel.selectedLocation.value == null)

    val location = Location(Point.fromLngLat(6.566, 46.519))
    mapViewModel.updateLocation(location)
    assert(mapViewModel.selectedLocation.value == location)
  }

  @Test
  fun mapViewModelTestScreenCoordinates() {
    assert(mapViewModel.screenCoordinates.value.isEmpty())

    val screenCoordinates = listOf(ScreenCoordinate(10.0, 10.0))
    mapViewModel.updateScreenCoordinates(screenCoordinates)
    assert(mapViewModel.screenCoordinates.value == screenCoordinates)
  }

  @Test
  fun mapViewModelTestZoomOnLocation() {
    assert(mapViewModel.screenCoordinates.value.isEmpty())
    val expected = Location(Point.fromLngLat(6.566, 46.519))
    mapViewModel.zoomOnLocation(expected)

    val actual =
        mapViewModel.cameraPosition.value?.center
            ?: Point.fromLngLat(0.0, 0.0) // Default to somewhere not expected
    assert(expected.center == actual)
  }
}
