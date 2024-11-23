package com.github.se.cyrcle.ui.parkingDetails

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParkingDetailsScreenAlertDialogTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testAssertAcceptAndCancelCloseDialog() {
    var dismissedCalled = false
    var acceptCalled = false
    composeTestRule.setContent {
      ParkingDetailsAlertDialog(
          onDismiss = { dismissedCalled = true },
          onAccept = { acceptCalled = true },
          newParkingImageLocalPath = "imagePath")
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("parkingDetailsAlertDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("acceptButton").performClick()
    composeTestRule.waitForIdle()
    assert(acceptCalled)
    assert(!dismissedCalled)
    composeTestRule.onNodeWithTag("cancelButton").performClick()
    assert(dismissedCalled)
  }
}
