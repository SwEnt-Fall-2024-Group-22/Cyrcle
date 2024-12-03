package com.github.se.cyrcle.di.mocks

import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingReport
import com.github.se.cyrcle.model.parking.ParkingReportReason
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.parking.Tile
import com.github.se.cyrcle.model.parking.online.ParkingRepository
import javax.inject.Inject

class MockParkingRepository @Inject constructor() : ParkingRepository {
  var uid = 0
  private val parkings = mutableListOf(TestInstancesParking.parking1)
  private val reports =
      mutableListOf(
          ParkingReport(
              "1", ParkingReportReason.INEXISTANT, "1", TestInstancesParking.parking1.uid, ""))

  override fun getNewUid(): String {
    return (uid++).toString()
  }

  override fun onSignIn(onSuccess: () -> Unit) {
    onSuccess()
  }

  override fun getReportsForParking(
      parkingId: String,
      onSuccess: (List<ParkingReport>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (parkingId.isEmpty()) {
      onFailure(Exception("Parking ID cannot be empty"))
    } else {
      val associatedReports = reports.filter { it.parking == parkingId }
      if (associatedReports.isEmpty()) {
        onFailure(Exception("No reports found for the given parking ID"))
      } else {
        onSuccess(associatedReports)
      }
    }
  }

  override fun getParkingById(
      id: String,
      onSuccess: (Parking) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (id == "" || parkings.none { it.uid == id }) onFailure(Exception("Error getting parking"))
    else onSuccess(parkings.find { it.uid == id }!!)
  }

  override fun getParkingsForTile(
      tile: Tile,
      onSuccess: (List<Parking>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (tile == "") onFailure(Exception("Error getting parkings"))
    else onSuccess(parkings.filter { it.tileUID == tile })
  }

  override fun addParking(parking: Parking, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    if (parking.uid == "") onFailure(Exception("Error adding parking"))
    else {
      parkings.add(parking)
      onSuccess()
    }
  }

  override fun updateParking(
      parking: Parking,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (parking.uid == "" || parkings.none { it.uid == parking.uid })
        onFailure(Exception("Error updating parking"))
    else {
      parkings.remove(parkings.find { it.uid == parking.uid })
      parkings.add(parking)
      onSuccess()
    }
  }

  override fun deleteParkingById(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (id == "" || parkings.none { it.uid == id }) onFailure(Exception("Error deleting parking"))
    else {
      parkings.remove(parkings.find { it.uid == id })
      onSuccess()
    }
  }

  override fun getParkingsByListOfIds(
      ids: List<String>,
      onSuccess: (List<Parking>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (ids.any { it == "" }) onFailure(Exception("Error getting parkings"))
    else onSuccess(parkings.filter { it.uid in ids })
  }

  override fun addReport(
      report: ParkingReport,
      onSuccess: (ParkingReport) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (report.uid == "") onFailure(Exception("Error adding report"))
    onSuccess(reports[0])
  }
}
