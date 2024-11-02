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
}
