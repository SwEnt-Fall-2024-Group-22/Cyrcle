package com.github.se.cyrcle.ui.report

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.*
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
class ParkingReportScreenTest {

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

        parkingViewModel =
            ParkingViewModel(
                MockImageRepository(),
                parkingRepository,
                MockOfflineParkingRepository(),
                reportedObjectRepository
            )
        userViewModel =
            UserViewModel(
                userRepository,
                parkingRepository,
                MockImageRepository(),
                MockAuthenticationRepository()
            )
        userViewModel.setCurrentUser(TestInstancesUser.user1)
        reviewViewModel = ReviewViewModel(reviewRepository, reportedObjectRepository)

        parkingViewModel.addParking(TestInstancesParking.parking2)
        parkingViewModel.addParking(TestInstancesParking.parking3)

        `when`(navigationActions.currentRoute()).thenReturn(Screen.PARKING_DETAILS)
    }
    @Test
    fun parkingReportScreenDisplaysCorrectly() {
        composeTestRule.setContent {
            ParkingReportScreen(
                navigationActions = navigationActions,
                userViewModel = userViewModel,
                parkingViewModel = parkingViewModel
            )
        }

        // Assert that the report bullet points section is displayed
        composeTestRule.onNodeWithTag("ReportBulletPoints").assertExists()

        // Assert that the dropdown for reason selection is displayed
        composeTestRule.onNodeWithTag("ReasonDropdown").assertExists()

        // Assert that the input field for additional details is displayed
        composeTestRule.onNodeWithTag("DetailsInput").assertExists()

        // Assert that the submit button is displayed by its text
        composeTestRule.onNodeWithText("Submit").assertExists()
    }

    @Test
    fun submitButtonDisplaysDialogWhenClicked() {
        composeTestRule.setContent {
            ParkingReportScreen(
                navigationActions = navigationActions,
                userViewModel = userViewModel,
                parkingViewModel = parkingViewModel
            )
        }

        // Click on the submit button by its text
        composeTestRule.onNodeWithText("Submit").performClick()

        // Assert that the dialog exists
        composeTestRule.onNodeWithTag("ReportScreenAlertDialog").assertExists()
    }

    @Test
    fun detailsInputAcceptsTextInput() {
        composeTestRule.setContent {
            ParkingReportScreen(
                navigationActions = navigationActions,
                userViewModel = userViewModel,
                parkingViewModel = parkingViewModel
            )
        }

        // Enter text in the details input field
        val detailsText = "This parking has accessibility issues."
        composeTestRule.onNodeWithTag("DetailsInput").performTextInput(detailsText)

        // Assert that the input field contains the entered text
        composeTestRule.onNodeWithTag("DetailsInput").assertTextContains(detailsText)
    }

    @Test
    fun reasonDropDownIsClickable() {
        composeTestRule.setContent {
            ParkingReportScreen(
                navigationActions = navigationActions,
                userViewModel = userViewModel,
                parkingViewModel = parkingViewModel
            )
        }

        // Click on the dropdown for reason selection
        composeTestRule.onNodeWithTag("ReasonDropdown").performClick()
    }
}