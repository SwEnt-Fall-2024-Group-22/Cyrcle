package com.github.se.cyrcle.io.serializer

import com.github.se.cyrcle.model.parking.Location
import com.mapbox.geojson.Point
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LocationAdapterTest {

  private val locationAdapter = LocationAdapter()

  @Test
  fun convertLocationTest() {
    fun testLocation(location: Location) {
      val serializedLocation = locationAdapter.serializeLocation(location)
      val deserializedLocation = locationAdapter.deserializeLocation(serializedLocation)
      assertEquals(location, deserializedLocation)
    }

    testLocation(Location(Point.fromLngLat(1.0, 1.0)))
    testLocation(Location(Point.fromLngLat(1.0, 1.0), Point.fromLngLat(2.0, 2.0), null, null, null))
  }
}
