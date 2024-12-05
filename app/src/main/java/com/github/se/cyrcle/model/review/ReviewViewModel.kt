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

  /** Selected parking has already been reported by current user */
  private val _hasAlreadyReported = MutableStateFlow<Boolean>(false)
  val hasAlreadyReported: StateFlow<Boolean> = _hasAlreadyReported

  /** Selected parking to review/edit */
  private val _selectedReviewReports = MutableStateFlow<List<ReviewReport>>(emptyList())
  val selectedReviewReports: StateFlow<List<ReviewReport>> = _selectedReviewReports

  fun addReview(review: Review) {
    reviewRepository.addReview(review, {}, { Log.e("ReviewViewModel", "Error adding review", it) })
  }

  fun selectReviewAdminScreen(review: Review) {
    _selectedReview.value = review
    loadReportsForSelectedReview()
  }

  fun selectReview(review: Review) {
    _selectedReview.value = review
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
        review,
        {
          _parkingReviews.value =
              _parkingReviews.value.map { if (it.uid == review.uid) review else it }
        },
        { Log.e("ReviewViewModel", "Error adding review", it) })
  }

  fun getReviewsByParking(parking: String) {
    reviewRepository.getReviewsByParkingId(
        parking,
        { reviews -> _parkingReviews.value = reviews },
        { Log.e("ReviewViewModel", "Error getting reviews", it) })
  }

    fun getReviewsByOwnerId(ownerId: String) {
        reviewRepository.getReviewsByOwnerId(
            ownerId,
            { reviews -> _userReviews.value = reviews },
            { Log.e("ReviewViewModel", "Error getting reviews for owner", it) }
        )
    }

  fun deleteReviewById(uid: String) {
    reviewRepository.deleteReviewById(
        uid, {}, { Log.e("ReviewViewModel", "Error deleting reviews", it) })
  }

  /**
   * Adds a like to a given review by a given user and updates the repository. This function also
   * removes the user from the dislikedBy list if they are present. This ensures that a user cannot
   * like and dislike a review at the same time.
   *
   * @param review The review to add a like to.
   * @param userId The user ID of the user adding the like.
   */
  private fun addLikeToReview(review: Review, userId: String) {
    val newLikedBy = review.likedBy.plus(userId)
    val newDislikedBy = review.dislikedBy.filter { it != userId }
    /* Note: The review is updated with the new lists of likedBy and dislikedBy rather than
    doing two separate updates for each list. This is to ensure that the review is not
    properly updated if one of the updates fails.
    */
    updateReview(review.copy(likedBy = newLikedBy, dislikedBy = newDislikedBy))
  }

  /**
   * Removes a like from a given review by a given user and updates the repository.
   *
   * @param review The review to remove a like from.
   * @param userId The user ID of the user removing the like.
   */
  private fun removeLikeFromReview(review: Review, userId: String) {
    val newLikedBy = review.likedBy.filter { it != userId }
    updateReview(review.copy(likedBy = newLikedBy))
  }

  /**
   * Adds a dislike to a given review by a given user and updates the repository. This function also
   * removes the user from the likedBy list if they are present. This ensures that a user cannot
   * like and dislike a review at the same time.
   *
   * @param review The review to add a dislike to.
   * @param userId The user ID of the user adding the dislike.
   */
  private fun addDislikeToReview(review: Review, userId: String) {
    val newDislikedBy = review.dislikedBy.plus(userId)
    val newLikedBy = review.likedBy.filter { it != userId }
    /* Note: The review is updated with the new lists of likedBy and dislikedBy rather than
    doing two separate updates for each list. This is to ensure that the review is not
    properly updated if one of the updates fails.
    */
    updateReview(review.copy(likedBy = newLikedBy, dislikedBy = newDislikedBy))
  }

  /**
   * Removes a dislike from a given review by a given user and updates the repository.
   *
   * @param review The review to remove a dislike from.
   * @param userId The user ID of the user removing the dislike.
   */
  private fun removeDislikeFromReview(review: Review, userId: String) {
    val newDislikedBy = review.dislikedBy.filter { it != userId }
    updateReview(review.copy(dislikedBy = newDislikedBy))
  }

  /**
   * Handles a user interaction with a review, either adding or removing a like or dislike.
   *
   * If the user presses the like button, the function checks if the user has already liked the
   * review. If they have, the like is removed. If they have not, the like is added. The same logic
   * applies to dislikes.
   *
   * @param review The review to interact with.
   * @param userId The user ID of the user interacting with the review.
   * @param isLike Whether the interaction is a like or dislike. (true for like, false for dislike)
   */
  fun handleInteraction(
      review: Review,
      userId: String,
      isLike: Boolean,
  ) {
    if (isLike) {
      if (review.likedBy.contains(userId)) {
        removeLikeFromReview(review, userId)
      } else {
        addLikeToReview(review, userId)
      }
    } else {
      if (review.dislikedBy.contains(userId)) {
        removeDislikeFromReview(review, userId)
      } else {
        addDislikeToReview(review, userId)
      }
    }
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

    Log.d(
        "ReviewViewModel",
        "Reports: ${selectedReview.nbReports}, MaxSeverityReports: ${selectedReview.nbMaxSeverityReports}")

    if (selectedReview.reportingUsers.contains(user.public.userId)) {
      _hasAlreadyReported.value = true
      return
    } else {
      _hasAlreadyReported.value = false
    }

    reviewRepository.addReport(
        report,
        onSuccess = {
          addReportingUser(user)
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

          reportedObjectRepository.checkIfObjectExists(
              objectUID = selectedReview.uid,
              onSuccess = { documentId ->
                if (documentId != null) {
                  reportedObjectRepository.updateReportedObject(
                      documentId = documentId,
                      updatedObject = newReportedObject,
                      onSuccess = { updateLocalReviewAndMetrics(report) },
                      onFailure = { Log.d("ReviewViewModel", "Error updating ReportedObject") })
                } else {
                  val shouldAdd =
                      (report.reason.severity == MAX_SEVERITY &&
                          selectedReview.nbMaxSeverityReports >= NB_REPORTS_MAXSEVERITY_THRESH) ||
                          (selectedReview.nbReports >= NB_REPORTS_THRESH)

                  if (shouldAdd) {
                    reportedObjectRepository.addReportedObject(
                        reportedObject = newReportedObject,
                        onSuccess = { updateLocalReviewAndMetrics(report) },
                        onFailure = { Log.d("ReviewViewModel", "Error adding ReportedObject") })
                  } else {
                    updateLocalReviewAndMetrics(report)
                  }
                }
              },
              onFailure = {
                Log.d("ReviewViewModel", "Error checking for ReportedObject")
                updateLocalReviewAndMetrics(report)
              })
        },
        onFailure = {
          Log.d("ReviewViewModel", "Report not added")
          updateLocalReviewAndMetrics(report)
        })
  }

  /** Updates the local review and metrics after a report is added or updated. */
  private fun updateLocalReviewAndMetrics(report: ReviewReport) {
    val selectedReview = _selectedReview.value ?: return
    _selectedReviewReports.update { currentReports -> currentReports.plus(report) }
    if (report.reason.severity == MAX_SEVERITY) {
      selectedReview.nbMaxSeverityReports += 1
    }
    selectedReview.nbReports += 1
    reviewRepository.updateReview(selectedReview, {}, {})
    Log.d("ReviewViewModel", "Review and metrics updated successfully: ${selectedReview.nbReports}")
  }

  /**
   * Adds a reporting user to the currently selected review and updates Firestore.
   *
   * This function first verifies if a review is selected. If a review is selected, it creates an
   * updated review object with the new reporting user and attempts to update Firestore to reflect
   * the change. Upon a successful update, the local state is also updated to ensure consistency. If
   * no review is selected, an error is logged and no operation is performed.
   *
   * @param user The user to be added to the reporting users list.
   */
  fun addReportingUser(user: User) {
    _selectedReview.update { currentReview ->
      currentReview?.let {
        val updatedReview = it.copy(reportingUsers = it.reportingUsers + user.public.userId)

        reviewRepository.updateReview(
            review = updatedReview,
            onSuccess = {
              Log.d("ReviewViewModel", "User added to reportingUsers in Firestore")
              _selectedReview.update { curReview ->
                if (curReview?.uid == updatedReview.uid) {
                  updatedReview
                } else {
                  curReview
                }
              }
            },
            onFailure = { exception ->
              Log.e("ReviewViewModel", "Failed to update review in Firestore: ${exception.message}")
            })

        updatedReview
      }
    }
  }

  /**
   * Reset the selectedReview This is to prevent old review from being shown while the new spots
   * review are being fetched
   */
  fun clearReviews() {
    _parkingReviews.value = emptyList()
  }
}
