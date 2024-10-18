package com.github.se.cyrcle.model.review

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ReviewRepositoryFirestore(private val db: FirebaseFirestore) : ReviewRepository {

  private val collectionPath = "reviews"

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

  override fun getAllReviews(onSuccess: (List<Review>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val reviews =
              querySnapshot.documents.mapNotNull { document ->
                document.data?.let { deserializeReview(it) }
              }
          onSuccess(reviews)
        }
        .addOnFailureListener { onFailure(it) }
  }

  override fun getReviewById(
      id: String,
      onSuccess: (Review) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(id)
        .get()
        .addOnSuccessListener { document ->
          val review = document.data?.let { deserializeReview(it) }
          if (review != null) {
            onSuccess(review)
          } else {
            onFailure(Exception("Parking not found"))
          }
        }
        .addOnFailureListener { onFailure(it) }
  }

  override fun getReviewsByOwner(
      owner: String,
      onSuccess: (List<Review>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("owner", owner) // Querying the "owner" (UID) field in Review documents
        .get()
        .addOnSuccessListener { querySnapshot ->
          val reviews =
              querySnapshot.documents.mapNotNull { document ->
                document.data?.let { deserializeReview(it) }
              }
          onSuccess(reviews)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getReviewByParking(
      parking: String,
      onSuccess: (List<Review>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("parking", parking) // Querying the "owner" (UID) field in Review documents
        .get()
        .addOnSuccessListener { querySnapshot ->
          val reviews =
              querySnapshot.documents.mapNotNull { document ->
                document.data?.let { deserializeReview(it) }
              }
          onSuccess(reviews)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun addReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(review.uid)
        .set(serializeReview(review))
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  override fun updateReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(review.uid)
        .set(serializeReview(review))
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  override fun deleteReviewById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(id)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  private fun serializeReview(Review: Review): Map<String, Any> {
    val gson = Gson()
    val type = object : TypeToken<Map<String, Any>>() {}.type
    return gson.fromJson(gson.toJson(Review), type)
  }

  private fun deserializeReview(data: Map<String, Any>): Review {
    val gson = Gson()
    return gson.fromJson(gson.toJson(data), Review::class.java)
  }
}
