package com.github.se.cyrcle.model.report

import com.github.se.cyrcle.model.parking.ParkingReportReason

/** Test instance for a ReportedObject */
object TestInstancesReportedObject {
  val reportedObject1 =
      ReportedObject(
          objectUID = "object1",
          reportUID = "report1",
          reason = ReportReason.Parking(ParkingReportReason.SAFETY_CONCERN),
          userUID = "user1",
          objectType = ReportedObjectType.PARKING)
}
