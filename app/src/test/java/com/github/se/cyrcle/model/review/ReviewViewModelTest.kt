package com.github.se.cyrcle.model.review

import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.user.TestInstancesUser
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReviewViewModelTest {

  @Mock private lateinit var reviewRepository: ReviewRepository
  @Mock private lateinit var reportedObjectRepository: ReportedObjectRepository
  private lateinit var reviewViewModel: ReviewViewModel

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    reviewViewModel = ReviewViewModel(reviewRepository, reportedObjectRepository)
  }

  @Test
  fun addReportTest() {

    val review = TestInstancesReview.review1.copy(nbReports = 0, nbMaxSeverityReports = 0)
    val user = TestInstancesUser.user1
    val report =
        ReviewReport(
            uid = "report1",
            reason = ReviewReportReason.HARMFUL,
            userId = user.public.userId,
            review = review.uid,
            "")

    // Mock repository behavior for report addition
    `when`(reviewRepository.addReport(eq(report), any(), any())).then {
      it.getArgument<(ReviewReport) -> Unit>(1)(report)
    }

    reviewViewModel.selectReview(review)
    reviewViewModel.addReport(report, user)

    // Verify that the report was added to the parking repository
    verify(reviewRepository).addReport(eq(report), any(), any())
    // Verify the parking repository update method is called to persist changes
  }

  @Test
  fun reviewInteractionTest() {
    val review = TestInstancesReview.review1
    val user = TestInstancesUser.user1

    // Step 1: Press on like the review. The review was not liked by the user before, so we expect
    // the review to be updated in the repository with the user's ID added to the likedBy list
    reviewViewModel.handleInteraction(review, user.public.userId, true)
    val updatedReview = review.copy(likedBy = listOf(user.public.userId))
    verify(reviewRepository).updateReview(eq(updatedReview), any(), any())

    clearInvocations(reviewRepository) // Clear the invocations to prepare for the next step

    // Step 2: Press on dislike the review. The review was liked by the user before, so we expect
    // the review to be updated in the repository with the user's ID removed from the likedBy list
    // and added to the dislikedBy list
    reviewViewModel.handleInteraction(updatedReview, user.public.userId, false)
    val updatedReview2 =
        updatedReview.copy(likedBy = emptyList(), dislikedBy = listOf(user.public.userId))
    verify(reviewRepository).updateReview(eq(updatedReview2), any(), any())

    clearInvocations(reviewRepository)

    // Step 3: Press on dislike the review. The review was disliked by the user before, so we expect
    // the review to be updated in the repository with the user's ID removed from the dislikedBy
    // list
    reviewViewModel.handleInteraction(updatedReview2, user.public.userId, false)
    val updatedReview3 = updatedReview2.copy(dislikedBy = emptyList())
    verify(reviewRepository).updateReview(eq(updatedReview3), any(), any())

    clearInvocations(reviewRepository)

    // Step 4: Press on like the review. The review was not liked by the user before, so we expect
    // the review to be updated in the repository with the user's ID added to the likedBy list
    reviewViewModel.handleInteraction(updatedReview3, user.public.userId, true)
    val updatedReview4 = updatedReview3.copy(likedBy = listOf(user.public.userId))
    verify(reviewRepository).updateReview(eq(updatedReview4), any(), any())

    clearInvocations(reviewRepository)

    // Step 5: Press on like the review. The review was liked by the user before, so we expect
    // the review to be updated in the repository with the user's ID removed from the likedBy list
    reviewViewModel.handleInteraction(updatedReview4, user.public.userId, true)
    val updatedReview5 = updatedReview4.copy(likedBy = emptyList())
    verify(reviewRepository).updateReview(eq(updatedReview5), any(), any())
  }
}
