package com.github.se.cyrcle.model.parking

import com.mapbox.geojson.Point
import com.mapbox.turf.TurfMeasurement
import java.math.BigDecimal
import java.math.RoundingMode

const val TILE_SIZE = 0.1
/**
 * Represents a tile in the map.
 *
 * @param bottomLeft the bottom left corner of the tile
 * @param topRight the top right corner of the tile
 */
data class Tile(val bottomLeft: Point, val topRight: Point) {
  override fun toString(): String {
    return "Tile(bottomLeft=${bottomLeft.longitude()},${bottomLeft.latitude()}, topRight=${topRight.longitude()},${topRight.latitude()})"
  }

  companion object {
    /** Rounds the tile to the nearest 0.01 */
    private fun roundTiles(tile: Tile): Tile {
      val roundedBotomLeft =
          Point.fromLngLat(
              (tile.bottomLeft.longitude() * 100).toInt() / 100.0,
              (tile.bottomLeft.latitude() * 100).toInt() / 100.0)
      val roundedTopRight =
          Point.fromLngLat(
              (tile.topRight.longitude() * 100).toInt() / 100.0,
              (tile.topRight.latitude() * 100).toInt() / 100.0)
      return Tile(roundedBotomLeft, roundedTopRight)
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
     * Return all tiles that are in the circle defined by the center and the radius.
     *
     * @param center the center of the circle
     * @param radius the radius of the circle in meter
     * @return a set of tiles that are in the circle
     */
    fun getAllTilesInCircle(center: Point, radius: Double): Set<Tile> {
      val leftestPoint = TurfMeasurement.destination(center, radius, -90.0, "meters")
      val rightestPoint = TurfMeasurement.destination(center, radius, 90.0, "meters")
      val topPoint = TurfMeasurement.destination(center, radius, 0.0, "meters")
      val bottomPoint = TurfMeasurement.destination(center, radius, 180.0, "meters")
      println("Found tiles in circle")
      println(
          getAllTilesInRectangle(
                  Point.fromLngLat(leftestPoint.longitude(), bottomPoint.latitude()),
                  Point.fromLngLat(rightestPoint.longitude(), topPoint.latitude()))
              .size)
      return getAllTilesInRectangle(
          Point.fromLngLat(leftestPoint.longitude(), bottomPoint.latitude()),
          Point.fromLngLat(rightestPoint.longitude(), topPoint.latitude()))
    }
  }
}
