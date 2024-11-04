package com.github.se.cyrcle.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class ViewProfileScreenTest {
  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var mockNavController: NavHostController

  @Before
  fun setupProfileScreen() {
    mockNavController = mock(NavHostController::class.java)
    val navigationActions = NavigationActions(mockNavController)
    composeTestRule.setContent { ViewProfileScreen(navigationActions = navigationActions) }
  }

  @Test
  fun testInitialDisplayMode() {
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("ProfileImage").fetchSemanticsNodes().isNotEmpty()
    }

    // Verify initial display mode elements
    composeTestRule.onNodeWithTag("ProfileImage").assertExists()
    composeTestRule.onNodeWithTag("EditButton").assertExists()
    composeTestRule.onNodeWithTag("DisplayFirstName").assertExists()
    composeTestRule.onNodeWithTag("DisplayLastName").assertExists()
    composeTestRule.onNodeWithTag("DisplayUsername").assertExists()
    composeTestRule.onNodeWithTag("FavoriteParkingsTitle").assertExists()
  }

  @Test
  fun testEditModeTransition() {
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("EditButton").fetchSemanticsNodes().isNotEmpty()
    }

    // Enter edit mode
    composeTestRule.onNodeWithTag("EditButton").performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("SaveButton").fetchSemanticsNodes().isNotEmpty()
    }

    // Verify edit mode elements
    composeTestRule.onNodeWithTag("FirstNameField").assertExists()
    composeTestRule.onNodeWithTag("LastNameField").assertExists()
    composeTestRule.onNodeWithTag("UsernameField").assertExists()
    composeTestRule.onNodeWithTag("SaveButton").assertExists()
    composeTestRule.onNodeWithTag("CancelButton").assertExists()
  }

  @Test
  fun testCancelEditMode() {
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("EditButton").fetchSemanticsNodes().isNotEmpty()
    }

    // Enter edit mode
    composeTestRule.onNodeWithTag("EditButton").performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("FirstNameField").fetchSemanticsNodes().isNotEmpty()
    }

    // Clear existing text and modify fields
    composeTestRule.onNodeWithTag("FirstNameField").performTextClearance()
    composeTestRule.onNodeWithTag("FirstNameField").performTextInput("New")

    composeTestRule.onNodeWithTag("LastNameField").performTextClearance()
    composeTestRule.onNodeWithTag("LastNameField").performTextInput("Name")

    composeTestRule.onNodeWithTag("UsernameField").performTextClearance()
    composeTestRule.onNodeWithTag("UsernameField").performTextInput("newuser")

    // Cancel edit mode
    composeTestRule.onNodeWithTag("CancelButton").performClick()

    // Add a small delay to ensure state updates
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("DisplayFirstName").fetchSemanticsNodes().isNotEmpty()
    }

    // Verify return to display mode with unchanged values
    composeTestRule.onNodeWithTag("DisplayFirstName").assertTextEquals("John")
    composeTestRule.onNodeWithTag("DisplayLastName").assertTextEquals("Doe")
    composeTestRule.onNodeWithTag("DisplayUsername").assertTextEquals("@johndoe")
  }

  @Test
  fun testSaveChanges() {
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("EditButton").fetchSemanticsNodes().isNotEmpty()
    }

    // Enter edit mode
    composeTestRule.onNodeWithTag("EditButton").performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("FirstNameField").fetchSemanticsNodes().isNotEmpty()
    }

    // Modify fields
    composeTestRule.onNodeWithTag("FirstNameField").performTextReplacement("Jane")
    composeTestRule.onNodeWithTag("LastNameField").performTextReplacement("Smith")
    composeTestRule.onNodeWithTag("UsernameField").performTextReplacement("janesmith")

    // Save changes
    composeTestRule.onNodeWithTag("SaveButton").performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("DisplayFirstName").fetchSemanticsNodes().isNotEmpty()
    }

    // Verify updated values in display mode
    composeTestRule.onNodeWithTag("DisplayFirstName").assertTextEquals("Jane")
    composeTestRule.onNodeWithTag("DisplayLastName").assertTextEquals("Smith")
    composeTestRule.onNodeWithTag("DisplayUsername").assertTextEquals("@janesmith")
  }

  @Test
  fun testFavoriteParkingsSectionWithHardcodedParkings() {
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("FavoriteParkingsTitle").fetchSemanticsNodes().isNotEmpty()
    }

    // Check favorite parkings section exists
    composeTestRule.onNodeWithTag("FavoriteParkingsTitle").assertExists()

    // Verify the favorite parking items
    composeTestRule.onNodeWithTag("FavoriteParkingList").assertExists()
    composeTestRule
        .onAllNodesWithTag("ParkingItem_")
        .assertAll(hasTextExactly("Parking Central", "Parking Station", "Parking Mall"))
    composeTestRule.onAllNodesWithTag("FavoriteToggle_").assertAll(isToggleable())
  }

  @Test
  fun testProfileImageInteraction() {
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("ProfileImage").fetchSemanticsNodes().isNotEmpty()
    }

    // Verify profile image exists
    composeTestRule.onNodeWithTag("ProfileImage").assertExists()

    // Enter edit mode
    composeTestRule.onNodeWithTag("EditButton").performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("ProfileImage").fetchSemanticsNodes().isNotEmpty()
    }

    // Verify profile image is clickable in edit mode
    composeTestRule.onNodeWithTag("ProfileImage").assertHasClickAction()
  }

  @Test
  fun testRemoveFavoriteParking() {
    // Wait for favorite parkings to be displayed
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("FavoriteToggle_0").fetchSemanticsNodes().isNotEmpty()
    }

    // Click remove favorite button
    composeTestRule.onNodeWithTag("FavoriteToggle_0").performClick()

    // Verify dialog appears
    composeTestRule.onNodeWithText("Remove favorite").assertExists()
    composeTestRule.onNodeWithText("Remove").assertExists()
    composeTestRule.onNodeWithText("Cancel").assertExists()

    // Confirm removal
    composeTestRule.onNodeWithText("Remove").performClick()

    // Verify parking was removed
    composeTestRule.onNodeWithText("Parking Central").assertDoesNotExist()
  }

  @Test
  fun testInputFieldValidation() {
    // Enter edit mode
    composeTestRule.onNodeWithTag("EditButton").performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("FirstNameField").fetchSemanticsNodes().isNotEmpty()
    }

    // Test empty fields
    composeTestRule.onNodeWithTag("FirstNameField").performTextClearance()
    composeTestRule.onNodeWithTag("LastNameField").performTextClearance()
    composeTestRule.onNodeWithTag("UsernameField").performTextClearance()

    // Test maximum length inputs
    val longInput = "a".repeat(50)
    composeTestRule.onNodeWithTag("FirstNameField").performTextInput(longInput)
    composeTestRule.onNodeWithTag("LastNameField").performTextInput(longInput)
    composeTestRule.onNodeWithTag("UsernameField").performTextInput(longInput)

    // Save changes
    composeTestRule.onNodeWithTag("SaveButton").performClick()

    // Verify the values were saved (or validation messages appeared if implemented)
    composeTestRule.onNodeWithTag("DisplayFirstName").assertExists()
  }
}
