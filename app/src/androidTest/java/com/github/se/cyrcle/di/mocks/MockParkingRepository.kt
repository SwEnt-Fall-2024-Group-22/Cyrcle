package com.github.se.cyrcle.di.mocks

import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfMeasurement
import javax.inject.Inject

class MockParkingRepository @Inject constructor() : ParkingRepository {
  private var uid = 0
  private val parkings = mutableListOf(TestInstancesParking.parking1)

  override fun getNewUid(): String {
    return (uid++).toString()
  }

  override fun onSignIn(onSuccess: () -> Unit) {
    onSuccess()
  }

  override fun getAllParkings(onSuccess: (List<Parking>) -> Unit, onFailure: (Exception) -> Unit) {
    if (parkings.none { it.uid == "" }) onSuccess(parkings)
    else onFailure(Exception("Error getting parkings"))
  }

  override fun getParkingById(
      id: String,
      onSuccess: (Parking) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (id == "" || parkings.none { it.uid == id }) onFailure(Exception("Error getting parking"))
    else onSuccess(parkings.find { it.uid == id }!!)
  }

  override fun getParkingsBetween(
      start: Point,
      end: Point,
      onSuccess: (List<Parking>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (start.latitude() > end.latitude() || start.longitude() > end.longitude())
        onFailure(Exception("Error getting parkings"))
    else
        onSuccess(
            parkings.filter {
              it.location.center.latitude() in start.latitude()..end.latitude() &&
                  it.location.center.longitude() in start.longitude()..end.longitude()
            })
  }

  override fun getKClosestParkings(
      location: Point,
      k: Int,
      onSuccess: (List<Parking>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (k < 0) onFailure(Exception("Error getting parkings"))
    else
        onSuccess(
            parkings
                .sortedBy {
                  TurfMeasurement.distance(TestInstancesParking.referencePoint, it.location.center)
                }
                .take(k))
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
}
