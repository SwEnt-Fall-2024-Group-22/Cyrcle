package com.github.se.cyrcle.ui.report

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.*
import com.github.se.cyrcle.model.parking.ImageReportReason
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.theme.molecules.SubmitButtonWithDialog
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class ImageReportScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navigationActions: NavigationActions
    private lateinit var parkingViewModel: ParkingViewModel
    private lateinit var userViewModel: UserViewModel

    @Before
    fun setUp() {
        navigationActions = Mockito.mock(NavigationActions::class.java)

        parkingViewModel = ParkingViewModel(
            MockImageRepository(),
            MockParkingRepository(),
            MockOfflineParkingRepository(),
            MockReportedObjectRepository()
        )
        userViewModel = UserViewModel(
            MockUserRepository(),
            MockParkingRepository(),
            MockImageRepository(),
            MockAuthenticationRepository()
        )

        userViewModel.setCurrentUser(TestInstancesUser.user1)
        parkingViewModel.selectImage("testImage123") // Simulating an image is selected
    }

    @Test
    fun imageReportScreen_displaysCorrectly() {
        composeTestRule.setContent {
            ImageReportScreen(
                navigationActions = navigationActions,
                userViewModel = userViewModel,
                parkingViewModel = parkingViewModel
            )
        }

        // Assert title is displayed
        composeTestRule.onNodeWithTag("ImageReportScreen").assertExists()

        // Assert bullet points section is displayed
        composeTestRule.onNodeWithTag("ReportBulletPoints").assertExists()

        // Assert dropdown for selecting reasons is displayed
        composeTestRule.onNodeWithTag("ReasonDropdown").assertExists()

        // Assert the input field for description is displayed
        composeTestRule.onNodeWithTag("DetailsInput").assertExists()

        // Assert the submit button is displayed
        composeTestRule.onNodeWithText("Submit").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun submitButton_displaysDialogWhenClicked() {
        composeTestRule.setContent {
            ImageReportScreen(
                navigationActions = navigationActions,
                userViewModel = userViewModel,
                parkingViewModel = parkingViewModel
            )
        }

        // Perform click on submit button
        composeTestRule.onNodeWithText("Submit").performClick()

        // Wait for dialog to appear
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("ReportScreenAlertDialog"), 5000)

        // Assert dialog exists
        composeTestRule.onNodeWithTag("ReportScreenAlertDialog").assertExists()
    }

    @Test
    fun detailsInput_acceptsTextInput() {
        composeTestRule.setContent {
            ImageReportScreen(
                navigationActions = navigationActions,
                userViewModel = userViewModel,
                parkingViewModel = parkingViewModel
            )
        }

        val inputText = "This is an issue with the image."

        // Input text in the details field
        composeTestRule.onNodeWithTag("DetailsInput").performTextInput(inputText)

        // Assert the input field contains the entered text
        composeTestRule.onNodeWithTag("DetailsInput").assertTextContains(inputText)
    }
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun reasonDropdown_isClickable() {
        composeTestRule.setContent {
            ImageReportScreen(
                navigationActions = navigationActions,
                userViewModel = userViewModel,
                parkingViewModel = parkingViewModel
            )
        }

        // Click on the dropdown to expand it
        composeTestRule.onNodeWithTag("ReasonDropdown").performClick()

        // Wait for the dropdown options to render
        composeTestRule.waitUntilAtLeastOneExists(
            hasText("OTHER", ignoreCase = true),
            timeoutMillis = 5000
        )

        // Assert the dropdown option exists
        composeTestRule.onNodeWithText("OTHER", ignoreCase = true).assertExists()
    }
}