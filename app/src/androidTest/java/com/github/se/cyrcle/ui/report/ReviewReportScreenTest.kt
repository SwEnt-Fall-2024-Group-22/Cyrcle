package com.github.se.cyrcle.ui.report

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockAuthenticationRepository
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockOfflineParkingRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockReportedObjectRepository
import com.github.se.cyrcle.di.mocks.MockReviewRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.parking.online.ParkingRepository
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.review.ReviewRepository
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.github.se.cyrcle.model.user.UserRepository
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class ReviewReportScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var parkingRepository: ParkingRepository
  private lateinit var userRepository: UserRepository
  private lateinit var reviewRepository: ReviewRepository
  private lateinit var reportedObjectRepository: ReportedObjectRepository

  private lateinit var userViewModel: UserViewModel
  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var reviewViewModel: ReviewViewModel

  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)

    parkingRepository = MockParkingRepository()
    userRepository = MockUserRepository()
    reviewRepository = MockReviewRepository()
    reportedObjectRepository = MockReportedObjectRepository()

    userViewModel =
        UserViewModel(
            userRepository,
            parkingRepository,
            MockImageRepository(),
            MockAuthenticationRepository())
    userViewModel.setCurrentUser(TestInstancesUser.user1)
    parkingViewModel =
        ParkingViewModel(
            MockImageRepository(),
            userViewModel,
            parkingRepository,
            MockOfflineParkingRepository(),
            reportedObjectRepository)

    reviewViewModel = ReviewViewModel(reviewRepository, reportedObjectRepository)

    parkingViewModel.addParking(TestInstancesParking.parking2)
    parkingViewModel.addParking(TestInstancesParking.parking3)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.PARKING_DETAILS)
  }

  @Test
  fun reviewReportScreenDisplaysCorrectly() {
    composeTestRule.setContent {
      ReviewReportScreen(
          navigationActions = navigationActions,
          userViewModel = userViewModel,
          reviewViewModel = reviewViewModel)
    }

    // Assert that the title is displayed
    composeTestRule.onNodeWithTag("ReportBulletPoints").assertExists()

    // Assert that bullet points are displayed
    composeTestRule.onNodeWithTag("ReportBulletPoints").assertExists()

    // Assert that the reason dropdown is displayed
    composeTestRule.onNodeWithTag("ReasonDropdown").assertExists()

    // Assert that the details input field is displayed
    composeTestRule.onNodeWithTag("DetailsInput").assertExists()

    // Assert that the submit button is displayed by text
    composeTestRule.onNodeWithText("Submit").assertExists()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun submitButtonDisplaysDialogWhenClicked() {
    composeTestRule.setContent {
      ReviewReportScreen(
          navigationActions = navigationActions,
          userViewModel = userViewModel,
          reviewViewModel = reviewViewModel)
    }

    // Click on the submit button
    composeTestRule.onNodeWithText("Submit").performClick()

    // Wait for the dialog
    composeTestRule.waitUntilAtLeastOneExists(
        hasTestTag("ReportScreenAlertDialog"), timeoutMillis = 5000)

    // Assert that the dialog exists
    composeTestRule.onNodeWithTag("ReportScreenAlertDialog").assertExists()
  }

  @Test
  fun detailsInputAcceptsTextInput() {
    composeTestRule.setContent {
      ReviewReportScreen(
          navigationActions = navigationActions,
          userViewModel = userViewModel,
          reviewViewModel = reviewViewModel)
    }

    val testInput = "This review is misleading."

    composeTestRule.onNodeWithTag("DetailsInput").performTextInput(testInput)
    composeTestRule.onNodeWithTag("DetailsInput").assertTextContains(testInput)
  }

  @Test
  fun reasonDropDownIsClickable() {
    composeTestRule.setContent {
      ReviewReportScreen(
          navigationActions = navigationActions,
          userViewModel = userViewModel,
          reviewViewModel = reviewViewModel)
    }

    composeTestRule.onNodeWithTag("ReasonDropdown").performClick()
  }
}
