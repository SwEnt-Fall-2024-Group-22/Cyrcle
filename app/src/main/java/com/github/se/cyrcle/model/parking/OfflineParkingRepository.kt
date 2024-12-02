package com.github.se.cyrcle.model.parking

class UnSupportedOperationException(message: String) : Exception(message)

/** Repository for offline parking data */
interface OfflineParkingRepository : ParkingRepository {

  /**
   * Download the tiles
   *
   * @param tiles the tiles to download
   */
  fun downloadTiles(tiles: Set<Tile>, onComplete: () -> Unit)

  /**
   * Deletes the tiles from the local storage
   *
   * @param tileIDs the IDs of the tiles to delete
   * @return the tiles
   */
  fun deleteTiles(tileIDs: Set<String>, onComplete: () -> Unit)
}
