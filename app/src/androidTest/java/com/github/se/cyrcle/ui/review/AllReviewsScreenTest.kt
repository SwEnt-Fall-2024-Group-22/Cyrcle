package com.github.se.cyrcle.ui.review

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockAuthenticationRepository
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockReportedObjectRepository
import com.github.se.cyrcle.di.mocks.MockReviewRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.review.TestInstancesReview
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class AllReviewsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var parkingRepository: MockParkingRepository
  private lateinit var reviewRepository: MockReviewRepository
  private lateinit var userRepository: MockUserRepository
  private lateinit var imageRepository: MockImageRepository
  private lateinit var authenticator: MockAuthenticationRepository
  private lateinit var reportedObjectRepository: MockReportedObjectRepository

  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var reviewViewModel: ReviewViewModel
  private lateinit var userViewModel: UserViewModel

  @Before
  fun setUp() {

    navigationActions = mock(NavigationActions::class.java)
    parkingRepository = MockParkingRepository()
    reviewRepository = MockReviewRepository()
    userRepository = MockUserRepository()
    imageRepository = MockImageRepository()
    authenticator = MockAuthenticationRepository()
    reportedObjectRepository = MockReportedObjectRepository()

    parkingViewModel =
        ParkingViewModel(imageRepository, parkingRepository, reportedObjectRepository)
    reviewViewModel = ReviewViewModel(reviewRepository, reportedObjectRepository)
    userViewModel = UserViewModel(userRepository, parkingRepository, imageRepository, authenticator)

    reviewViewModel.addReview(TestInstancesReview.review4)
    reviewViewModel.addReview(TestInstancesReview.review3)
    reviewViewModel.addReview(TestInstancesReview.review2)
    reviewViewModel.addReview(TestInstancesReview.review1)
    parkingViewModel.selectParking(TestInstancesParking.parking2)
  }

  @Test
  fun allReviewsScreen_displaysList() {

    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
      userViewModel.setCurrentUser(TestInstancesUser.user1)
    }

    composeTestRule.onNodeWithTag("ReviewCard0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ReviewCard1").assertIsDisplayed()
  }

  @Test
  fun clickingReviewCard_expandsCard() {
    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
    }

    composeTestRule.onNodeWithTag("ReviewCard0").performClick()
    composeTestRule.onNodeWithText("New Review.").assertIsDisplayed()
  }

  @Test
  fun sortingReviewsChangesCardOrder() {
    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
    }

    val firstReviewCardB4 = composeTestRule.onNodeWithTag("ReviewCard0")
    // Open filter and select sorting by rating
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
    composeTestRule.onNodeWithText("Sort By Rating (Best Rated First)").performClick()

    // Verify if the reviews are sorted by rating by checking the order of ReviewCards
    val firstReviewCardAfter = composeTestRule.onNodeWithTag("ReviewCard0")
    assertNotEquals(firstReviewCardB4, firstReviewCardAfter)
  }

  @Test
  fun clickingAnotherReviewCard_expandsCorrectCard() {

    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
      userViewModel.setCurrentUser(TestInstancesUser.user1)
    }
    composeTestRule.onNodeWithTag("ReviewCard1").performClick()
    composeTestRule.onNodeWithText("Bad Parking.").assertIsDisplayed()
  }

  @Test
  fun clickingFilterButton_displaysFilterOptions() {
    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
    }

    // Click the filter button
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()

    // Verify that the filter options are displayed
    composeTestRule.onNodeWithText("Sort By Rating (Best Rated First)").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sort By Date (Most Recent First)").assertIsDisplayed()
  }

  @Test
  fun clickingAddEditReviewButton_navigatesToReviewScreen() {
    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
      userViewModel.setCurrentUser(TestInstancesUser.user1)
    }

    // Click the "Add/Edit Review" button
    composeTestRule.onNodeWithTag("AddOrEditReviewButton").performClick()

    verify(navigationActions).navigateTo(Screen.ADD_REVIEW)
  }

  @Test
  fun clickingDeleteReviewButton_doesntMoveIfUnneeded() {
    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
      userViewModel.setCurrentUser(TestInstancesUser.user1)
    }
    composeTestRule.onNodeWithTag("ReviewCard0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DeleteReviewButton").performClick()
    verify(navigationActions, times(0)).navigateTo(Screen.PARKING_DETAILS)
  }

  @Test
  fun dropdownMenu_displaysOptions() {
    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
      userViewModel.setCurrentUser(TestInstancesUser.user1)
    }
    val tag0 = "MoreOptions0"
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ReviewCard0").assertIsDisplayed()

    // Click the dropdown menu
    composeTestRule
        .onNodeWithTag("${tag0}Button")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()

    // Verify that the dropdown menu options are displayed
    composeTestRule.onNodeWithTag("${tag0}Menu").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${tag0}ReportReviewItem")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()
    verify(navigationActions).navigateTo(Screen.REVIEW_REPORT)
  }
}
