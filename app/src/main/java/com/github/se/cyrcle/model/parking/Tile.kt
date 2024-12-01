package com.github.se.cyrcle.model.parking

import android.annotation.SuppressLint
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfMeasurement
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.floor
import kotlin.math.pow

/**
 * Represents a tile in the map.
 *
 * @param bottomLeft the bottom left corner of the tile
 * @param topRight the top right corner of the tile
 * @param parkings the parkings in the tile
 * @param uid the unique identifier of the tile
 */
@Entity(tableName = "tiles")
data class Tile(
    val bottomLeft: Point,
    val topRight: Point,
    val parkings: MutableSet<Parking> = mutableSetOf(),
    @PrimaryKey val uid: String = getUidForPoint(bottomLeft)
) {
  override fun toString(): String {
    return "Tile(bottomLeft=${bottomLeft.longitude()},${bottomLeft.latitude()}, topRight=${topRight.longitude()},${topRight.latitude()})" +
        "    with ${parkings.size} parkings"
  }

  companion object {

    private const val TILE_SIZE = 0.1
    private const val DECIMALS = 2

    /**
     * Rounds the double to the smallest decimal value that is closest to it
     *
     * @param value the value to round
     * @param decimals the number of decimals to round to
     * @return the rounded value
     */
    private fun roundDouble(value: Double, decimals: Int): Double {
      val roundingFactor = 10.0.pow(decimals)
      return floor(value * roundingFactor) / roundingFactor
    }
    /**
     * Rounds the tile to the nearest 0.01
     *
     * @param tile Tile to round up
     * @return the rounded tile
     */
    private fun roundTiles(tile: Tile): Tile {

      val roundedBottomLeft =
          Point.fromLngLat(
              roundDouble(tile.bottomLeft.longitude(), DECIMALS),
              roundDouble(tile.bottomLeft.latitude(), DECIMALS))
      val roundedTopRight =
          Point.fromLngLat(
              roundDouble(tile.topRight.longitude(), DECIMALS),
              roundDouble(tile.topRight.latitude(), DECIMALS))
      return Tile(roundedBottomLeft, roundedTopRight)
    }

    /**
     * For a given point, returns the UID of the tile it is in
     *
     * @param point Point used to create the unique ID
     * @return a string in the format "<longitude>.xx_<latitude>.xx"
     */
    @SuppressLint("DefaultLocale")
    fun getUidForPoint(point: Point): String {
      val x = floor(point.longitude() / TILE_SIZE) * TILE_SIZE
      val y = floor(point.latitude() / TILE_SIZE) * TILE_SIZE

      val firstCoordinate = String.format("%.${DECIMALS}f", roundDouble(x, DECIMALS))
      val secondCoordinate = String.format("%.${DECIMALS}f", roundDouble(y, DECIMALS))
      return "${firstCoordinate}_${secondCoordinate}"
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
          BigDecimal(bottomLeft.longitude() / TILE_SIZE).setScale(7, RoundingMode.HALF_EVEN).toInt()
      val bottomLeftY =
          BigDecimal(bottomLeft.latitude() / TILE_SIZE).setScale(7, RoundingMode.HALF_EVEN).toInt()
      // round up topRight to the nearest TILE_SIZE
      val topRightX =
          BigDecimal(topRight.longitude() / TILE_SIZE).setScale(0, RoundingMode.UP).toInt()
      val topRightY =
          BigDecimal(topRight.latitude() / TILE_SIZE).setScale(0, RoundingMode.UP).toInt()

      for (x in bottomLeftX until topRightX) {
        for (y in bottomLeftY until topRightY) {
          tiles.add(
              roundTiles(
                  Tile(
                      Point.fromLngLat(x * TILE_SIZE, y * TILE_SIZE),
                      Point.fromLngLat((x + 1) * TILE_SIZE, (y + 1) * TILE_SIZE))))
        }
      }
      return tiles
    }

    /**
     * Returns the tile that contains the given point.
     *
     * @param point the point to search for
     * @return the tile that contains the point
     */
    fun getTileFromPoint(point: Point): Tile {
      val x = (point.longitude() / TILE_SIZE).toInt()
      val y = (point.latitude() / TILE_SIZE).toInt()
      return roundTiles(
          Tile(
              Point.fromLngLat(x * TILE_SIZE, y * TILE_SIZE),
              Point.fromLngLat((x + 1) * TILE_SIZE, (y + 1) * TILE_SIZE)))
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
}
