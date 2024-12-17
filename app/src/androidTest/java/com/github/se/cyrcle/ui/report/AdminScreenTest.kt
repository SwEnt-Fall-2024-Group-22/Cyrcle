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
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class AdminScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var reviewViewModel: ReviewViewModel
  private lateinit var reportedObjectViewModel: ReportedObjectViewModel

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)

    // Initialize ViewModels with Mock Repositories
    parkingViewModel =
        ParkingViewModel(
            MockImageRepository(),
            userViewModel =
                UserViewModel(
                    MockUserRepository(),
                    MockParkingRepository(),
                    MockImageRepository(),
                    MockAuthenticationRepository()),
            MockParkingRepository(),
            MockOfflineParkingRepository(),
            MockReportedObjectRepository())
    reviewViewModel = ReviewViewModel(MockReviewRepository(), MockReportedObjectRepository())
    reportedObjectViewModel = ReportedObjectViewModel(MockReportedObjectRepository())

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
    composeTestRule.onNodeWithTag("TopAppBar").assertExists()

    // Check if the report list is displayed
    composeTestRule.onNodeWithTag("ReportList").assertExists()
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

    // Click on sort by Parking
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
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

    // Expand the first report card
    composeTestRule.onNodeWithTag("ReportCard0").performClick()
    composeTestRule.waitForIdle()

    // Click the More Options button
    composeTestRule.onNodeWithTag("MoreOptionsButton0").performClick()
    composeTestRule.waitForIdle()

    // Assert dialog appears
    composeTestRule.onNode(isDialog()).assertExists()

    // Validate dialog content
    composeTestRule.onNodeWithText("Details for this Reported Object:").assertExists()
    composeTestRule.onNodeWithText("Loading parking details...").assertExists()

    // Validate buttons
    composeTestRule.onNodeWithText("Go To Parking").assertExists().assertHasClickAction()
    composeTestRule.onNodeWithText("Return").assertExists().assertHasClickAction()

    // Dismiss the dialog
    composeTestRule.onNodeWithText("Return").performClick()
    composeTestRule.waitForIdle()

    // Ensure dialog is dismissed
    composeTestRule.onNode(isDialog()).assertDoesNotExist()
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
    composeTestRule
        .onNodeWithTag("BulletPointParkingUID")
        .assertExists()
        .onChildren() // Focus on child nodes to inspect contents
        .assertAny(hasText("Parking UID: Test_spot_1"))

    composeTestRule
        .onNodeWithTag("BulletPointParkingOwner")
        .assertExists()
        .onChildren()
        .assertAny(hasText("Owner: user1"))

    // Print the state of the root composable after validation
    composeTestRule.onRoot().printToLog("ParkingDetailsTestRootAfterValidation")
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

    // Directly assert the text content exists
    composeTestRule.onNodeWithText("Owner: user1").assertExists()
  }
}
