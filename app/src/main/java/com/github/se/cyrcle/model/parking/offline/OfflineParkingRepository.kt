package com.github.se.cyrcle.model.parking.offline

import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.Tile
import com.github.se.cyrcle.model.parking.online.ParkingRepository

class UnSupportedOperationException(message: String) : Exception(message)

/** Repository for offline parking data */
interface OfflineParkingRepository : ParkingRepository {

  /**
   * Download the parkings
   *
   * @param parkings the parkings to download
   */
  fun downloadParkings(parkings: List<Parking>, onComplete: () -> Unit)

  /**
   * Deletes the parkings contained within the set of tiles from local storage
   *
   * @param tiles the IDs of the tiles to delete
   */
  fun deleteTiles(tiles: Set<Tile>, onComplete: () -> Unit)
}
