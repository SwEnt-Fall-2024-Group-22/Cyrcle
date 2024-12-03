package com.github.se.cyrcle.model.parking

import com.mapbox.geojson.Point
import com.mapbox.turf.TurfMeasurement
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

typealias Tile = String

object TileUtils {
  // Use inverse powers of 2 to avoid floating point errors
  private const val TILE_SIZE = 0.125
  private const val INV_TILE_SIZE = 8

  // Need 3 decimals to represent the ".125" in the TILE_SIZE
  private const val DECIMALS = 3

  /**
   * For a given point, returns the UID of the tile it is in
   *
   * @param point Point used to create the unique ID
   * @return a string in the format "<longitude>.xx_<latitude>.xx"
   */
  fun getTileFromPoint(point: Point): String {
    val x = (point.longitude() * INV_TILE_SIZE).toInt() * TILE_SIZE
    val y = (point.latitude() * INV_TILE_SIZE).toInt() * TILE_SIZE

    // Locale does not matter, it just has to be consistent between devices
    val xFormated = String.format(Locale.US, "%.${DECIMALS}f", x)
    val yFormated = String.format(Locale.US, "%.${DECIMALS}f", y)
    return "${xFormated}_${yFormated}"
  }

  /**
   * Returns all the tiles that are in the rectangle defined by the two points.
   *
   * @param bottomLeft the bottom left corner of the rectangle
   * @param topRight the top right corner of the rectangle
   * @return a list of all the tiles in the rectangle
   */
  fun getAllTilesInRectangle(bottomLeft: Point, topRight: Point): Set<Tile> {
    val tiles = mutableSetOf<Tile>()
    // round with a formatter to the 7 digits the bottomLeft and topRight

    /* round down bottomLeft to the nearest TILE_SIZE
    Note : The BigDecimal is used to avoid floating point errors i.e 7.01 turning into 7.0099999999999998
    Making the repo look for the tile containing 7.0099999999999998 when not needed
     */
    val bottomLeftX =
        BigDecimal(bottomLeft.longitude() * INV_TILE_SIZE)
            .setScale(7, RoundingMode.HALF_EVEN)
            .toInt()
    val bottomLeftY =
        BigDecimal(bottomLeft.latitude() * INV_TILE_SIZE)
            .setScale(7, RoundingMode.HALF_EVEN)
            .toInt()
    // round up topRight to the nearest TILE_SIZE
    val topRightX =
        BigDecimal(topRight.longitude() * INV_TILE_SIZE).setScale(0, RoundingMode.UP).toInt()
    val topRightY =
        BigDecimal(topRight.latitude() * INV_TILE_SIZE).setScale(0, RoundingMode.UP).toInt()

    for (x in bottomLeftX until topRightX) {
      for (y in bottomLeftY until topRightY) {
        tiles.add(getTileFromPoint(Point.fromLngLat(x * TILE_SIZE, y * TILE_SIZE)))
      }
    }

    return tiles
  }

  /**
   * Return a pair of point representing the bottom left and top right corner of the smallest
   * rectangle that contains the circle.
   *
   * @param center the center of the circle
   * @param radius the radius of the circle in meter
   * @return a pair of point representing the bottom left and top right corner of the smallest
   */
  fun getSmallestRectangleEnclosingCircle(center: Point, radius: Double): Pair<Point, Point> {
    val westPoint = TurfMeasurement.destination(center, radius, -90.0, "meters")
    val eastPoint = TurfMeasurement.destination(center, radius, 90.0, "meters")
    val northPoint = TurfMeasurement.destination(center, radius, 0.0, "meters")
    val southPoint = TurfMeasurement.destination(center, radius, 180.0, "meters")
    return Pair(
        Point.fromLngLat(westPoint.longitude(), southPoint.latitude()),
        Point.fromLngLat(eastPoint.longitude(), northPoint.latitude()))
  }
}
