package com.github.se.cyrcle.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileImageComponentTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testImageEditable() {
    composeTestRule.setContent {
      ProfileImageComponent(url = "https://example.com", onClick = {}, isEditable = true)
    }

    composeTestRule
        .onNodeWithTag("EditProfileImageIcon", useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag("ProfileImage", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("EmptyProfileImage", useUnmergedTree = true).assertDoesNotExist()
  }

  @Test
  fun testEmptyImageEditable() {
    composeTestRule.setContent { ProfileImageComponent(url = "", onClick = {}, isEditable = true) }

    composeTestRule
        .onNodeWithTag("EditProfileImageIcon", useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag("RealProfileImage", useUnmergedTree = true).assertDoesNotExist()
    composeTestRule.onNodeWithTag("EmptyProfileImage", useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun testEmptyImage() {
    composeTestRule.setContent { ProfileImageComponent(url = "", onClick = {}, isEditable = false) }

    composeTestRule
        .onNodeWithTag("EditProfileImageIcon", useUnmergedTree = true)
        .assertDoesNotExist()
    composeTestRule.onNodeWithTag("RealProfileImage", useUnmergedTree = true).assertDoesNotExist()
    composeTestRule.onNodeWithTag("EmptyProfileImage", useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun testImage() {
    composeTestRule.setContent {
      ProfileImageComponent(url = "https://example.com", onClick = {}, isEditable = false)
    }

    composeTestRule
        .onNodeWithTag("EditProfileImageIcon", useUnmergedTree = true)
        .assertDoesNotExist()
    composeTestRule.onNodeWithTag("RealProfileImage", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("EmptyProfileImage", useUnmergedTree = true).assertDoesNotExist()
  }
}
