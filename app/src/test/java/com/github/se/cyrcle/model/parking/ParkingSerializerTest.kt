package com.github.se.cyrcle.model.parking

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ParkingSerializerTest {

  private val parkingAdapter = ParkingAdapter()

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
      assert(parking == deserializedParking1)
    }

    testParking(TestInstancesParking.parking1)
    testParking(TestInstancesParking.parking2)
    testParking(TestInstancesParking.parking3)
    testParking(TestInstancesParking.parking4)
  }
}
