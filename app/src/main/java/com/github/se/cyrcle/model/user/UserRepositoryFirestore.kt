package com.github.se.cyrcle.model.user

import android.util.Log
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
          val userPublic: UserPublic =
              try {
                Log.d("UserRepositoryFirestore", " Correctly fetched UserPublic")
                deserializeUserPublic(document.data!!)
              } catch (e: Exception) {
                Log.d("UserRepositoryFirestore", "Error fetching UserPublic")
                onFailure(e)
                return@addOnSuccessListener
              }

          db.collection(collectionPath)
              .document(userId)
              .collection("private")
              .document("details")
              .get()
              .addOnSuccessListener { detailsDocument ->
                val userDetails = deserializeUserDetails(detailsDocument.data!!)
                onSuccess(User(userPublic, userDetails))
              }
              /*
              We still call onSuccess even if the request to get the user's private information fails.
              This happens when trying to get the information of a user that is not the current user.
              The user's public information is still useful to have. i.e when loading the reviewer's name in a review.
              Another solution would be to make two separate function, one for the public information and one for the private information.
              but this seems easier and robust.
              */
              .addOnFailureListener { onSuccess(User(userPublic, null)) }
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
                val userPublic = deserializeUserPublic(document.data!!)
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

  private fun deserializeUserPublic(data: Map<String, Any>): UserPublic {
    val gson = Gson()
    return gson.fromJson(gson.toJson(data), UserPublic::class.java)
  }

  private fun deserializeUserDetails(data: Map<String, Any>): UserDetails {
    val gson = Gson()
    return gson.fromJson(gson.toJson(data), UserDetails::class.java)
  }
}
