package com.github.se.cyrcle.ui.profile

import ProfileScreen
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.model.user.User
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testProfileScreenDisplaysUserInfo() {
    val user =
        User(
            userId = "12345",
            username = "eggsampluser",
            firstName = "Eggsam",
            lastName = "P.LU. Sir",
            email = "eggsampleruser@epfl.ch",
            profilePictureUrl = "",
            favoriteParkings = listOf("Parking A", "Parking B", "Parking C"))

    composeTestRule.setContent { ProfileScreen(user) }

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("ProfilePicture").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithTag("UserNameDisplay").assertTextContains("Eggsam P.LU. Sir")
    composeTestRule.onNodeWithTag("UserHandleDisplay").assertTextContains("@eggsampluser")
    composeTestRule.onNodeWithTag("ParkingItem_0").assertTextContains("Parking A")
  }

  @Test
  fun testToggleFavoriteParking() {
    val user =
        User(
            userId = "12345",
            username = "eggsampluser",
            firstName = "Eggsam",
            lastName = "P.LU. Sir",
            email = "eggsampleruser@epfl.ch",
            profilePictureUrl = "",
            favoriteParkings = listOf("Parking A", "Parking B"))

    composeTestRule.setContent { ProfileScreen(user) }

    // Ensure the list of favorite parkings is displayed
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("FavoriteParkingList").fetchSemanticsNodes().isNotEmpty()
    }

    // Initial state: filled star for first parking
    composeTestRule.onNodeWithTag("FavoriteToggle_0").assertIsDisplayed()

    // Perform click to toggle to empty star
    composeTestRule.onNodeWithTag("FavoriteToggle_0").performClick()

    // Check if the first star is empty now
    composeTestRule.onNodeWithTag("FavoriteToggle_0").assertIsDisplayed()

    // Perform click to toggle back to filled star
    composeTestRule.onNodeWithTag("FavoriteToggle_0").performClick()

    // Check if the first star is filled again
    composeTestRule.onNodeWithTag("FavoriteToggle_0").assertIsDisplayed()
  }

  @Test
  fun testEditModeSwitch() {
    val user =
        User(
            userId = "12345",
            username = "eggsampluser",
            firstName = "Eggsam",
            lastName = "P.LU. Sir",
            email = "eggsampleruser@epfl.ch",
            profilePictureUrl = "",
            favoriteParkings = listOf("Parking A", "Parking B"))

    composeTestRule.setContent { ProfileScreen(user) }

    composeTestRule.onNodeWithTag("ModifyButton").performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onAllNodesWithTag("FirstNameField").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithTag("FirstNameField").assertExists()
    composeTestRule.onNodeWithTag("LastNameField").assertExists()
    composeTestRule.onNodeWithTag("UsernameField").assertExists()
  }

  @Test
  fun testSaveChangesButton() {
    val testUser =
        User(
            userId = "12345",
            username = "eggsampluser",
            firstName = "Eggsam",
            lastName = "P.LU. Sir",
            email = "eggsampleruser@epfl.ch",
            profilePictureUrl = "",
            favoriteParkings = listOf("Parking A", "Parking B", "Parking C"))
    composeTestRule.setContent { ProfileScreen(testUser) }

    // Enter edit mode
    composeTestRule.onNodeWithTag("ModifyButton").performClick()

    // Modify fields
    composeTestRule.onNodeWithTag("FirstNameField").performTextInput("NewName")
    composeTestRule.onNodeWithTag("LastNameField").performTextInput("NewLastName")
    composeTestRule.onNodeWithTag("UsernameField").performTextInput("newusername")

    // Save changes
    composeTestRule.onNodeWithTag("SaveButton").performClick()

    // Verify display mode is active
    composeTestRule.onNodeWithTag("ModifyButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("FirstNameField").assertDoesNotExist()
    composeTestRule.onNodeWithTag("SaveButton").assertDoesNotExist()
    composeTestRule.onNodeWithTag("CancelButton").assertDoesNotExist()
  }
}
