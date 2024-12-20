package com.github.se.cyrcle.di.mocks

import com.github.se.cyrcle.model.parking.ImageReport
import com.github.se.cyrcle.model.parking.ImageReportReason
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingReport
import com.github.se.cyrcle.model.parking.ParkingReportReason
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.parking.Tile
import com.github.se.cyrcle.model.parking.offline.OfflineParkingRepository
import javax.inject.Inject

class MockOfflineParkingRepository @Inject constructor() : OfflineParkingRepository {
  var uid: Int = 0
  private var parkings = mutableListOf<Parking>()

  private val reports =
      mutableListOf(
          ParkingReport(
              "1", ParkingReportReason.INEXISTANT, "1", TestInstancesParking.parking1.uid, ""))
  private val reports2 =
      mutableListOf(ImageReport("1", ImageReportReason.WRONG, "2", "parking/2/b.png", ""))

  override fun downloadParkings(parkings: List<Parking>, onComplete: () -> Unit) {
    this.parkings += parkings
    onComplete()
  }

  override fun deleteTiles(tileIDs: Set<String>, onComplete: () -> Unit) {
    parkings = parkings.filterNot { it.tile in tileIDs }.toMutableList()
    onComplete()
  }

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

  override fun getReportsForImage(
      parkingId: String,
      imageId: String,
      onSuccess: (List<ImageReport>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (parkingId.isEmpty()) {
      onFailure(Exception("Parking ID cannot be empty"))
    } else {
      val associatedReports = reports2.filter { it.uid == parkingId }
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
    else onSuccess(parkings.filter { it.tile == tile })
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

  override fun addImageReport(
      report: ImageReport,
      parking: String,
      onSuccess: (ImageReport) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (report.uid == "") onFailure(Exception("Error adding report"))
    onSuccess(reports2[0])
  }
}
