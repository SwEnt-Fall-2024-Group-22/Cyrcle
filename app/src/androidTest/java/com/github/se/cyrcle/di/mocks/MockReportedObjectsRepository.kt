package com.github.se.cyrcle.di.mocks

import com.github.se.cyrcle.model.report.ReportedObject
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.report.ReportedObjectType
import javax.inject.Inject

class MockReportedObjectRepository @Inject constructor() : ReportedObjectRepository {

  private var uid = 0
  private val reportedObjects = mutableListOf<ReportedObject>()

  override fun getNewUid(): String {
    return (uid++).toString()
  }

  override fun getReportedObjectsByType(
      type: ReportedObjectType,
      onSuccess: (List<ReportedObject>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val filtered = reportedObjects.filter { it.objectType == type }
    if (filtered.isNotEmpty()) {
      onSuccess(filtered)
    } else {
      onFailure(Exception("No ReportedObjects found for type: $type"))
    }
  }

  override fun addReportedObject(
      reportedObject: ReportedObject,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    try {
      // Validate the reportedObject fields
      if (reportedObject.reportUID.isBlank()) {
        throw IllegalArgumentException("ReportUID cannot be blank.")
      }
      if (reportedObject.objectUID.isBlank()) {
        throw IllegalArgumentException("ObjectUID cannot be blank.")
      }
      if (reportedObject.userUID.isBlank()) {
        throw IllegalArgumentException("UserUID cannot be blank.")
      }

      // Check if the object already exists to avoid duplicates
      val existingObject = reportedObjects.find { it.reportUID == reportedObject.reportUID }
      if (existingObject != null) {
        onFailure(Exception("Error in adding ReportedObject"))
      }

      // Add the object to the list
      reportedObjects.add(reportedObject)
      onSuccess()
    } catch (e: Exception) {
      onFailure(e)
    }
  }

  override fun getReportedObjectsByUser(
      userUID: String,
      onSuccess: (List<ReportedObject>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val filtered = reportedObjects.filter { it.userUID == userUID }
    if (filtered.isNotEmpty()) {
      onSuccess(filtered)
    } else {
      onFailure(Exception("No ReportedObjects found for userUID: $userUID"))
    }
  }

  override fun deleteReportedObject(
      reportUID: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val removed = reportedObjects.removeIf { it.reportUID == reportUID }
    if (removed) {
      onSuccess()
    } else {
      onFailure(Exception("No ReportedObject found with reportUID: $reportUID"))
    }
  }

  override fun getAllReportedObjects(
      onSuccess: (List<ReportedObject>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (reportedObjects.isNotEmpty()) {
      onSuccess(reportedObjects)
    } else {
      onFailure(Exception("No ReportedObjects found"))
    }
  }

  override fun updateReportedObject(
      objectUID: String,
      updatedObject: ReportedObject,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val index = reportedObjects.indexOfFirst { it.objectUID == objectUID }
    if (index != -1) {
      reportedObjects[index] = updatedObject
      onSuccess()
    } else {
      onFailure(Exception("No ReportedObject found with objectUID: $objectUID"))
    }
  }

  override fun checkIfObjectExists(
      objectUID: String,
      onSuccess: (documentId: String?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val existingObject = reportedObjects.find { it.objectUID == objectUID }
    if (existingObject != null) {
      onSuccess(existingObject.reportUID)
    } else {
      onSuccess(null)
    }
  }
}
