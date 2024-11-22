package com.github.se.cyrcle.io

import java.io.File
import java.io.IOException

private const val CACHE_DIR = "./cache/map"
private const val EXTENSION_NAME = "mapbin"

/**
 * Store a tile at the specified zoom level and coordinates.
 *
 * @param zoomLevel The zoom level of the tile.
 * @param x The x-coordinate of the tile.
 * @param y The y-coordinate of the tile.
 * @param tileData The byte array of the tile data.
 * @throws IOException If an I/O error occurs.
 */
@Throws(IOException::class)
fun storeTile(zoomLevel: Int, x: Int, y: Int, tileData: ByteArray) {
  val tileDir = File("$CACHE_DIR/$zoomLevel/$x")
  if (!tileDir.exists()) {
    tileDir.mkdirs()
  }
  val tileFile = File(tileDir, "$y.$EXTENSION_NAME")
  if (tileFile.exists()) return // Tile should change, there is no point in updating them
  tileFile.writeBytes(tileData)
}

/**
 * Load a tile from the specified zoom level and coordinates.
 *
 * @param zoomLevel The zoom level of the tile.
 * @param x The x-coordinate of the tile.
 * @param y The y-coordinate of the tile.
 * @return The byte array of the tile data, or null if the tile does not exist.
 * @throws IOException If an I/O error occurs.
 */
@Throws(IOException::class)
fun loadTile(zoomLevel: Int, x: Int, y: Int): ByteArray? {
  val tileFile = File("$CACHE_DIR/$zoomLevel/$x/$y.$EXTENSION_NAME")
  return if (tileFile.exists()) {
    tileFile.readBytes()
  } else {
    null
  }
}
