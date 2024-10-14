package com.github.se.cyrcle.model.parking

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ParkingRepositoryFirestore(private val db: FirebaseFirestore) : ParkingRepository {

  private val collectionPath = "parkings"

  override fun onSignIn(onSuccess: () -> Unit) {
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override fun getParkings(onSuccess: (List<Parking>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val parkings =
              querySnapshot.documents.mapNotNull { document ->
                document.data?.let { deserializeParking(it) }
              }
          onSuccess(parkings)
        }
        .addOnFailureListener { onFailure(it) }
  }

  override fun getParkingById(
      id: String,
      onSuccess: (Parking) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(id)
        .get()
        .addOnSuccessListener { document ->
          val parking = document.data?.let { deserializeParking(it) }
          if (parking != null) {
            onSuccess(parking)
          } else {
            onFailure(Exception("Parking not found"))
          }
        }
        .addOnFailureListener { onFailure(it) }
  }

  override fun getParkingsBetween(
      start: Point,
      end: Point,
      onSuccess: (List<Parking>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (start.latitude > end.latitude || start.longitude > end.longitude) {
      onFailure(Exception("Invalid range"))
      return
    }
    db.collection(collectionPath)
        .whereGreaterThanOrEqualTo("location.center.latitude", start.latitude)
        .whereLessThanOrEqualTo("location.center.latitude", end.latitude)
        .whereGreaterThanOrEqualTo("location.center.longitude", start.longitude)
        .whereLessThanOrEqualTo("location.center.longitude", end.longitude)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val parkings =
              querySnapshot.documents.mapNotNull { document ->
                document.data?.let { deserializeParking(it) }
              }
          onSuccess(parkings)
        }
        .addOnFailureListener { e -> onFailure(e) }
  }

  override fun getKClosestParkings(
      location: Point,
      k: Int,
      onSuccess: (List<Parking>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // variables for radius search
    val initialRadius = 0.01
    val maxRadius = 0.5
    val step = 0.01

    var currentRadius = initialRadius
    var parkings = emptyList<Parking>()

    fun getMoreParkings() {
      getParkingsBetween(
          Point(location.latitude - currentRadius, location.longitude - currentRadius),
          Point(location.latitude + currentRadius, location.longitude + currentRadius),
          { newParkings ->
            parkings = (parkings + newParkings).distinctBy { it.uid }
            if (parkings.size < k && currentRadius < maxRadius) {
              currentRadius += step
              getMoreParkings()
            } else {
              onSuccess(parkings.sortedBy { it.location.center?.distanceTo(location) }.take(k))
            }
          },
          onFailure)
    }

    getMoreParkings()
  }

  override fun addParking(parking: Parking, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(parking.uid)
        .set(serializeParking(parking))
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  override fun updateParking(
      parking: Parking,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(parking.uid)
        .set(serializeParking(parking))
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  override fun deleteParkingById(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(id)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  private fun serializeParking(parking: Parking): Map<String, Any> {
    val gson = Gson()
    val type = object : TypeToken<Map<String, Any>>() {}.type
    return gson.fromJson(gson.toJson(parking), type)
  }

  private fun deserializeParking(map: Map<String, Any>): Parking {
    val gson = Gson()
    return gson.fromJson(gson.toJson(map), Parking::class.java)
  }
}
