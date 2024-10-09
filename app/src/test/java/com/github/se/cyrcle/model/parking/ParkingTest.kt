package com.github.se.cyrcle.model.parking

import com.google.gson.Gson
import org.junit.Before
import org.junit.Test

class ParkingTest {
  private lateinit var gson: Gson
  private lateinit var parking: Parking

  @Before
  fun setUp() {
    gson = Gson()
    parking =
        Parking(
            "1",
            "EPFL",
            "Parking for EPFL",
            Pair(46.518, 6.565),
            listOf("imageUrl"),
            ParkingCapacity.LARGE,
            ParkingRackType.TWO_TIER,
            ParkingProtection.COVERED,
            0.0,
            true)
  }

  @Test
  fun testJsonSerialization() {
    val json = gson.toJson(parking)
    val parkingFromJson = gson.fromJson(json, Parking::class.java)
    assert(parking == parkingFromJson)
  }

  @Test
  fun testJsonSerializationWithoutName() {
    val parkingWithoutName =
        Parking(
            "1",
            null,
            null,
            Pair(46.518, 6.565),
            listOf("imageUrl"),
            ParkingCapacity.LARGE,
            ParkingRackType.TWO_TIER,
            ParkingProtection.COVERED,
            0.0,
            true)
    val json = gson.toJson(parkingWithoutName)
    val parkingFromJson = gson.fromJson(json, Parking::class.java)
    assert(parkingWithoutName == parkingFromJson)
  }
}
