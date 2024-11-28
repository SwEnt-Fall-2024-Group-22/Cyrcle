package com.github.se.cyrcle.model.review

import com.github.se.cyrcle.ui.theme.molecules.DropDownableEnum
import com.google.firebase.Timestamp

data class Review(
    val uid: String,
    val owner: String,
    val text: String,
    val rating: Double,
    val parking: String,
    val time: Timestamp = Timestamp.now(),
    var nbReports: Int = 0,
    var nbMaxSeverityReports: Int = 0
)

enum class ReviewReportReason(override val description: String, val severity: Int) :
    DropDownableEnum {
  IRRELEVANT("This Review is Irrelevant", 1),
  DUPLICATE("This Review is Spam/ Abusive Duplicate of another User's", 2),
  DEFAMATION("This Review is Defamation", 2),
  MISLEADING("This Review is Misleading", 3),
  HARMFUL("This Review is Harmful", 3),
  OTHER("OTHER", 1)
}

data class ReviewReport(
    val uid: String = "",
    val reason: ReviewReportReason = ReviewReportReason.OTHER,
    val userId: String = "",
    val review: String = "",
    val description: String = ""
)
