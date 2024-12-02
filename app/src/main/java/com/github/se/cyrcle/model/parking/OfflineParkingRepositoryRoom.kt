package com.github.se.cyrcle.model.parking

import com.github.se.cyrcle.model.parking.offline.TileDao
import com.github.se.cyrcle.model.parking.offline.TileDatabase
import com.mapbox.geojson.Point
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OfflineParkingRepositoryRoom @Inject constructor(tileDatabase: TileDatabase) :
    OfflineParkingRepository {

  private val tileManager: TileDao = tileDatabase.tileDao

  private val coroutine = CoroutineScope(Dispatchers.IO)

  override fun downloadTiles(tiles: Set<Tile>, onComplete: () -> Unit) {
    coroutine.launch {
      tiles.forEach { tileManager.upsert(it) }
      onComplete()
    }
  }

  override fun deleteTiles(tileIDs: Set<String>, onComplete: () -> Unit) {
    coroutine.launch {
      tileIDs.forEach { tileManager.delete(it) }
      onComplete()
    }
  }

  override fun getNewUid(): String {
    throw UnSupportedOperationException("Get new UID called on offline repository")
  }

  override fun onSignIn(onSuccess: () -> Unit) {
    // Do nothing
    onSuccess()
  }

  override fun getParkingById(
      id: String,
      onSuccess: (Parking) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    TODO("Not yet implemented")
  }

  override fun getParkingsBetween(
      start: Point,
      end: Point,
      onSuccess: (List<Parking>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    TODO("Not yet implemented")
  }

  override fun addParking(parking: Parking, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    onFailure(Exception("Add parking called on offline repository"))
  }

  override fun updateParking(
      parking: Parking,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onFailure(Exception("Update parking called on offline repository"))
  }

  override fun getReportsForParking(
      parkingId: String,
      onSuccess: (List<ParkingReport>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onFailure(UnSupportedOperationException("Get reports for parking called on offline repository"))
  }

  override fun deleteParkingById(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onFailure(UnSupportedOperationException("Delete parking by ID called on offline repository"))
  }

  override fun getParkingsByListOfIds(
      ids: List<String>,
      onSuccess: (List<Parking>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    TODO("Not yet implemented")
  }

  override fun addReport(
      report: ParkingReport,
      onSuccess: (ParkingReport) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onFailure(UnSupportedOperationException("Add report called on offline repository"))
  }
}
