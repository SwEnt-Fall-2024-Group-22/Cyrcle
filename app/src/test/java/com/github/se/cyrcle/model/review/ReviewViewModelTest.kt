package com.github.se.cyrcle.model.review

import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.user.TestInstancesUser
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReviewViewModelTest {

  @Mock private lateinit var reviewRepository: ReviewRepository
  @Mock private lateinit var reportedObjectRepository: ReportedObjectRepository
  @Mock private lateinit var reviewViewModel: ReviewViewModel

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
            review = review.uid)

    // Mock repository behavior for report addition
    `when`(reviewRepository.addReport(eq(report), any(), any())).then {
      it.getArgument<(ReviewReport) -> Unit>(1)(report)
    }

    reviewViewModel.selectReview(review)
    reviewViewModel.addReport(report, user)

    // Verify that the report was added to the parking repository
    verify(reviewRepository).addReport(eq(report), any(), any())

    assertEquals(1, review.nbReports) // Number of reports should increment
    assertEquals(1, review.nbMaxSeverityReports) // Max severity report counter should increment

    // Verify the parking repository update method is called to persist changes
    verify(reviewRepository).updateReview(eq(review), any(), any())
  }
}
