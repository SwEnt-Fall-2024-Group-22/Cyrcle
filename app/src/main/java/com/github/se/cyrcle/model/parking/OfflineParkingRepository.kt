package com.github.se.cyrcle.model.parking

class UnSupportedOperationException(message: String) : Exception(message)

/** Repository for offline parking data */
interface OfflineParkingRepository : ParkingRepository {

  /**
   * Download the parkings
   *
   * @param parkings the parkings to download
   */
  fun downloadParkings(parkings: Set<Parking>, onComplete: () -> Unit)

  /**
   * Deletes the parkings contained within the set of tiles from local storage
   *
   * @param tileIDs the IDs of the tiles to delete
   */
  fun deleteTiles(tileIDs: Set<String>, onComplete: () -> Unit)
}
