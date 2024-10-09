package com.github.se.cyrcle.model.parking

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
            task.result?.mapNotNull { document -> deserializeParking(document.data) } ?: emptyList()
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
                task.result?.mapNotNull { document -> deserializeParking(document.data) }
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
    // variables for radius search
    val initialRadius = 0.01
    val maxRadius = 0.2
    val step = 0.01

    var currentRadius = initialRadius
    var parkings = emptyList<Parking>()

    fun getMoreParkings() {
      getParkingsBetween(
          Pair(location.first - currentRadius, location.second - currentRadius),
          Pair(location.first + currentRadius, location.second + currentRadius),
          { newParkings ->
            parkings = (parkings + newParkings).distinctBy { it.uid }
            if (parkings.size < k && currentRadius < maxRadius) {
              currentRadius += step
              getMoreParkings()
            } else {
              onSuccess(parkings.take(k))
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
