package com.github.se.cyrcle.ui.report

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.cyrcle.di.mocks.*
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.report.ReportedObject
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.report.ReportedObjectType
import com.github.se.cyrcle.model.report.ReportedObjectViewModel
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class AdminScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var parkingRepository: ParkingRepository
  private lateinit var userRepository: UserRepository
  private lateinit var reviewRepository: ReviewRepository
  private lateinit var reportedObjectRepository: ReportedObjectRepository

  private lateinit var userViewModel: UserViewModel
  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var reviewViewModel: ReviewViewModel
  private lateinit var reportedObjectViewModel: ReportedObjectViewModel

  private lateinit var navigationActions: NavigationActions

  val mockReport =
      ReportedObject(
          objectUID = "mockUID",
          reportUID = "",
          userUID = "",
          nbOfTimesReported = 5,
          nbOfTimesMaxSeverityReported = 2,
          objectType = ReportedObjectType.PARKING)

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)

    parkingRepository = MockParkingRepository()
    userRepository = MockUserRepository()
    reviewRepository = MockReviewRepository()
    reportedObjectRepository = MockReportedObjectRepository()

    parkingViewModel =
        ParkingViewModel(MockImageRepository(), parkingRepository, reportedObjectRepository)
    userViewModel =
        UserViewModel(
            userRepository,
            parkingRepository,
            MockImageRepository(),
            MockAuthenticationRepository())
    userViewModel.setCurrentUser(TestInstancesUser.user1)
    reviewViewModel = ReviewViewModel(reviewRepository, reportedObjectRepository)
    reportedObjectViewModel = ReportedObjectViewModel(reportedObjectRepository)
    parkingViewModel.addParking(TestInstancesParking.parking2)
    parkingViewModel.addParking(TestInstancesParking.parking3)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.PARKING_DETAILS)
  }

  @Test
  fun testAdminScreenRendersCorrectly() {
    composeTestRule.setContent {
      AdminScreen(
          navigationActions = navigationActions,
          reportedObjectViewModel = reportedObjectViewModel,
          parkingViewModel = parkingViewModel,
          reviewViewModel = reviewViewModel)
    }

    // Check if top bar is displayed
    composeTestRule.onNodeWithTag("AdminTopBar").assertExists()

    // Check if the report list is displayed
    composeTestRule.onNodeWithTag("ReportList").assertExists()
  }

  @Test
  fun testReportCardDisplaysCorrectly() {
    // Mock data

    composeTestRule.setContent {
      AdminScreen(
          navigationActions = navigationActions,
          reportedObjectViewModel = reportedObjectViewModel,
          parkingViewModel = parkingViewModel,
          reviewViewModel = reviewViewModel)
    }

    // Check if report card displays correct data
    composeTestRule
        .onNodeWithTag("ReportCardContent0")
        .assertTextContains("5") // Number of times reported
    composeTestRule
        .onNodeWithTag("ReportCardContent0")
        .assertTextContains("2") // Number of times max severity reported
  }

  @Test
  fun testNavigationToViewReports() {

    composeTestRule.setContent {
      AdminScreen(
          navigationActions = navigationActions,
          reportedObjectViewModel = reportedObjectViewModel,
          parkingViewModel = parkingViewModel,
          reviewViewModel = reviewViewModel)
    }

    // Click the check reports button
    composeTestRule.onNodeWithTag("CheckReportsButton0").performClick()

    // Verify navigation
    verify(navigationActions).navigateTo(Screen.VIEW_REPORTS)
  }
}
