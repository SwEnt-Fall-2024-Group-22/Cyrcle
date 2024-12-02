package com.github.se.cyrcle.io.serializer

import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.mapbox.geojson.Point
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ParkingSerializerTest {

  private val pointAdapter = PointAdapter()
  private val parkingSetAdapter = ParkingSetAdapter()
  private val parkingAdapter = ParkingAdapter()

  @Test
  fun convertSetsOfParkingsTest() {
    fun testSet(parkingSet: MutableSet<Parking>) {
      val serializedParking1 = parkingSetAdapter.serialize(parkingSet)
      val deserializedParking1 = parkingSetAdapter.deserialize(serializedParking1)
      assertEquals(parkingSet, deserializedParking1)
    }

    testSet(mutableSetOf())
    testSet(mutableSetOf(TestInstancesParking.parking1))
    testSet(mutableSetOf(TestInstancesParking.parking1, TestInstancesParking.parking2))
  }

  @Test
  fun testEmptySetOfParkings() {
    val serializedParking1 = parkingSetAdapter.serialize(mutableSetOf())
    val deserializedParking1 = parkingSetAdapter.deserialize(serializedParking1)
    assert(deserializedParking1.isEmpty())
  }

  @Test
  fun convertParkingAdapterTest() {
    fun testParking(parking: Parking) {
      val serializedParking1 = parkingAdapter.serialize(parking)
      val deserializedParking1 = parkingAdapter.deserialize(serializedParking1)
      assert(parking == deserializedParking1)
    }

    testParking(TestInstancesParking.parking1)
    testParking(TestInstancesParking.parking2)
    testParking(TestInstancesParking.parking3)
    testParking(TestInstancesParking.parking4)
  }

  @Test
  fun convertParkingForRepoAdapterTest() {
    fun testParking(parking: Parking) {
      val serializedParking1 = parkingAdapter.serializeParking(parking)
      val deserializedParking1 = parkingAdapter.deserializeParking(serializedParking1)
      assertEquals(parking, deserializedParking1)
    }

    testParking(TestInstancesParking.parking1)
    testParking(TestInstancesParking.parking2)
    testParking(TestInstancesParking.parking3)
    testParking(TestInstancesParking.parking4)
  }

  @Test
  fun convertPointsAdapterTest() {
    fun testPoint(point: Point) {
      val serializedPoint = pointAdapter.serialize(point)
      val deserializedPoint = pointAdapter.deserialize(serializedPoint)
      assertEquals(point, deserializedPoint)
    }

    testPoint(TestInstancesParking.referencePoint)
  }
}
