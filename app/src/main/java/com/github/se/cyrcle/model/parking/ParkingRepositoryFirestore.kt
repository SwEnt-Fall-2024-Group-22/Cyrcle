package com.github.se.cyrcle.model.parking

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class ParkingRepositoryFirestore(private val db: FirebaseFirestore) : ParkingRepository {

  private val collectionPath = "parkings"

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override fun getParkings(onSuccess: (List<Parking>) -> Unit, onFailure: (Exception) -> Unit) {
    Log.d("ParkingRepositoryFirestore", "getParkings")
    db.collection(collectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val parkings =
            task.result?.mapNotNull { document -> document.toObject(Parking::class.java) }
                ?: emptyList()
        onSuccess(parkings)
      } else {
        task.exception?.let { e ->
          Log.e("ParkingRepositoryFirestore", "Error getting documents", e)
          onFailure(e)
        }
      }
    }
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
          val parking = document.toObject(Parking::class.java)
          if (parking != null) {
            onSuccess(parking)
          } else {
            onFailure(Exception("Parking not found"))
          }
        }
        .addOnFailureListener { onFailure(it) }
  }

  override fun getParkingsBetween(
      start: Pair<Double, Double>,
      end: Pair<Double, Double>,
      onSuccess: (List<Parking>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {

    db.collection(collectionPath)
        .whereGreaterThanOrEqualTo("location.first", start.first)
        .whereLessThanOrEqualTo("location.first", end.first)
        .whereGreaterThanOrEqualTo("location.second", start.second)
        .whereLessThanOrEqualTo("location.second", end.second)
        .get()
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            val parkings =
                task.result?.mapNotNull { document -> document.toObject(Parking::class.java) }
                    ?: emptyList()
            onSuccess(parkings)
          } else {
            task.exception?.let { e ->
              Log.e("ParkingRepositoryFirestore", "Error getting documents", e)
              onFailure(e)
            }
          }
        }
  }

  override fun getKClosestParkings(
      location: Pair<Double, Double>,
      k: Int,
      onSuccess: (List<Parking>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    TODO("Not yet implemented")
  }

  override fun addParking(parking: Parking, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(parking.uid)
        .set(parking)
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
        .set(parking)
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
}
