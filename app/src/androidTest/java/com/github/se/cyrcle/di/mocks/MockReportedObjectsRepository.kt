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

  override fun addReportedObject(
      reportedObject: ReportedObject,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (reportedObject.reportUID.isEmpty() || reportedObject.objectUID.isEmpty()) {
      onFailure(Exception("Invalid ReportedObject data"))
    } else {
      reportedObjects.add(reportedObject)
      onSuccess()
    }
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

  override fun getObjectUID(
      objectUID: String,
      reportedObject: ReportedObject,
      shouldAddIfNotExist: Boolean,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val existingObject = reportedObjects.find { it.objectUID == objectUID }
    if (existingObject != null) {
      // Object exists, so update it
      updateReportedObject(
          objectUID = objectUID,
          updatedObject = reportedObject,
          onSuccess = onSuccess,
          onFailure = onFailure)
    } else if (shouldAddIfNotExist) {
      // Object does not exist, so add it
      addReportedObject(
          reportedObject = reportedObject, onSuccess = onSuccess, onFailure = onFailure)
    } else {
      onFailure(Exception("Document does not exist, and addition is not allowed."))
    }
  }
}
