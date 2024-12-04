package com.github.se.cyrcle.model.parking

import com.mapbox.geojson.Point
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Unit test to check that the Tile extension function works as intended. */
@RunWith(RobolectricTestRunner::class)
class TileManagerTest {
  @Test
  fun getAllTilesInRectangleTest() {
    val tiles =
        TileUtils.getAllTilesInRectangle(
            Point.fromLngLat(6.0101, 46.0101),
            Point.fromLngLat(6.09, 46.09),
        )
    assertEquals(1, tiles.size)

    val tiles2 =
        TileUtils.getAllTilesInRectangle(
            Point.fromLngLat(6.010, 46.01),
            Point.fromLngLat(6.13, 46.13),
        )
    assertEquals(4, tiles2.size)
  }

  @Test
  fun getTileFromPointTest() {
    var uid = TileUtils.getTileFromPoint(Point.fromLngLat(6.05, 46.05))
    assertEquals("6.000_46.000", uid)

    uid = TileUtils.getTileFromPoint(Point.fromLngLat(6.6, 46.5))
    assertEquals("6.500_46.500", uid)
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
        TileUtils.getSmallestRectangleEnclosingCircle(Point.fromLngLat(6.05, 46.05), 10.0)
    assert(bottomLeft.longitude() < 6.05)
    assert(bottomLeft.latitude() < 46.05)
    assert(topRight.longitude() > 6.05)
    assert(topRight.latitude() > 46.05)
  }
}
