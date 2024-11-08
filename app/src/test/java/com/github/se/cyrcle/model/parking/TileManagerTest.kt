package com.github.se.cyrcle.model.parking

import com.mapbox.geojson.Point
import org.junit.Test

/** Unit test to check that the Tile extension function works as intended. */
class TileManagerTest {
  @Test
  fun getAllTilesInRectangleTest() {
    val tiles =
        Tile.getAllTilesInRectangle(
            Point.fromLngLat(6.0101, 46.0101),
            Point.fromLngLat(6.09, 46.09),
        )
    assert(tiles.size == 1)

    val tiles2 =
        Tile.getAllTilesInRectangle(
            Point.fromLngLat(6.010, 46.01),
            Point.fromLngLat(6.13, 46.13),
        )
    assert(tiles2.size == 4)
  }

  @Test
  fun getTileFromPointTest() {
    val tile =
        Tile.getTileFromPoint(
            Point.fromLngLat(6.05, 46.05),
        )
    assert(tile == Tile(Point.fromLngLat(6.0, 46.0), Point.fromLngLat(6.1, 46.1)))
  }

  /*
   * Test the smallest rectangle that contains a circle
   * The circle is centered at 6.05, 46.05 with a radius of 10
   * The rectangle should have a bottom left corner smaller than 6.05, 46.05
   * and a top right corner bigger than 6.05, 46.05
   * This function is pretty simple, so it doesn't need more tests.
   */
  @Test
  fun getSmallestRectangleEnclosingCircleTest() {
    val (bottomLeft, topRight) =
        Tile.getSmallestRectangleEnclosingCircle(Point.fromLngLat(6.05, 46.05), 10.0)
    assert(bottomLeft.longitude() < 6.05)
    assert(bottomLeft.latitude() < 46.05)
    assert(topRight.longitude() > 6.05)
    assert(topRight.latitude() > 46.05)
  }
}
