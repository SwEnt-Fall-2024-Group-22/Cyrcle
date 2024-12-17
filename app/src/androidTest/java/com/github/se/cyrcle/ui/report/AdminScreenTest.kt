package com.github.se.cyrcle.ui.report

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.*
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking.parking1
import com.github.se.cyrcle.model.report.ReportedObject
import com.github.se.cyrcle.model.report.ReportedObjectType
import com.github.se.cyrcle.model.report.ReportedObjectViewModel
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class AdminScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions

  // Repositories
  private lateinit var imageRepository: MockImageRepository
  private lateinit var userRepository: MockUserRepository
  private lateinit var parkingRepository: MockParkingRepository
  private lateinit var offlineParkingRepository: MockOfflineParkingRepository
  private lateinit var reportedObjectRepository: MockReportedObjectRepository
  private lateinit var reviewRepository: MockReviewRepository
  private lateinit var authenticationRepository: MockAuthenticationRepository

  // ViewModels
  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var reviewViewModel: ReviewViewModel
  private lateinit var reportedObjectViewModel: ReportedObjectViewModel

  @Before
  fun setUp() {
    // Initialize Repositories
    imageRepository = MockImageRepository()
    userRepository = MockUserRepository()
    parkingRepository = MockParkingRepository()
    offlineParkingRepository = MockOfflineParkingRepository()
    reportedObjectRepository = MockReportedObjectRepository()
    reviewRepository = MockReviewRepository()
    authenticationRepository = MockAuthenticationRepository()

    // Initialize Navigation Actions
    navigationActions = mock(NavigationActions::class.java)

    // Initialize ViewModels with Mock Repositories
    parkingViewModel =
        ParkingViewModel(
            imageRepository,
            userViewModel =
                UserViewModel(
                    userRepository, parkingRepository, imageRepository, authenticationRepository),
            parkingRepository,
            offlineParkingRepository,
            reportedObjectRepository)

    reviewViewModel = ReviewViewModel(reviewRepository, reportedObjectRepository)
    reportedObjectViewModel = ReportedObjectViewModel(reportedObjectRepository)

    val mockReport1 =
        ReportedObject(
            objectUID = "mockUID1",
            reportUID = "report1",
            userUID = "user1",
            nbOfTimesReported = 5,
            nbOfTimesMaxSeverityReported = 2,
            objectType = ReportedObjectType.PARKING)
    val mockReport2 =
        ReportedObject(
            objectUID = "mockUID2",
            reportUID = "report2",
            userUID = "user2",
            nbOfTimesReported = 3,
            nbOfTimesMaxSeverityReported = 1,
            objectType = ReportedObjectType.REVIEW)

    val updatedList = reportedObjectViewModel.reportsList.value.toMutableList()
    updatedList.add(mockReport1)
    updatedList.add(mockReport2)
    reportedObjectViewModel._reportsList.value = updatedList
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

    // Check if the top bar is displayed
    composeTestRule.onNodeWithTag("TopAppBar").assertIsDisplayed()

    // Check if the report list is displayed
    composeTestRule.onNodeWithTag("ReportList").assertIsDisplayed()
  }

  @Test
  fun testReportCardDisplaysCorrectly() {

    // Act: Set the content
    composeTestRule.setContent {
      AdminScreen(
          navigationActions = navigationActions,
          reportedObjectViewModel = reportedObjectViewModel,
          parkingViewModel = parkingViewModel,
          reviewViewModel = reviewViewModel)
    }

    // Assert: Check if the text is displayed correctly
    composeTestRule
        .onNodeWithTag("TimesMaxSeverityReported0", useUnmergedTree = true)
        .assertTextContains("Has been reported at max level 2 times")

    composeTestRule
        .onNodeWithTag("TimesReported0", useUnmergedTree = true)
        .assertTextContains("Has been reported 5 times")
  }

  @Test
  fun testFilterSectionTogglesCorrectly() {
    composeTestRule.setContent {
      AdminScreen(
          navigationActions = navigationActions,
          reportedObjectViewModel = reportedObjectViewModel,
          parkingViewModel = parkingViewModel,
          reviewViewModel = reviewViewModel)
    }

    // Verify initial state: Filter button exists and filters are hidden
    composeTestRule.onNodeWithTag("ShowFiltersButton").assertExists()
    composeTestRule.onNodeWithTag("ViewReports").assertDoesNotExist() // Ensure filters are hidden
  }

  @Test
  fun testSortingOptionSelectorWorksCorrectly() {
    composeTestRule.setContent {
      AdminScreen(
          navigationActions = navigationActions,
          reportedObjectViewModel = reportedObjectViewModel,
          parkingViewModel = parkingViewModel,
          reviewViewModel = reviewViewModel)
    }

    composeTestRule.onNodeWithTag("ShowFiltersButton").assertHasClickAction()
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
    composeTestRule
        .onNodeWithText("Get Reported Parkings (Most Reported First)")
        .assertHasClickAction()
    composeTestRule.onNodeWithText("Get Reported Parkings (Most Reported First)").performClick()

    // Check if the list is sorted by Parking
    val firstReportCard = composeTestRule.onNodeWithTag("ReportCard0")
    firstReportCard.assertTextContains("Has been reported at max level 2 times")
  }

  @Test
  fun testNavigationOpensCheckReportsButton() {
    composeTestRule.setContent {
      AdminScreen(
          navigationActions = navigationActions,
          reportedObjectViewModel = reportedObjectViewModel,
          parkingViewModel = parkingViewModel,
          reviewViewModel = reviewViewModel)
    }

    composeTestRule.onNodeWithTag("ReportCard0").performClick()
    composeTestRule.onNodeWithTag("CheckReportsButton0").performClick()
  }

  @Test
  fun testAlertDialogAppearsOnMoreOptionsClick() {
    composeTestRule.setContent {
      AdminScreen(
          navigationActions = navigationActions,
          reportedObjectViewModel = reportedObjectViewModel,
          parkingViewModel = parkingViewModel,
          reviewViewModel = reviewViewModel)
    }
    composeTestRule.onNodeWithTag("ReportCard0").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("MoreOptionsButton0").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNode(isDialog()).assertIsDisplayed()
    composeTestRule.onNodeWithText("Details for this Reported Object:").assertIsDisplayed()
    composeTestRule.onNodeWithText("Loading parking details...").assertIsDisplayed()
    composeTestRule.onNodeWithText("Go To Parking").assertIsDisplayed().assertHasClickAction()
    composeTestRule.onNodeWithText("Return").assertIsDisplayed().assertHasClickAction()
    composeTestRule.onNodeWithText("Return").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNode(isDialog()).assertIsNotDisplayed()
  }

  @Test
  fun testReportDetailsContentForParkingType() {
    composeTestRule.setContent {
      ReportDetailsContent(
          objectType = ReportedObjectType.PARKING,
          objectUID = parking1.uid,
          userUID = parking1.owner,
          reviewViewModel = reviewViewModel,
          parkingViewModel = parkingViewModel)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Parking UID: Test_spot_1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Owner: user1").assertIsDisplayed()
  }

  @Test
  fun testReportDetailsContentForImageType() {
    composeTestRule.setContent {
      ReportDetailsContent(
          objectType = ReportedObjectType.IMAGE,
          objectUID = "image_456",
          userUID = "user1",
          reviewViewModel = reviewViewModel,
          parkingViewModel = parkingViewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Owner: user1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Image UID: image_456").assertIsDisplayed()
  }
}
