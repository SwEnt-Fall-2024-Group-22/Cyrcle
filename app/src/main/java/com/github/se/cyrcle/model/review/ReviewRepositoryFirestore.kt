package com.github.se.cyrcle.model.review

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
          val review = document.data?.let { deserializeReview(it) }
          if (review != null) {
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
    db.collection(collectionPath)
        .document(id)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  override fun addReport(
      report: ReviewReport,
      onSuccess: (ReviewReport) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val reportId = getNewUid() // Generate a new unique ID for the report
    val reportData =
        mapOf(
            "id" to report.uid,
            "reason" to report.reason,
            "user" to report.userId,
            "review" to report.review)
    db.collection(collectionPath)
        .document(report.review)
        .collection("reports")
        .document(reportId)
        .set(reportData)
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
    val json = gson.toJson(processedData)
    return gson.fromJson(json, Review::class.java)
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
