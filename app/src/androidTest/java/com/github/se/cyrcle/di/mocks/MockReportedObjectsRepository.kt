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

  override fun getReportedObjectsByObjectUID(
      objectUID: String,
      onSuccess: (List<ReportedObject>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val filtered = reportedObjects.filter { it.objectUID == objectUID }
    if (filtered.isNotEmpty()) {
      onSuccess(filtered)
    } else {
      onFailure(Exception("No ReportedObjects found for objectUID: $objectUID"))
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
}
