package com.github.se.cyrcle.model.review

interface ReviewRepository {
  /**
   * Get a new unique identifier for a review
   *
   * @return a new unique identifier for a review
   */
  fun getNewUid(): String

  /**
   * Initialize the repository
   *
   * @param onSuccess a callback that is called when the repository is initialized
   */
  fun onSignIn(onSuccess: () -> Unit)

  /**
   * Get a review by its identifier
   *
   * @param id the identifier of the review
   * @param onSuccess a callback that is called when the review is retrieved
   * @param onFailure a callback that is called when an error occurs
   */
  fun getReviewById(id: String, onSuccess: (Review) -> Unit, onFailure: (Exception) -> Unit)

  fun getReviewsByParkingId(
      parkingId: String,
      onSuccess: (List<Review>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Get reviews for a specific owner (e.g., user or parking location)
   *
   * @param ownerId the identifier of the owner (could be a user or parking location)
   * @param onSuccess a callback that is called when the reviews are retrieved
   * @param onFailure a callback that is called when an error occurs
   */
  fun getReviewsByOwnerId(
      ownerId: String,
      onSuccess: (List<Review>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Add a review
   *
   * @param review the review to add
   * @param onSuccess a callback that is called when the review is added
   * @param onFailure a callback that is called when an error occurs
   */
  fun addReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Update a review
   *
   * @param review the review to update
   * @param onSuccess a callback that is called when the review is updated
   * @param onFailure a callback that is called when an error occurs
   */
  fun updateReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Delete a review by its identifier
   *
   * @param id the identifier of the review to delete
   * @param onSuccess a callback that is called when the review is deleted
   * @param onFailure a callback that is called when an error occurs
   */
  fun deleteReviewById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun addReport(
      report: ReviewReport,
      onSuccess: (ReviewReport) -> Unit,
      onFailure: (Exception) -> Unit
  )
}
