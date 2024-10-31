package com.github.se.cyrcle.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.ui.navigation.NavigationActions
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private fun setupProfileScreen() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      ProfileScreen(navigationActions = navigationActions)
    }
  }

  @Test
  fun testInitialDisplayMode() {
    setupProfileScreen()

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
    setupProfileScreen()

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
    setupProfileScreen()

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
    setupProfileScreen()

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
  fun testFavoriteParkingsSection() {
    setupProfileScreen()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("FavoriteParkingsTitle").fetchSemanticsNodes().isNotEmpty()
    }

    // Check favorite parkings section exists
    composeTestRule.onNodeWithTag("FavoriteParkingsTitle").assertExists()

    // If no favorites, verify empty state message
    composeTestRule.onNodeWithTag("NoFavoritesMessage").assertExists()
  }

  @Test
  fun testProfileImageInteraction() {
    setupProfileScreen()

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
}
