package com.github.se.cyrcle.model.review

import android.util.Log
import androidx.lifecycle.ViewModel
import com.github.se.cyrcle.model.report.ReportedObject
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.report.ReportedObjectType
import com.github.se.cyrcle.model.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

const val NB_REPORTS_THRESH = 10
const val NB_REPORTS_MAXSEVERITY_THRESH = 3
const val MAX_SEVERITY = 3

class ReviewViewModel(
    private val reviewRepository: ReviewRepository,
    private val reportedObjectRepository: ReportedObjectRepository
) : ViewModel() {

  /** Selected parking to review/edit */
  private val _selectedReview = MutableStateFlow<Review?>(null)
  val selectedReview: StateFlow<Review?> = _selectedReview

  private val _parkingReviews = MutableStateFlow<List<Review>>(emptyList())
  val parkingReviews: StateFlow<List<Review>> = _parkingReviews

  private val _userReviews = MutableStateFlow<List<Review?>>(emptyList())
  val userReviews: StateFlow<List<Review?>> = _userReviews

  /** Selected parking to review/edit */
  private val _selectedReviewReports = MutableStateFlow<List<ReviewReport>>(emptyList())
  val selectedReviewReports: StateFlow<List<ReviewReport>> = _selectedReviewReports

  fun addReview(review: Review) {
    reviewRepository.addReview(review, {}, { Log.e("ReviewViewModel", "Error adding review", it) })
  }

  fun selectReview(review: Review) {
    _selectedReview.value = review
    loadReportsForSelectedReview()
  }

  fun getReviewById(id: String, onSuccess: (Review) -> Unit, onFailure: (Exception) -> Unit) {
    reviewRepository.getReviewById(id, onSuccess, onFailure)
  }

  fun getNewUid(): String {
    return reviewRepository.getNewUid()
  }

  fun clearSelectedReview() {
    _selectedReview.value = null
    _selectedReviewReports.value = emptyList()
  }

  private fun loadReportsForSelectedReview() {
    reviewRepository.getReportsForReview(
        reviewId = selectedReview.value?.uid!!,
        onSuccess = { reports ->
          _selectedReviewReports.value = reports // Update the state with fetched reports
          Log.d("ReviewViewModel", "Reports loaded successfully: ${reports.size}")
        },
        onFailure = { exception ->
          Log.e("ReviewViewModel", "Error loading reports: ${exception.message}")
        })
  }

  fun updateReview(review: Review) {
    reviewRepository.updateReview(
        review, {}, { Log.e("ReviewViewModel", "Error adding review", it) })
  }

  fun getReviewsByParking(parking: String) {
    reviewRepository.getReviewsByParkingId(
        parking,
        { reviews -> _parkingReviews.value = reviews },
        { Log.e("ReviewViewModel", "Error getting reviews", it) })
  }

  fun deleteReviewById(uid: String) {
    reviewRepository.deleteReviewById(
        uid, {}, { Log.e("ReviewViewModel", "Error deleting reviews", it) })
  }

  /**
   * Adds a report for the currently selected review and updates the repository.
   *
   * This function first verifies that a review is selected. If no review is selected, it logs an
   * error and returns. It then attempts to add the report to the review repository. Upon successful
   * addition, the report is evaluated against severity and threshold limits to determine if a
   * `ReportedObject` should be created and added to the reported objects repository.
   *
   * Updates the selected review's report count and severity metrics and ensures these changes are
   * reflected in the repository.
   *
   * @param report The report to be added, which includes details such as the reason and user ID.
   * @param user The user submitting the report, required for identifying the reporter.
   */
  fun addReport(report: ReviewReport, user: User) {
    val selectedReview = _selectedReview.value
    if (selectedReview == null) {
      Log.e("ReviewViewModel", "No review selected")
      return
    }

    reviewRepository.addReport(
        report,
        onSuccess = {
          val newReportedObject =
              ReportedObject(
                  objectUID = selectedReview.uid,
                  reportUID = report.uid,
                  nbOfTimesReported = selectedReview.nbReports + 1,
                  nbOfTimesMaxSeverityReported =
                      if (report.reason.severity == MAX_SEVERITY)
                          selectedReview.nbMaxSeverityReports + 1
                      else selectedReview.nbMaxSeverityReports,
                  userUID = user.public.userId,
                  objectType = ReportedObjectType.REVIEW,
              )

          Log.d(
              "ReviewViewModel",
              "${selectedReview.nbReports}, ${selectedReview.nbMaxSeverityReports}")

          reportedObjectRepository.getObjectUID(
              objectUID = selectedReview.uid,
              reportedObject = newReportedObject,
              shouldAddIfNotExist =
                  ((report.reason.severity == MAX_SEVERITY &&
                      selectedReview.nbMaxSeverityReports >= NB_REPORTS_MAXSEVERITY_THRESH) ||
                      (selectedReview.nbReports >= NB_REPORTS_THRESH)),
              onSuccess = { updateLocalReviewAndMetrics(report, selectedReview) },
              onFailure = { Log.e("ReviewViewModel", "Error adding to ReportedObjects") })
        },
        onFailure = {
          Log.e("ReviewViewModel", "Report not added")
          updateLocalReviewAndMetrics(report, selectedReview)
        })
  }
  /** Updates the local review and metrics after a report is added or updated. */
  private fun updateLocalReviewAndMetrics(report: ReviewReport, selectedReview: Review) {
    // Update the local reports and review metrics
    _selectedReviewReports.update { currentReports ->
      currentReports.plus(report) ?: listOf(report)
    }
    if (report.reason.severity == MAX_SEVERITY) {
      selectedReview.nbMaxSeverityReports += 1
    }
    selectedReview.nbReports += 1
    reviewRepository.updateReview(selectedReview, {}, {})
    Log.d("ReviewViewModel", "Review and metrics updated successfully: ${selectedReview.nbReports}")
  }

  /**
   * Reset the selectedReview This is to prevent old review from being shown while the new spots
   * review are being fetched
   */
  fun clearReviews() {
    _parkingReviews.value = emptyList()
  }
}
