package com.github.se.cyrcle.model.user

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserRepositoryFirestore(private val db: FirebaseFirestore) : UserRepository {

  private val collectionPath = "users"

  override fun onSignIn(onSuccess: () -> Unit) {
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override fun getUserById(
      userId: String,
      onSuccess: (User) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(userId)
        .get()
        .addOnSuccessListener { document ->
          val user = document.data?.let { deserializeUser(it) }
          if (user != null) {
            onSuccess(user)
          } else {
            onFailure(Exception("Parking not found"))
          }
        }
        .addOnFailureListener { onFailure(it) }
  }

  override fun addUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(user.userId)
        .set(serializeUser(user))
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  override fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(user.userId)
        .set(serializeUser(user))
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  override fun deleteUserById(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(userId)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }
  /*
  // Technically this keeps track of lastKnownLocation
  override fun updateUserLocation(
      userId: String,
      location: Location,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val locationData =
        mapOf(
            "center" to
                mapOf(
                    "latitude" to location.center.latitude(),
                    "longitude" to location.center.longitude()),
            "topLeft" to
                location.topLeft?.let {
                  mapOf("latitude" to it.latitude(), "longitude" to it.longitude())
                },
            "topRight" to
                location.topRight?.let {
                  mapOf("latitude" to it.latitude(), "longitude" to it.longitude())
                },
            "bottomLeft" to
                location.bottomLeft?.let {
                  mapOf("latitude" to it.latitude(), "longitude" to it.longitude())
                },
            "bottomRight" to
                location.bottomRight?.let {
                  mapOf("latitude" to it.latitude(), "longitude" to it.longitude())
                })

    db.collection(collectionPath)
        .document(userId)
        .update("lastKnownLocation", locationData)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  override fun getLastKnownLocation(
      userId: String,
      onSuccess: (Location?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(userId)
        .get()
        .addOnSuccessListener { document ->
          val user = document.toObject(User::class.java)
          onSuccess(user?.lastKnownLocation)
        }
        .addOnFailureListener { onFailure(it) }
  } */

  // TODO(I expect someone to change ParkingRepositoryFirestore
  // by adding a contributorId on addParking and updateParking.
  // I don't have any idea how to do this differently otherwise...)
  /*
   override fun getUserContributedSpots(
       userId: String,
       onSuccess: (List<String>) -> Unit,
       onFailure: (Exception) -> Unit
   ) {
     db.collection("parkings")
         .whereEqualTo("contributorId", userId)
         .get()
         .addOnSuccessListener { result ->
           val parkings =
               result.documents.mapNotNull { document -> document.toObject(String::class.java) }
           onSuccess(parkings)
         }
         .addOnFailureListener { onFailure(it) }
   }
  */

  private fun serializeUser(user: User): Map<String, Any> {
    val gson = Gson()
    val type = object : TypeToken<Map<String, Any>>() {}.type
    return gson.fromJson(gson.toJson(user), type)
  }

  private fun deserializeUser(data: Map<String, Any>): User {
    val gson = Gson()
    return gson.fromJson(gson.toJson(data), User::class.java)
  }
}
