package com.github.se.cyrcle.di.mocks

import com.github.se.cyrcle.model.review.Review
import com.github.se.cyrcle.model.review.ReviewReport
import com.github.se.cyrcle.model.review.ReviewReportReason
import com.github.se.cyrcle.model.review.ReviewRepository
import com.github.se.cyrcle.model.review.TestInstancesReview
import javax.inject.Inject

class MockReviewRepository @Inject constructor() : ReviewRepository {
  private var uid = 0
  private val reviews = mutableListOf<Review>()
  private val reports =
      mutableListOf(
          ReviewReport(
              "1", ReviewReportReason.IRRELEVANT, "1", TestInstancesReview.review1.uid, ""),
          ReviewReport(
              "2", ReviewReportReason.DEFAMATION, "2", TestInstancesReview.review1.uid, ""),
          ReviewReport("3", ReviewReportReason.HARMFUL, "3", "nonexistent-review-id", ""))

  override fun getNewUid(): String {
    return (uid++).toString()
  }

  override fun onSignIn(onSuccess: () -> Unit) {
    onSuccess()
  }

  override fun getReviewById(
      id: String,
      onSuccess: (Review) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (id == "" || reviews.none { it.uid == id })
        onFailure(Exception("Failed to get review by id"))
    else onSuccess(reviews.find { it.uid == id }!!)
  }

  override fun getReviewsByParkingId(
      parkingId: String,
      onSuccess: (List<Review>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (parkingId == "" || reviews.none { it.parking == parkingId })
        onFailure(Exception("Failed to get review by parking id"))
    else onSuccess(reviews.filter { it.parking == parkingId })
  }

  override fun getReviewsByOwnerId(
      ownerId: String,
      onSuccess: (List<Review>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (ownerId == "" || reviews.none { it.owner == ownerId })
        onFailure(Exception("Failed to get review by parking id"))
    else onSuccess(reviews.filter { it.owner == ownerId })
  }

  override fun addReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    if (review.uid == "") onFailure(Exception("Failed to add a review"))
    else {
      reviews.add(review)
      onSuccess()
    }
  }

  override fun updateReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    if (review.uid == "") onFailure(Exception("Failed to update review")) else onSuccess()
  }

  override fun deleteReviewById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    if (id == "") onFailure(Exception("Failed to delete review")) else onSuccess()
  }

  override fun addReport(
      report: ReviewReport,
      onSuccess: (ReviewReport) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (report.uid == "") onFailure(Exception("Error adding report"))
    else {
      reports.add(report)
      onSuccess(report)
    }
  }

  override fun getReportsForReview(
      reviewId: String,
      onSuccess: (List<ReviewReport>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (reviewId == "") {
      onFailure(Exception("Review ID is empty"))
    } else {
      val filteredReports = reports.filter { it.uid == reviewId }
      if (filteredReports.isNotEmpty()) {
        onSuccess(filteredReports)
      } else {
        onFailure(Exception("No reports found for review ID: $reviewId"))
      }
    }
  }
}
