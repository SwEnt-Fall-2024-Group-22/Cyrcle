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
  // Note : This Test only works if the tile size is 0.1
  @Test
  fun getAllTilesInCircleTest() {
    val tiles = Tile.getAllTilesInCircle(Point.fromLngLat(6.05, 46.05), 5.0)
    assert(tiles.size == 1)
    assert(tiles.contains(Tile(Point.fromLngLat(6.0, 46.0), Point.fromLngLat(6.1, 46.1))))

    val tiles2 = Tile.getAllTilesInCircle(Point.fromLngLat(6.5, 46.5), 5.0)
    assert(tiles2.size == 4)
    assert(tiles2.contains(Tile(Point.fromLngLat(6.4, 46.4), Point.fromLngLat(6.5, 46.5))))
    assert(tiles2.contains(Tile(Point.fromLngLat(6.5, 46.4), Point.fromLngLat(6.6, 46.5))))
    assert(tiles2.contains(Tile(Point.fromLngLat(6.4, 46.5), Point.fromLngLat(6.5, 46.6))))
    assert(tiles2.contains(Tile(Point.fromLngLat(6.5, 46.5), Point.fromLngLat(6.6, 46.6))))
  }
}
