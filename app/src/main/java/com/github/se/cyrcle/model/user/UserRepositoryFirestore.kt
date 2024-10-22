package com.github.se.cyrcle.model.user

import android.util.Log
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
            onFailure(Exception("User not found"))
          }
        }
        .addOnFailureListener { onFailure(it) }
  }

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .get()
        .addOnSuccessListener { querySnapshot ->
          Log.d("UserRepositoryFirestore", "getAllUsers")
          val users =
              querySnapshot.documents.mapNotNull { document ->
                Log.d("UserRepositoryFirestore", document.data.toString())
                document.data?.let { deserializeUser(it) }
              }
          onSuccess(users)
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
  // TODO: add a list of contributors to the parking spot
  override fun getUserContributedSpots(
      userId: String,
      onSuccess: (List<String>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection("parkings")
        .whereArrayContains("contributors", userId)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val parkingIds = querySnapshot.documents.map { it.id }
          onSuccess(parkingIds)
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
