package com.github.se.cyrcle.model.report

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import javax.inject.Inject

/**
 * A Firestore-backed implementation of the ReportedObjectRepository. Manages CRUD operations for
 * reported objects in a Firestore database.
 *
 * @property db The Firestore instance used for database operations.
 */
class ReportReasonAdapter : JsonDeserializer<ReportReason>, JsonSerializer<ReportReason> {
  override fun serialize(
      src: ReportReason,
      typeOfSrc: Type,
      context: JsonSerializationContext
  ): JsonElement {
    val jsonObject = JsonObject()
    jsonObject.addProperty("type", src::class.java.simpleName)
    jsonObject.add("data", context.serialize(src))
    return jsonObject
  }

  override fun deserialize(
      json: JsonElement,
      typeOfT: Type,
      context: JsonDeserializationContext
  ): ReportReason {
    val jsonObject = json.asJsonObject
    val type = jsonObject.get("type").asString
    val data = jsonObject.get("data")
    return when (type) {
      "Review" -> context.deserialize(data, ReportReason.Review::class.java)
      "Parking" -> context.deserialize(data, ReportReason.Parking::class.java)
      else -> throw JsonParseException("Unknown type: $type")
    }
  }
}

/**
 * A Firestore-backed implementation of the ReportedObjectRepository. Manages CRUD operations for
 * reported objects in a Firestore database.
 *
 * @property db The Firestore instance used for database operations.
 */
class ReportedObjectRepositoryFirestore @Inject constructor(private val db: FirebaseFirestore) :
    ReportedObjectRepository {

  private val collectionPath = "reported_objects"
  private val gson: Gson =
      GsonBuilder().registerTypeAdapter(ReportReason::class.java, ReportReasonAdapter()).create()

  /**
   * Generates a new unique identifier for a reported object.
   *
   * @return A string representing a unique identifier.
   */
  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  /**
   * Adds a new reported object to the Firestore collection.
   *
   * @param reportedObject The reported object to add.
   * @param onSuccess A callback invoked when the operation is successful.
   * @param onFailure A callback invoked when the operation fails with an exception.
   */
  override fun addReportedObject(
      reportedObject: ReportedObject,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val reportData = gson.toJson(reportedObject) // Serialize to JSON
    val reportMap: Map<String, Any> =
        gson.fromJson(reportData, object : TypeToken<Map<String, Any>>() {}.type)

    db.collection(collectionPath)
        .document(reportedObject.reportUID)
        .set(reportMap)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  /**
   * Retrieves all reported objects of a specific type (e.g., PARKING or REVIEW).
   *
   * @param type The type of the reported objects to fetch (e.g., [ReportedObjectType.PARKING]).
   * @param onSuccess A callback invoked with a list of reported objects when successful.
   * @param onFailure A callback invoked with an exception when the operation fails.
   */
  override fun getReportedObjectsByType(
      type: ReportedObjectType,
      onSuccess: (List<ReportedObject>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("objectType", type.name)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val reportedObjects =
              querySnapshot.documents.mapNotNull { document ->
                gson.fromJson(document.data?.let { gson.toJson(it) }, ReportedObject::class.java)
              }
          onSuccess(reportedObjects)
        }
        .addOnFailureListener { onFailure(it) }
  }

  /**
   * Retrieves all reported objects associated with a specific object UID.
   *
   * @param objectUID The unique identifier of the object being reported.
   * @param onSuccess A callback invoked with a list of reported objects when successful.
   * @param onFailure A callback invoked with an exception when the operation fails.
   */
  override fun getReportedObjectsByObjectUID(
      objectUID: String,
      onSuccess: (List<ReportedObject>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("objectUID", objectUID)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val reportedObjects =
              querySnapshot.documents.mapNotNull { document ->
                gson.fromJson(document.data?.let { gson.toJson(it) }, ReportedObject::class.java)
              }
          onSuccess(reportedObjects)
        }
        .addOnFailureListener { onFailure(it) }
  }

  /**
   * Retrieves all reported objects submitted by a specific user.
   *
   * @param userUID The unique identifier of the user who submitted the reports.
   * @param onSuccess A callback invoked with a list of reported objects when successful.
   * @param onFailure A callback invoked with an exception when the operation fails.
   */
  override fun getReportedObjectsByUser(
      userUID: String,
      onSuccess: (List<ReportedObject>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("userUID", userUID)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val reportedObjects =
              querySnapshot.documents.mapNotNull { document ->
                gson.fromJson(document.data?.let { gson.toJson(it) }, ReportedObject::class.java)
              }
          onSuccess(reportedObjects)
        }
        .addOnFailureListener { onFailure(it) }
  }

  /**
   * Deletes a reported object by its unique report identifier.
   *
   * @param reportUID The unique identifier of the report to delete.
   * @param onSuccess A callback invoked when the operation is successful.
   * @param onFailure A callback invoked with an exception when the operation fails.
   */
  override fun deleteReportedObject(
      reportUID: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(reportUID)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
  }

  /**
   * Serializes a [ReportedObject] into a JSON string.
   *
   * @param reportedObject The reported object to serialize.
   * @return A JSON string representation of the reported object.
   */
  fun serializeReportedObject(reportedObject: ReportedObject): String {
    return gson.toJson(reportedObject)
  }

  /**
   * Deserializes a JSON string into a [ReportedObject].
   *
   * @param data A JSON string representation of a reported object.
   * @return A [ReportedObject] instance.
   */
  fun deserializeReportedObject(data: Map<String, Any>): ReportedObject {
    val json = gson.toJson(data)
    return gson.fromJson(json, ReportedObject::class.java)
  }
}
