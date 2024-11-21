package com.github.se.cyrcle.model.report

import com.github.se.cyrcle.model.parking.ParkingReportReason

object TestInstancesReportedObject {
  val reportedObject1 =
      ReportedObject(
          objectUID = "object1",
          reportUID = "report1",
          reason = ReportReason.Parking(ParkingReportReason.SAFETY_CONCERN),
          userUID = "user1",
          objectType = ReportedObjectType.PARKING)
}
