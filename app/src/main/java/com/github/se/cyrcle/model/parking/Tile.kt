package com.github.se.cyrcle.model.parking

import com.mapbox.geojson.Point

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
              (tile.bottomLeft.latitude() * 100).toInt() / 100.0,
              (tile.bottomLeft.longitude() * 100).toInt() / 100.0)
      val roundedTopRight =
          Point.fromLngLat(
              (tile.topRight.latitude() * 100).toInt() / 100.0,
              (tile.topRight.longitude() * 100).toInt() / 100.0)
      return Tile(roundedBotomLeft, roundedTopRight)
    }

    /**
     * Returns all the tiles that are in the rectangle defined by the two points.
     *
     * @param bottomLeft the bottom left corner of the rectangle
     * @param topRight the top right corner of the rectangle
     * @return a list of all the tiles in the rectangle
     */
    fun getAllTilesInRectangle(bottomLeft: Point, topRight: Point): List<Tile> {
      val tiles = mutableListOf<Tile>()
      for (x in
          (bottomLeft.longitude() / TILE_SIZE).toInt()..(topRight.longitude() / TILE_SIZE)
                  .toInt()) {
        for (y in
            (bottomLeft.latitude() / TILE_SIZE).toInt()..(topRight.latitude() / TILE_SIZE)
                    .toInt()) {
          val currentTile =
              Tile(
                  Point.fromLngLat(x * TILE_SIZE, y * TILE_SIZE),
                  Point.fromLngLat((x + 1) * TILE_SIZE, (y + 1) * TILE_SIZE))
          tiles.add(roundTiles(currentTile))
        }
      }
      return tiles
    }
  }
}
