package com.github.se.cyrcle.model.user

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import javax.inject.Inject

class UserRepositoryFirestore
@Inject
constructor(private val db: FirebaseFirestore, private val auth: FirebaseAuth) : UserRepository {

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
          val userPublic = deserializeUser(document.data!!)

          db.collection(collectionPath)
              .document(userId)
              .collection("private")
              .document("details")
              .get()
              .addOnSuccessListener { detailsDocument ->
                val userDetails = deserializeUserDetails(detailsDocument.data!!)
                onSuccess(User(userPublic, userDetails))
              }
              .addOnFailureListener(onFailure)
        }
        .addOnFailureListener(onFailure)
  }

  override fun getUid(): String {
    val uid = auth.currentUser?.uid ?: ""
    if (uid.isEmpty()) throw Exception("User not signed in")
    return uid
  }

  override fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val users =
              querySnapshot.documents.mapNotNull { document ->
                val userPublic = deserializeUser(document.data!!)
                User(userPublic, null)
              }
          onSuccess(users)
        }
        .addOnFailureListener(onFailure)
  }

  override fun addUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    if (user.details == null) {
      onFailure(Exception("User details are required"))
      return
    }
    db.collection(collectionPath)
        .document(user.public.userId)
        .get()
        .addOnSuccessListener {
          if (it.exists()) {
            onSuccess()
            return@addOnSuccessListener
          } else {
            db.collection(collectionPath)
                .document(user.public.userId)
                .set(user.public)
                .addOnSuccessListener {
                  db.collection(collectionPath)
                      .document(user.public.userId)
                      .collection("private")
                      .document("details")
                      .set(user.details)
                      .addOnSuccessListener { onSuccess() }
                      .addOnFailureListener(onFailure)
                }
                .addOnFailureListener(onFailure)
          }
        }
        .addOnFailureListener(onFailure)
  }

  override fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    if (user.details == null) {
      onFailure(Exception("User details are required"))
      return
    }
    db.collection(collectionPath)
        .document(user.public.userId)
        .set(user.public)
        .addOnSuccessListener {
          db.collection(collectionPath)
              .document(user.public.userId)
              .collection("private")
              .document("details")
              .set(user.details)
              .addOnSuccessListener { onSuccess() }
              .addOnFailureListener { onFailure(it) }
        }
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

  private fun deserializeUser(data: Map<String, Any>): UserPublic {
    val gson = Gson()
    return gson.fromJson(gson.toJson(data), UserPublic::class.java)
  }

  private fun deserializeUserDetails(data: Map<String, Any>): UserDetails {
    val gson = Gson()
    return gson.fromJson(gson.toJson(data), UserDetails::class.java)
  }
}
