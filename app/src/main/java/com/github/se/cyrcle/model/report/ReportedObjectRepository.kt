package com.github.se.cyrcle.model.report

/**
 * Interface for managing reported objects in the system. Defines the operations for adding,
 * retrieving, updating, and deleting reported objects.
 */
interface ReportedObjectRepository {

  /**
   * Generates a new unique identifier for a reported object.
   *
   * @return A string representing a unique identifier.
   */
  fun getNewUid(): String

  /**
   * Adds a new reported object to the repository.
   *
   * @param reportedObject The reported object to add.
   * @param onSuccess A callback invoked when the operation is successful.
   * @param onFailure A callback invoked when the operation fails with an exception.
   */
  fun addReportedObject(
      reportedObject: ReportedObject,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Checks if a reported object exists in the repository by its objectUID.
   *
   * @param objectUID The unique identifier of the object to check.
   * @param onSuccess A callback invoked with the document ID if the object exists, or null if it
   *   does not exist.
   * @param onFailure A callback invoked when the operation fails with an exception.
   */
  fun checkIfObjectExists(
      objectUID: String,
      onSuccess: (documentId: String?) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Updates a reported object with a given document ID in the Firestore collection.
   *
   * @param objectUID The unique identifier of the object to update.
   * @param updatedObject The updated [ReportedObject] instance.
   * @param onSuccess A callback invoked when the operation is successful.
   * @param onFailure A callback invoked when the operation fails with an exception.
   */
  fun updateReportedObject(
      objectUID: String,
      updatedObject: ReportedObject,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves all reported objects from the Firestore collection.
   *
   * @param onSuccess A callback invoked with a list of all reported objects when successful.
   * @param onFailure A callback invoked with an exception when the operation fails.
   */
  fun getAllReportedObjects(
      onSuccess: (List<ReportedObject>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves all reported objects of a specific type (e.g., PARKING or REVIEW).
   *
   * @param type The type of the reported objects to fetch (e.g., [ReportedObjectType.PARKING]).
   * @param onSuccess A callback invoked with a list of reported objects when successful.
   * @param onFailure A callback invoked with an exception when the operation fails.
   */
  fun getReportedObjectsByType(
      type: ReportedObjectType,
      onSuccess: (List<ReportedObject>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves all reported objects submitted by a specific user.
   *
   * @param userUID The unique identifier of the user who submitted the reports.
   * @param onSuccess A callback invoked with a list of reported objects when successful.
   * @param onFailure A callback invoked with an exception when the operation fails.
   */
  fun getReportedObjectsByUser(
      userUID: String,
      onSuccess: (List<ReportedObject>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Deletes a reported object by its unique report identifier.
   *
   * @param reportUID The unique identifier of the report to delete.
   * @param onSuccess A callback invoked when the operation is successful.
   * @param onFailure A callback invoked with an exception when the operation fails.
   */
  fun deleteReportedObject(reportUID: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
