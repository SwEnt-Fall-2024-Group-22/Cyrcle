package com.github.se.cyrcle.di.mocks

import com.github.se.cyrcle.model.review.Review
import com.github.se.cyrcle.model.review.ReviewRepository
import javax.inject.Inject

class MockReviewRepository @Inject constructor() : ReviewRepository {
  private var uid = 0
  private val reviews = mutableListOf<Review>()

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
}
