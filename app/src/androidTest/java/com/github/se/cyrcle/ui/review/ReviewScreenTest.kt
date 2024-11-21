package com.github.se.cyrcle.ui.review

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockReportedObjectRepository
import com.github.se.cyrcle.di.mocks.MockReviewRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.image.ImageRepository
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.review.ReviewRepository
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.review.TestInstancesReview
import com.github.se.cyrcle.model.user.UserRepository
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.doNothing

@RunWith(AndroidJUnit4::class)
class ReviewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // Mock dependencies
  private lateinit var mockNavigationActions: NavigationActions

  private lateinit var mockParkingRepository: ParkingRepository
  private lateinit var mockImageRepository: ImageRepository
  private lateinit var mockReviewRepository: ReviewRepository
  private lateinit var mockUserRepository: UserRepository
  private lateinit var mockReportedObjectRepository: ReportedObjectRepository

  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var reviewViewModel: ReviewViewModel
  private lateinit var userViewModel: UserViewModel

  @Before
  fun setUp() {
    mockNavigationActions = mock(NavigationActions::class.java)

    mockImageRepository = MockImageRepository()
    mockUserRepository = MockUserRepository()
    mockParkingRepository = MockParkingRepository()
    mockReviewRepository = MockReviewRepository()
    mockReportedObjectRepository = MockReportedObjectRepository()

    userViewModel = UserViewModel(mockUserRepository, mockParkingRepository)
    parkingViewModel =
        ParkingViewModel(mockImageRepository, mockParkingRepository, mockReportedObjectRepository)
    reviewViewModel = ReviewViewModel(mockReviewRepository, mockReportedObjectRepository)

    parkingViewModel.selectParking(TestInstancesParking.parking1)
    reviewViewModel.addReview(TestInstancesReview.review1)
  }

  @Test
  fun reviewScreen_hasTopAppBar() {
    composeTestRule.setContent {
      ReviewScreen(mockNavigationActions, parkingViewModel, reviewViewModel, userViewModel)
    }

    composeTestRule
        .onNodeWithTag("TopAppBarTitle")
        .assertIsDisplayed()
        .assertTextContains("Add your review")
  }

  @Test
  fun reviewScreen_sliderChanges() {
    composeTestRule.setContent {
      ReviewScreen(mockNavigationActions, parkingViewModel, reviewViewModel, userViewModel)
    }
    // Perform actions on the slider
    composeTestRule.onNodeWithTag("Slider").performTouchInput { swipeRight() }
  }

  @Test
  fun reviewScreen_addReviewButtonSaves() {
    doNothing().`when`(mockNavigationActions).navigateTo(Screen.PARKING_DETAILS)

    composeTestRule.setContent {
      ReviewScreen(mockNavigationActions, parkingViewModel, reviewViewModel, userViewModel)
    }
    // Enter a review
    composeTestRule.onNodeWithTag("ReviewInput").performTextInput("Great parking!")

    // Click Add Review button
    composeTestRule.onNodeWithTag("AddReviewButton").performClick()
  }
}
