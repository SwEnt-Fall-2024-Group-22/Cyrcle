package com.github.se.cyrcle.model.parking

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Data class representing a parking spot.
 *
 * @property uid Unique identifier of the parking spot.
 * @property optName Optional name of the parking spot.
 * @property optDescription Optional description of the parking spot.
 * @property location Location of the parking spot as a pair of latitude and longitude.
 * @property images List of URLs of images of the parking spot.
 * @property capacity Capacity of the parking spot.
 * @property rackType Type of rack in the parking spot.
 * @property protection Protection of the parking spot from the weather.
 * @property price Price of the parking spot. The price is in the currency of the country where the
 *   parking spot is located. 0 if the parking spot is free.
 */
data class Parking(
    val uid: String,
    val optName: String?,
    val optDescription: String?,
    val location: Location,
    val images: List<String>,
    val capacity: ParkingCapacity,
    val rackType: ParkingRackType,
    val protection: ParkingProtection,
    val price: Double,
    val hasSecurity: Boolean
    // TODO: Add list of reviews when implemented
)

/** Enum class representing the capacity of a parking spot. */
enum class ParkingCapacity {
  XSMALL, // less than 10 spots
  SMALL, // 10-25 spots
  MEDIUM, // 26-50 spots
  LARGE, // 51-100 spots
  XLARGE // more than 100 spots
}

/** Enum class representing the type of rack in a parking spot. */
enum class ParkingRackType {
  TWO_TIER,
  U_RACK,
  VERTICAL,
  WAVE,
  WALL_BUTTERFLY,
  POST_AND_RING,
  GRID,
  OTHER
}

/** Enum class representing the protection of a parking spot from the weather. */
enum class ParkingProtection {
  INDOOR,
  COVERED,
  NONE
}

data class Point(val latitude: Double, val longitude: Double) {
  override fun toString(): String {
    return "Point(latitude=$latitude, longitude=$longitude)"
  }

  fun distanceTo(other: Point): Double {
    return sqrt((latitude - other.latitude).pow(2) + (longitude - other.longitude).pow(2))
  }
}

data class Location(
    // TODO replace with Point Mapbox.Point
    val topLeft: Point?,
    val topRight: Point?,
    val bottomLeft: Point?,
    val bottomRight: Point?,
    val center: Point?
) {
  constructor(
      topLeft: Point,
      topRight: Point,
      bottomLeft: Point,
      bottomRight: Point
  ) : this(
      topLeft,
      topRight,
      bottomLeft,
      bottomRight,
      Point(
          (topLeft.latitude + bottomRight.latitude) / 2,
          (topLeft.longitude + bottomRight.longitude) / 2))

  constructor(center: Point) : this(null, null, null, null, center)
}
