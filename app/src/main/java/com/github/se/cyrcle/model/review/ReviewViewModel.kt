package com.github.se.cyrcle.model.review

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ReviewViewModel(private val reviewRepository: ReviewRepository) : ViewModel() {

  /** Selected parking to review/edit */
  private val _selectedReview = MutableStateFlow<Review?>(null)
  val selectedReview: StateFlow<Review?> = _selectedReview

  private val _parkingReviews = MutableStateFlow<List<Review>>(emptyList())
  val parkingReviews: StateFlow<List<Review>> = _parkingReviews

  private val _userReviews = MutableStateFlow<List<Review?>>(emptyList())
  val userReviews: StateFlow<List<Review?>> = _userReviews

  /** Selected parking to review/edit */
  private val _selectedReviewReports = MutableStateFlow<List<ReviewReport>?>(null)
  val selectedReviewReports: StateFlow<List<ReviewReport>?> = _selectedReviewReports

  fun addReview(review: Review) {
    reviewRepository.addReview(review, {}, { Log.e("ReviewViewModel", "Error adding review", it) })
  }

  fun selectReview(review: Review) {
    _selectedReview.value = review
  }

  fun getNewUid(): String {
    return reviewRepository.getNewUid()
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

  fun deleteReviewById(review: Review) {
    reviewRepository.deleteReviewById(
        review.uid, {}, { Log.e("ReviewViewModel", "Error deleting reviews", it) })
  }

  fun addReport(report: ReviewReport) {
    val selectedParking = _selectedReview.value
    if (selectedParking == null) {
      Log.e("ParkingViewModel", "No parking selected")
      return
    }

    reviewRepository.addReport(
        report,
        onSuccess = {
          // Add the new report to the current list of reports
          _selectedReviewReports.update { currentReports -> currentReports?.plus(it) ?: listOf(it) }
          Log.d("ParkingViewModel", "Report added successfully")
        },
        onFailure = { exception ->
          Log.e("ParkingViewModel", "Error adding report: ${exception.message}", exception)
        })
  }

  /**
   * Reset the selectedReview This is to prevent old review from being shown while the new spots
   * review are being fetched
   */
  fun clearReviews() {
    _parkingReviews.value = emptyList()
  }

  // create factory (imported from bootcamp)
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReviewViewModel(ReviewRepositoryFirestore(FirebaseFirestore.getInstance())) as T
          }
        }
  }
}
