package com.github.se.cyrcle.io

import java.io.File
import org.junit.After
import org.junit.Before
import org.junit.Test

class MapTileStorageTest {

  private val dummyTile1 = byteArrayOf(0x00, 0x01, 0x02, 0x03)
  private val dummyTile2 = byteArrayOf(0x04, 0x05, 0x06, 0x07)
  private val cacheDir = "./data/map"
  private val extensionName = "mapbin"

  @Before
  fun cacheTearDown() {
    File(cacheDir).deleteRecursively()
  }

  @After
  fun storageTearDown() {
    File(cacheDir).deleteRecursively()
  }

  @Test
  fun testStoreTile() {
    val x = 0
    val y = 0
    val zoomLevel = 0

    storeTile(zoomLevel, x, y, dummyTile1)

    val expectedTileFile = File("$cacheDir/$zoomLevel/$x/$y.$extensionName")
    assert(expectedTileFile.exists())
    assert(expectedTileFile.isFile)
    assert(expectedTileFile.readBytes().contentEquals(dummyTile1))
  }

  @Test
  fun testStoreTileTwice() {
    val x = 0
    val y = 0
    val zoomLevel = 0

    storeTile(zoomLevel, x, y, dummyTile1)
    storeTile(zoomLevel, x, y, dummyTile2)

    val expectedTileFile = File("$cacheDir/$zoomLevel/$x/$y.$extensionName")
    assert(expectedTileFile.exists())
    assert(expectedTileFile.isFile)
    assert(expectedTileFile.readBytes().contentEquals(dummyTile1))
  }

  @Test
  fun testStoreTileForceWrite() {
    val x = 0
    val y = 0
    val zoomLevel = 0

    storeTile(zoomLevel, x, y, dummyTile1, false)
    storeTile(zoomLevel, x, y, dummyTile2, true)

    val expectedTileFile = File("$cacheDir/$zoomLevel/$x/$y.$extensionName")
    assert(expectedTileFile.exists())
    assert(expectedTileFile.isFile)
    assert(expectedTileFile.readBytes().contentEquals(dummyTile2))
  }

  @Test
  fun testLoadTile() {
    val x = 0
    val y = 0
    val zoomLevel = 0

    val expectedTileFile = File("$cacheDir/$zoomLevel/$x/$y.$extensionName")
    File("$cacheDir/$zoomLevel/$x").mkdirs()
    expectedTileFile.writeBytes(dummyTile1)

    val loadedTile = loadTile(zoomLevel, x, y)
    assert(loadedTile?.contentEquals(dummyTile1) ?: false)
  }

  @Test
  fun testLoadTileNonExistent() {
    val x = 0
    val y = 0
    val zoomLevel = 0

    val loadedTile = loadTile(zoomLevel, x, y)
    assert(loadedTile == null)
  }
}
