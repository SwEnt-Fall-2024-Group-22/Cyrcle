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

  override fun getAllReviews(onSuccess: (List<Review>) -> Unit, onFailure: (Exception) -> Unit) {
    if (reviews.none { it.uid == "" }) onSuccess(reviews)
    else onFailure(Exception("Failed to get all reviews"))
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

  override fun getReviewByParking(
      id: String,
      onSuccess: (List<Review>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (id == "" || reviews.none { it.parking == id })
        onFailure(Exception("Failed to get review by parking id"))
    else onSuccess(reviews.filter { it.parking == id })
  }

  override fun getReviewsByOwner(
      owner: String,
      onSuccess: (List<Review>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (owner == "" || reviews.none { it.owner == owner })
        onFailure(Exception("Failed to get review by parking id"))
    else onSuccess(reviews.filter { it.owner == owner })
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
