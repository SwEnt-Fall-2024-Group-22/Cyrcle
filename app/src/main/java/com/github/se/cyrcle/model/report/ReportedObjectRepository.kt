package com.github.se.cyrcle.model.report

interface ReportedObjectRepository {

  fun getNewUid(): String

  fun addReportedObject(
      reportedObject: ReportedObject,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getReportedObjectsByType(
      type: ReportedObjectType,
      onSuccess: (List<ReportedObject>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getReportedObjectsByUser(
      userUID: String,
      onSuccess: (List<ReportedObject>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun deleteReportedObject(reportUID: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
