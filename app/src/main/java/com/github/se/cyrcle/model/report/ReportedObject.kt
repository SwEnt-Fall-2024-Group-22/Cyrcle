package com.github.se.cyrcle.model.report

import com.github.se.cyrcle.model.parking.ParkingReportReason
import com.github.se.cyrcle.model.review.ReviewReportReason

sealed class ReportReason {
  data class Parking(val reason: ParkingReportReason) : ReportReason()

  data class Review(val reason: ReviewReportReason) : ReportReason()
}

enum class ReportedObjectType {
  PARKING,
  REVIEW
}

data class ReportedObject(
    val objectUID: String,
    val reportUID: String,
    val reason: ReportReason,
    val userUID: String,
    val objectType: ReportedObjectType
)
