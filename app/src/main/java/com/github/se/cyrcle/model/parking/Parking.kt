package com.github.se.cyrcle.model.parking

import com.mapbox.geojson.Point

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

interface ParkingAttribute {
  val description: String
}
/** Enum class representing the capacity of a parking spot. */
enum class ParkingCapacity(override val description: String) : ParkingAttribute {
  XSMALL("Less than 10 spots"),
  SMALL("10-25 spots"),
  MEDIUM("26-50 spots"),
  LARGE("51-100 spots"),
  XLARGE("More than 100 spots")
}

/** Enum class representing the type of rack in a parking spot. */
enum class ParkingRackType(override val description: String) : ParkingAttribute {
  TWO_TIER("Two-tier rack"),
  U_RACK("U-Rack"),
  VERTICAL("Vertical rack"),
  WAVE("Wave rack"),
  WALL_BUTTERFLY("Wall butterfly rack"),
  POST_AND_RING("Post and ring rack"),
  GRID("Grid rack"),
  OTHER("Other type of rack")
}

enum class ParkingProtection(override val description: String) : ParkingAttribute {
  INDOOR("Indoor"),
  COVERED("Covered"),
  NONE("Exposed")
}

data class Location(
    val center: Point,
    val topLeft: Point?,
    val topRight: Point?,
    val bottomLeft: Point?,
    val bottomRight: Point?,
) {
  constructor(
      topLeft: Point,
      topRight: Point,
      bottomRight: Point,
      bottomLeft: Point
  ) : this(
      Point.fromLngLat(
          (topLeft.longitude() + bottomRight.longitude()) / 2,
          (topLeft.latitude() + bottomRight.latitude()) / 2),
      topLeft,
      topRight,
      bottomLeft,
      bottomRight)

  constructor(center: Point) : this(center, null, null, null, null)

  constructor(
      listPoints: List<Point>
  ) : this(listPoints[0], listPoints[1], listPoints[2], listPoints[3])
}
