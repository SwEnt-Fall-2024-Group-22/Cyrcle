package com.github.se.cyrcle.model.report

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

/**
 * A Firestore-backed implementation of the ReportedObjectRepository. Manages CRUD operations for
 * reported objects in a Firestore database.
 *
 * @property db The Firestore instance used for database operations.
 */
class ReportedObjectRepositoryFirestore @Inject constructor(private val db: FirebaseFirestore) :
    ReportedObjectRepository {

  private val collectionPath = "reported_objects"
  private val gson: Gson = GsonBuilder().create()

  /**
   * Generates a new unique identifier for a reported object.
   *
   * @return A string representing a unique identifier.
   */
  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  /**
   * Updates a reported object with a certain objectUID in the Firestore collection.
   *
   * @param objectUID The unique identifier of the object to update.
   * @param updatedObject The updated [ReportedObject] instance.
   * @param onSuccess A callback invoked when the operation is successful.
   * @param onFailure A callback invoked when the operation fails with an exception.
   */
  override fun updateReportedObject(
      objectUID: String,
      updatedObject: ReportedObject,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("objectUID", objectUID) // Query for the document with the matching objectUID
        .get()
        .addOnSuccessListener { querySnapshot ->
          if (querySnapshot.documents.isNotEmpty()) {
            val documentId = querySnapshot.documents.first().id
            db.collection(collectionPath)
                .document(documentId)
                .set(
                    updatedObject,
                    com.google.firebase.firestore.SetOptions
                        .merge()) // Use merge to update only provided fields
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { exception -> onFailure(exception) }
          } else {
            // No document found for the given objectUID
            onFailure(Exception("No document found with objectUID: $objectUID"))
          }
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  /**
   * Adds a new reported object to the Firestore collection.
   *
   * @param reportedObject The reported object to add.
   * @param onSuccess A callback invoked when the operation is successful.
   * @param onFailure A callback invoked when the operation fails with an exception.
   */
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
    try {
      // Serialize the ReportedObject using the updated method
      val reportData = serializeReportedObject(reportedObject)

      // Deserialize the JSON string into a Map for Firestore
      val reportMap: Map<String, Any> =
          gson.fromJson(reportData, object : TypeToken<Map<String, Any>>() {}.type)

      // Save the Map to Firestore
      db.collection(collectionPath)
          .document(reportedObject.reportUID)
          .set(reportMap)
          .addOnSuccessListener { onSuccess() }
          .addOnFailureListener { exception -> onFailure(exception) }
    } catch (exception: Exception) {
      onFailure(exception)
    }
  }

  /**
   * Retrieves all reported objects from the Firestore collection.
   *
   * @param onSuccess A callback invoked with a list of all reported objects when successful.
   * @param onFailure A callback invoked with an exception when the operation fails.
   */
  override fun getAllReportedObjects(
      onSuccess: (List<ReportedObject>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val reportedObjects =
              querySnapshot.documents.mapNotNull { document ->
                document.data?.let { deserializeReportedObject(it) }
              }
          onSuccess(reportedObjects)
        }
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
                document.data?.let { deserializeReportedObject(it) }
              }
          onSuccess(reportedObjects)
        }
        .addOnFailureListener { onFailure(it) }
  }

  /**
   * Adds or updates a reported object in the Firestore collection based on `objectUID`.
   * - If the object already exists, it merges the update.
   * - If the object does not exist, it creates a new one.
   *
   * @param objectUID The unique identifier of the object being reported.
   * @param reportedObject The reported object to save.
   * @param onSuccess A callback invoked on successful addition or update.
   * @param onFailure A callback invoked when the operation fails.
   */
  override fun getObjectUID(
      objectUID: String,
      reportedObject: ReportedObject,
      shouldAddIfNotExist: Boolean, // New parameter
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("objectUID", objectUID)
        .get()
        .addOnSuccessListener { querySnapshot ->
          if (querySnapshot.documents.isNotEmpty()) {
            // Document exists: update it
            val documentId = querySnapshot.documents.first().id
            db.collection(collectionPath)
                .document(documentId)
                .set(reportedObject, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }
          } else {
            // Document does not exist
            if (shouldAddIfNotExist) {
              // Add the document if the condition allows it
              db.collection(collectionPath)
                  .document(objectUID)
                  .set(reportedObject)
                  .addOnSuccessListener { onSuccess() }
                  .addOnFailureListener { onFailure(it) }
            } else {
              // Fail if the condition does not allow adding a new document
              onFailure(Exception("Document does not exist, and addition is not allowed."))
            }
          }
        }
        .addOnFailureListener { exception ->
          // Handle query failure
          onFailure(exception)
        }
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
                document.data?.let { deserializeReportedObject(it) }
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
        .whereEqualTo("objectUID", reportUID) // Query documents where objectUID matches reportUID
        .limit(1) // Fetch only the first matching document
        .get()
        .addOnSuccessListener { querySnapshot ->
          if (!querySnapshot.isEmpty) {
            val documentId = querySnapshot.documents[0].id // Get the document ID
            db.collection(collectionPath)
                .document(documentId) // Reference the document
                .delete() // Delete the document
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }
          } else {
            onFailure(Exception("No document found with objectUID = $reportUID"))
          }
        }
        .addOnFailureListener { exception ->
          onFailure(exception) // Handle query failure
        }
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
