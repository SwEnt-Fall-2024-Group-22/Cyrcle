package com.github.se.cyrcle.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class ProfileScreenMock {

    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var mockNavigationActions: NavigationActions
    private lateinit var mockUserRepository: MockUserRepository
    private lateinit var mockParkingRepository: MockParkingRepository

    private lateinit var userViewModel: UserViewModel

    @Before
    fun setUp() {
        mockNavigationActions = mock(NavigationActions::class.java)
        mockUserRepository = MockUserRepository()
        mockParkingRepository = MockParkingRepository()

        // Set up mock user data
        val user = User(
            userId = "1",
            username = "janesmith",
            firstName = "Jane",
            lastName = "Smith",
            email = "jane.smith@example.com",
            profilePictureUrl = "http://example.com/jane.jpg"
        )
        mockUserRepository.addUser(user, {}, {})

        userViewModel = UserViewModel(mockUserRepository, mockParkingRepository)

        // Fetch the user
        userViewModel.getUserById("1")

        composeTestRule.setContent {
            ProfileScreen(
                navigationActions = mockNavigationActions,
                userViewModel = userViewModel
            )
        }
    }

    @Test
    fun testInitialDisplayMode() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("ProfileImage").assertIsDisplayed()
        composeTestRule.onNodeWithTag("EditButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("DisplayFirstName").assertTextEquals("Jane")
        composeTestRule.onNodeWithTag("DisplayLastName").assertTextEquals("Smith")
        composeTestRule.onNodeWithTag("DisplayUsername").assertTextEquals("@janesmith")
    }

    @Test
    fun testEditModeTransition() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("EditButton").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("ProfileImage").assertIsDisplayed()
        composeTestRule.onNodeWithTag("EditButton").assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("FirstNameField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("LastNameField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("UsernameField").assertIsDisplayed()
    }

    @Test
    fun testSaveChanges() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("EditButton").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("FirstNameField").performTextReplacement("Alice")
        composeTestRule.onNodeWithTag("LastNameField").performTextReplacement("Johnson")
        composeTestRule.onNodeWithTag("UsernameField").performTextReplacement("alicejohnson")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("SaveButton").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("ProfileImage").assertIsDisplayed()
        composeTestRule.onNodeWithTag("EditButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("DisplayFirstName").assertTextEquals("Alice")
        composeTestRule.onNodeWithTag("DisplayLastName").assertTextEquals("Johnson")
        composeTestRule.onNodeWithTag("DisplayUsername").assertTextEquals("@alicejohnson")
    }

    @Test
    fun testCancelEditMode() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("EditButton").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("FirstNameField").performTextReplacement("Alice")
        composeTestRule.onNodeWithTag("LastNameField").performTextReplacement("Johnson")
        composeTestRule.onNodeWithTag("UsernameField").performTextReplacement("@alicejohnson")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("CancelButton").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("ProfileImage").assertIsDisplayed()
        composeTestRule.onNodeWithTag("EditButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("DisplayFirstName").assertTextEquals("Jane")
        composeTestRule.onNodeWithTag("DisplayLastName").assertTextEquals("Smith")
        composeTestRule.onNodeWithTag("DisplayUsername").assertTextEquals("@janesmith")
    }

    @Test
    fun testCantChangeProfilePicture() {
        // Dont enter edit mode
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("ProfileImage").assertHasNoClickAction()
    }

    @Test
    fun testChangeProfilePicture() {
        // Enter edit mode
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("EditButton").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("ProfileImage").assertHasClickAction()
    }

}