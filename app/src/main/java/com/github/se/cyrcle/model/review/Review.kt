package com.github.se.cyrcle.model.review

import com.github.se.cyrcle.ui.theme.molecules.DropDownableEnum
import com.google.firebase.Timestamp

data class Review(
    val uid: String,
    val owner: String,
    val text: String,
    val rating: Double,
    val parking: String,
    val time: Timestamp = Timestamp.now()
)

enum class ReviewReportReason(override val description: String) : DropDownableEnum {
  IRRELEVANT("This Review is Irrelevant"),
  DUPLICATE("This Review is Spam/ Abusive Duplicate of another User's"),
  DEFAMATION("This Review is Defamation"),
  MISLEADING("This Review is Misleading"),
  HARMFUL("This Review is Harmful")
}

data class ReviewReport(
    val uid: String,
    val reason: ReviewReportReason,
    val userId: String,
    val review: String
)
