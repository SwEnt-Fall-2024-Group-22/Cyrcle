package com.github.se.cyrcle.model.parking

import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.se.cyrcle.ui.theme.molecules.DropDownableEnum
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.turf.TurfMeasurement

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
 * @property nbReviews Number of Reviews for this Parking spot as of now.
 * @property avgScore Average Review Score for this Parking spot as of now.
 */
@Entity(tableName = Parking.TABLE_NAME)
data class Parking(
    @PrimaryKey val uid: String,
    val optName: String?,
    val optDescription: String?,
    val location: Location,
    val images: List<String>, // List of Image paths in Cloud Repository
    val imageObjects: List<ParkingImage>, // List of Image paths to repository for general purposes
    var maxNumOfImages: Int = 0,
    var reportedImages: List<ParkingImage>, // List of Image paths to repository for report purposes
    val capacity: ParkingCapacity,
    val rackType: ParkingRackType,
    val protection: ParkingProtection,
    val price: Double,
    val hasSecurity: Boolean,
    val owner: String,
    var reportingUsers: List<String>,
    var nbReviews: Int = 0,
    var avgScore: Double = 0.0,
    var nbReports: Int = 0,
    var nbMaxSeverityReports: Int = 0,
    val tile: Tile = TileUtils.getTileFromPoint(location.center)
) {

  /**
   * Finds a ParkingImage in the imageObjects list that matches the given imagePath.
   *
   * @param destinationPath The imagePath to match.
   * @return The ParkingImage with the matching imagePath, or null if no match is found.
   */
  fun findImageByPath(destinationPath: String): ParkingImage? {
    return imageObjects.find { it.imagePath == destinationPath }
  }

  companion object {
    const val TABLE_NAME = "parkings"
  }
}
/** data class representing an image of a Parking. */
data class ParkingImage(
    val uid: String = "",
    val imagePath: String = "",
    val owner: String = "",
    var nbReports: Int = 0,
    var nbMaxSeverityReports: Int = 0
)

/** data class representing a report of a Parking. */
data class ParkingReport(
    val uid: String = "",
    val reason: ParkingReportReason = ParkingReportReason.OTHER,
    val userId: String = "",
    val parking: String = "",
    val description: String = ""
)

/** data class representing a report of a ParkingImage. */
data class ImageReport(
    val uid: String = "",
    val reason: ImageReportReason = ImageReportReason.OTHER,
    val userId: String = "",
    val image: String = "",
    val description: String = ""
)

interface ParkingAttribute : DropDownableEnum

/** Enum class representing the capacity of a parking spot. */
enum class ParkingCapacity(override val description: String) : ParkingAttribute {
  XSMALL("Less than 10 spots"),
  SMALL("10-25 spots"),
  MEDIUM("26-50 spots"),
  LARGE("51-100 spots"),
  XLARGE("More than 100 spots")
}

// Reasons for reporting a Parking
enum class ParkingReportReason(override val description: String, val severity: Int) :
    DropDownableEnum {
  POOR_MAINTENANCE("This Spot has Poor Maintenance", 1),
  INACCESSIBLE("This Spot is Inaccessible", 2),
  ILLEGAL_SPOT("This Spot is in an Illegal Place", 3),
  SAFETY_CONCERN("This Spot is in an Dangerous Place", 3),
  INEXISTANT("This Spot does Not Exist", 3),
  OTHER("OTHER", 1)
}

// Reasons for reporting a Parking
enum class ImageReportReason(override val description: String, val severity: Int) :
    DropDownableEnum {
  USELESS("This Image isn't useful ", 1),
  MISLEADING("This Image doesn't Represent the Parking", 2),
  WRONG("This Image isn't related to the Parking", 3),
  SAFETY_CONCERN("I'm in this picture and I don't like it", 3),
  ILLEGAL_CONTENT("This Image contains illegal content", 3),
  OTHER("OTHER", 1)
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

  /**
   * Convert the location to a polygon.
   *
   * @return The polygon representing the location.
   */
  fun toPolygon(): Polygon {
    if (topLeft == null || topRight == null || bottomLeft == null || bottomRight == null) {
      Log.e("Location", "Cannot convert location to polygon, some corners are null")
      return Polygon.fromLngLats(listOf(listOf()))
    }
    return Polygon.fromLngLats(listOf(listOf(topLeft, topRight, bottomRight, bottomLeft, topLeft)))
  }

  /**
   * Compute the area of the location in square meters. The area is computed using the Turf
   * Measurement library.
   *
   * @return The area of the location in square meters.
   */
  fun computeArea(): Double {
    return TurfMeasurement.area(toPolygon())
  }

  /**
   * Compute the height and width of the location in meters.
   *
   * @return A pair of the height and width of the location in meters.
   */
  fun computeHeightAndWidth(): Pair<Double, Double> {
    if (topLeft == null || topRight == null || bottomLeft == null || bottomRight == null) {
      Log.e("Location", "Cannot compute height and width of location, some corners are null")
      return Pair(0.0, 0.0)
    }
    val height = TurfMeasurement.distance(topLeft, bottomLeft, "meters")
    val width = TurfMeasurement.distance(topLeft, topRight, "meters")
    return Pair(height, width)
  }

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
