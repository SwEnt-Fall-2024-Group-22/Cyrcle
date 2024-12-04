package com.github.se.cyrcle.io.serializer

import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.mapbox.geojson.Point
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ParkingSerializerTest {

  private val pointAdapter = PointAdapter()
  private val parkingAdapter = ParkingAdapter()

  @Test
  fun convertParkingProtectionTest() {
    fun testProtection(protection: ParkingProtection) {
      val serializedProtection = parkingAdapter.serializeParkingProtection(protection)
      val deserializedProtection = parkingAdapter.deserializeParkingProtection(serializedProtection)
      assertEquals(protection, deserializedProtection)
    }

    testProtection(ParkingProtection.NONE)
    testProtection(ParkingProtection.COVERED)
    testProtection(ParkingProtection.INDOOR)
  }

  @Test
  fun convertParkingRackType() {
    fun testRackType(enumValue: ParkingRackType) {
      val serialized = parkingAdapter.serializeParkingRackType(enumValue)
      val deserialized = parkingAdapter.deserializeParkingRackType(serialized)
      assertEquals(enumValue, deserialized)
    }

    testRackType(ParkingRackType.U_RACK)
    testRackType(ParkingRackType.TWO_TIER)
    testRackType(ParkingRackType.GRID)
    testRackType(ParkingRackType.WAVE)
  }

  @Test
  fun convertParkingCapacity() {
    fun testCapacity(enumValue: ParkingCapacity) {
      val serialized = parkingAdapter.serializeParkingCapacity(enumValue)
      val deserialized = parkingAdapter.deserializeParkingCapacity(serialized)
      assertEquals(enumValue, deserialized)
    }

    testCapacity(ParkingCapacity.XSMALL)
    testCapacity(ParkingCapacity.SMALL)
    testCapacity(ParkingCapacity.LARGE)
    testCapacity(ParkingCapacity.XLARGE)
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
