package com.github.se.cyrcle.model.report

/** Test instance for a ReportedObject */
object TestInstancesReportedObject {
  val reportedObject1 =
      ReportedObject(
          objectUID = "object1",
          reportUID = "report1",
          nbOfTimesReported = 0,
          nbOfTimesMaxSeverityReported = 0,
          userUID = "user1",
          objectType = ReportedObjectType.PARKING)
}
