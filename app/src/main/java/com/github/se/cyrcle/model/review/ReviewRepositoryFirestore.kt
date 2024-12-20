package com.github.se.cyrcle.model.review

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import javax.inject.Inject

class TimestampAdapter : JsonSerializer<Timestamp>, JsonDeserializer<Timestamp> {
  override fun serialize(
      src: Timestamp,
      typeOfSrc: Type,
      context: JsonSerializationContext
  ): JsonElement {
    val jsonObject = JsonObject()
    jsonObject.addProperty(
        "seconds", src.seconds.toString()) // Convert to String to prevent scientific notation
    jsonObject.addProperty(
        "nanoseconds",
        src.nanoseconds.toString()) // Convert to String to prevent scientific notation
    return jsonObject
  }

  override fun deserialize(
      json: JsonElement,
      typeOfT: Type,
      context: JsonDeserializationContext
  ): Timestamp {
    val jsonObject = json.asJsonObject
    val seconds = jsonObject.get("seconds").asString.toLong() // Convert back to Long from String
    val nanoseconds =
        jsonObject.get("nanoseconds").asString.toInt() // Convert back to Int from String
    return Timestamp(seconds, nanoseconds)
  }
}

class ReviewRepositoryFirestore @Inject constructor(private val db: FirebaseFirestore) :
    ReviewRepository {

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

  override fun getReviewById(
      id: String,
      onSuccess: (Review) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(id)
        .get()
        .addOnSuccessListener { document ->
          val data = document.data
          if (data != null) {
            val review = deserializeReview(data) // Use explicit deserialization
            onSuccess(review)
          } else {
            onFailure(Exception("Review not found"))
          }
        }
        .addOnFailureListener { onFailure(it) }
  }

  override fun getReviewsByOwnerId(
      ownerId: String,
      onSuccess: (List<Review>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("owner", ownerId) // Querying the "owner" (UID) field in Review documents
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

  override fun getReviewsByParkingId(
      parkingId: String,
      onSuccess: (List<Review>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("parking", parkingId) // Querying the "owner" (UID) field in Review documents
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
    val reviewDocRef = db.collection(collectionPath).document(id)
    val reportsCollectionRef = reviewDocRef.collection("reports")

    // Fetch and delete all reports in the subcollection
    reportsCollectionRef
        .get()
        .addOnSuccessListener { querySnapshot ->
          val batch = db.batch()

          // Add delete operations for each document in the reports subcollection
          for (document in querySnapshot.documents) {
            batch.delete(document.reference)
          }

          // Commit the batch to delete all reports
          batch
              .commit()
              .addOnSuccessListener {
                // After deleting reports, delete the review document
                reviewDocRef
                    .delete()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
              }
              .addOnFailureListener { exception ->
                onFailure(exception) // Handle batch failure
              }
        }
        .addOnFailureListener { exception ->
          onFailure(exception) // Handle fetching reports failure
        }
  }

  override fun getReportsForReview(
      reviewId: String,
      onSuccess: (List<ReviewReport>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath) // Access the "reviews" collection
        .document(reviewId) // Access the specific review by its ID
        .collection("reports") // Access the "reports" sub-collection
        .get()
        .addOnSuccessListener { querySnapshot ->
          val reports =
              querySnapshot.documents.mapNotNull { document ->
                document.toObject(ReviewReport::class.java) // Convert each document to ReviewReport
              }
          onSuccess(reports) // Pass the list of reports to the success callback
        }
        .addOnFailureListener { exception ->
          Log.e("ReviewRepository", "Error fetching reports for review: ${exception.message}")
          onFailure(exception) // Pass the error to the failure callback
        }
  }

  override fun addReport(
      report: ReviewReport,
      onSuccess: (ReviewReport) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val reportId = getNewUid() // Generate a new unique ID for the report
    db.collection(collectionPath)
        .document(report.review)
        .collection("reports")
        .document(reportId)
        .set(report)
        .addOnSuccessListener { onSuccess(report) }
        .addOnFailureListener { onFailure(it) }
  }

  private val gson: Gson =
      GsonBuilder()
          .registerTypeAdapter(
              Timestamp::class.java, TimestampAdapter()) // Register Timestamp adapter
          .create()

  fun serializeReview(review: Review): Map<String, Any> {
    // Serialize the Review object directly to a Map using Gson
    val json = gson.toJson(review)
    val type = object : TypeToken<Map<String, Any>>() {}.type
    return gson.fromJson(json, type)
  }

  fun deserializeReview(data: Map<String, Any>): Review {
    val processedData = data.toMutableMap()
    val timeMap = data["time"] as? Map<String, Any>
    if (timeMap != null) {
      val timestamp = createTimestamp(timeMap)
      if (timestamp != null) {
        processedData["time"] = timestamp
      }
    }

    // Ensure default values for missing fields
    val nbReports = (data["nbReports"] as? Number)?.toInt() ?: 0
    val nbMaxSeverityReports = (data["nbMaxSeverityReports"] as? Number)?.toInt() ?: 0
    val dislikedBy: List<String> = data["dislikedBy"] as? List<String> ?: emptyList()
    val likedBy: List<String> = data["likedBy"] as? List<String> ?: emptyList()
    val reportingUsers = (data["reportingUsers"] as? List<String>) ?: emptyList()

    // Convert the map to JSON, then deserialize into a Review object
    val json = gson.toJson(processedData)
    val review = gson.fromJson(json, Review::class.java)

    // Set default values for missing fields directly in the Review object
    return review.copy(
        nbReports = nbReports,
        nbMaxSeverityReports = nbMaxSeverityReports,
        reportingUsers = reportingUsers,
        likedBy = likedBy,
        dislikedBy = dislikedBy)
  }

  fun createTimestamp(timeAttributes: Map<String, Any>): Timestamp? {
    val seconds = timeAttributes["seconds"] as? Number
    val nanoseconds = timeAttributes["nanoseconds"] as? Number

    return if (seconds != null && nanoseconds != null) {
      Timestamp(seconds.toLong(), nanoseconds.toInt())
    } else {
      null
    }
  }
}
