package com.github.se.cyrcle.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.github.se.cyrcle.ui.theme.atoms.Button
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditProfileComponentTest {

  @Composable
  private fun SaveButton() {
    Button(text = "Save", onClick = {}, testTag = "SaveButton")
  }

  @Composable
  private fun CancelButton() {
    Button(text = "Cancel", onClick = {}, testTag = "CancelButton")
  }

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testNullEditProfileComponent() {
    composeTestRule.setContent { EditProfileComponent(null, { SaveButton() }, { CancelButton() }) }

    composeTestRule.onNodeWithTag("NullUserText").assertIsDisplayed()

    composeTestRule.onNodeWithTag("ProfileImage").assertDoesNotExist()
    composeTestRule.onNodeWithTag("FirstNameField").assertDoesNotExist()
    composeTestRule.onNodeWithTag("LastNameField").assertDoesNotExist()
    composeTestRule.onNodeWithTag("UsernameField").assertDoesNotExist()
    composeTestRule.onNodeWithTag("SaveButton").assertDoesNotExist()
    composeTestRule.onNodeWithTag("CancelButton").assertDoesNotExist()
  }

  @Test
  fun testEditProfileComponentHorizontalButtons() {
    val user = TestInstancesUser.user1

    composeTestRule.setContent { EditProfileComponent(user, { SaveButton() }, { CancelButton() }) }

    composeTestRule.onNodeWithTag("NullUserText").assertDoesNotExist()

    composeTestRule.onNodeWithTag("ProfileImage").assertIsDisplayed().assertHasClickAction()

    composeTestRule
        .onNodeWithTag("FirstNameField")
        .assertIsDisplayed()
        .assertTextContains(user.details?.firstName ?: "")

    composeTestRule
        .onNodeWithTag("LastNameField")
        .assertIsDisplayed()
        .assertTextContains(user.details?.lastName ?: "")

    composeTestRule
        .onNodeWithTag("UsernameField")
        .assertIsDisplayed()
        .assertTextContains(user.public.username)

    composeTestRule.onNodeWithTag("EditComponentButtonRow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SaveButton", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("CancelButton", useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun testEditProfileComponentVerticalButtons() {
    val user = TestInstancesUser.user1

    composeTestRule.setContent {
      EditProfileComponent(user, { SaveButton() }, { CancelButton() }, verticalButtonDisplay = true)
    }

    composeTestRule.onNodeWithTag("NullUserText").assertDoesNotExist()

    composeTestRule.onNodeWithTag("ProfileImage").assertIsDisplayed().assertHasClickAction()

    composeTestRule
        .onNodeWithTag("FirstNameField")
        .assertIsDisplayed()
        .assertTextContains(user.details?.firstName ?: "")

    composeTestRule
        .onNodeWithTag("LastNameField")
        .assertIsDisplayed()
        .assertTextContains(user.details?.lastName ?: "")

    composeTestRule
        .onNodeWithTag("UsernameField")
        .assertIsDisplayed()
        .assertTextContains(user.public.username)

    composeTestRule.onNodeWithTag("EditComponentButtonRow").assertDoesNotExist()
    composeTestRule.onNodeWithTag("SaveButton", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("CancelButton", useUnmergedTree = true).assertIsDisplayed()
  }
}
