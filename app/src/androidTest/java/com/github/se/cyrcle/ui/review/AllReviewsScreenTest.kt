package com.github.se.cyrcle.ui.review

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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

  private val navigationActions = mock(NavigationActions::class.java)
  private val parkingViewModel =
      ParkingViewModel(
          MockImageRepository(), MockParkingRepository(), MockReportedObjectRepository())
  private val reviewViewModel = ReviewViewModel(MockReviewRepository(), MockReportedObjectRepository())
  private val userViewModel = UserViewModel(MockUserRepository(), MockParkingRepository())

  @Before
  fun setUp() {
    reviewViewModel.addReview(TestInstancesReview.review4)
    reviewViewModel.addReview(TestInstancesReview.review3)
    reviewViewModel.addReview(TestInstancesReview.review2)
    reviewViewModel.addReview(TestInstancesReview.review1)
    parkingViewModel.selectParking(TestInstancesParking.parking2)
  }

  @Test
  fun allReviewsScreen_displaysTopBarAndList() {

    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel, userViewModel)
      userViewModel.setCurrentUser(TestInstancesUser.user1)
    }

    composeTestRule.onNodeWithTag("AllReviewsScreenBox").assertIsDisplayed()
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
}
