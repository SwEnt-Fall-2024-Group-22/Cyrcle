package com.github.se.cyrcle.ui.report

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockAuthenticationRepository
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockOfflineParkingRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockReportedObjectRepository
import com.github.se.cyrcle.di.mocks.MockReviewRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.parking.ParkingReport
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.report.ReportedObject
import com.github.se.cyrcle.model.report.ReportedObjectType
import com.github.se.cyrcle.model.report.ReportedObjectViewModel
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class ViewReportsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationActions: NavigationActions
  private lateinit var reportedObjectViewModel: ReportedObjectViewModel
  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var reviewViewModel: ReviewViewModel
  private lateinit var userViewModel: UserViewModel

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)

    userViewModel =
        UserViewModel(
            MockUserRepository(),
            MockParkingRepository(),
            MockImageRepository(),
            MockAuthenticationRepository())
    parkingViewModel =
        ParkingViewModel(
            MockImageRepository(),
            userViewModel,
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
  fun testDisplaysNoReportsWhenListIsEmpty() {
    composeTestRule.setContent {
      ViewReportsScreen(
          navigationActions = navigationActions,
          reportedObjectViewModel = reportedObjectViewModel,
          parkingViewModel = parkingViewModel,
          reviewViewModel = reviewViewModel)
    }

    composeTestRule.onNodeWithTag("NoReportsText").assertExists()
  }

  @Test
  fun testDisplaysReportsListWhenNotEmpty() {

    composeTestRule.setContent {
      ViewReportsScreen(
          navigationActions = navigationActions,
          reportedObjectViewModel = reportedObjectViewModel,
          parkingViewModel = parkingViewModel,
          reviewViewModel = reviewViewModel)
    }

    val parkingReport = ParkingReport()
    parkingViewModel.selectParking(TestInstancesParking.parking1)
    composeTestRule.onRoot(useUnmergedTree = true).printToLog("ViewReportsScreen")

    composeTestRule.onNodeWithTag("ReportsList").assertExists()
    composeTestRule.onNodeWithTag("ReportCard").assertExists()
  }

  @Test
  fun testDeleteFloatingActionButtonNavigatesBack() {
    val mockReport =
        ReportedObject(
            objectUID = "mockUID",
            reportUID = "mockReportUID",
            userUID = "mockUserUID",
            nbOfTimesReported = 3,
            nbOfTimesMaxSeverityReported = 1,
            objectType = ReportedObjectType.PARKING)
    reportedObjectViewModel.selectObject(mockReport)

    composeTestRule.setContent {
      ViewReportsScreen(
          navigationActions = navigationActions,
          reportedObjectViewModel = reportedObjectViewModel,
          parkingViewModel = parkingViewModel,
          reviewViewModel = reviewViewModel)
    }

    composeTestRule.onNodeWithTag("DeleteFloatingActionButton").performClick()

    verify(navigationActions).navigateTo(Screen.ADMIN)
  }
}
