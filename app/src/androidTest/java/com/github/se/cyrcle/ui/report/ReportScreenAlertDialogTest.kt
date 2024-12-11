package com.github.se.cyrcle.ui.report

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportScreenAlertDialogTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun dialogDisplaysCorrectContent() {
    composeTestRule.setContent { ReportScreenAlertDialog(onDismiss = {}, onAccept = {}) }

    // Check that the dialog is displayed
    composeTestRule.onNodeWithTag("ReportScreenAlertDialog").assertIsDisplayed()

    // Check the title
    composeTestRule.onNodeWithTag("AlertDialogTitle").assertTextEquals("Are You Sure?")

    // Check the content text
    composeTestRule
        .onNodeWithTag("AlertDialogContent")
        .assertTextContains(
            "We take reports very seriously. Please make sure that the information you have mentioned is accurate. Do you want to add this Report?")

    // Check for "Yes" and "No" buttons
    composeTestRule.onNodeWithTag("CancelButton").assertIsDisplayed().assertTextEquals("No")

    composeTestRule.onNodeWithTag("AcceptButton").assertIsDisplayed().assertTextEquals("Yes")
  }

  @Test
  fun dismissButtonWorksCorrectly() {
    var isDismissed = false
    composeTestRule.setContent {
      ReportScreenAlertDialog(onDismiss = { isDismissed = true }, onAccept = {})
    }

    // Click on the "No" button
    composeTestRule.onNodeWithTag("CancelButton").performClick()

    // Verify the dismissal action
    assert(isDismissed)
  }

  @Test
  fun acceptButtonWorksCorrectly() {
    var isAccepted = false
    composeTestRule.setContent {
      ReportScreenAlertDialog(onDismiss = {}, onAccept = { isAccepted = true })
    }

    // Click on the "Yes" button
    composeTestRule.onNodeWithTag("AcceptButton").performClick()

    // Verify the acceptance action
    assert(isAccepted)
  }

  @Test
  fun buttonsHaveCorrectArrangement() {
    composeTestRule.setContent { ReportScreenAlertDialog(onDismiss = {}, onAccept = {}) }

    // Check that buttons are horizontally aligned
    composeTestRule
        .onNodeWithTag("AlertDialogButtons")
        .assert(hasAnyChild(hasTestTag("CancelButton")))
        .assert(hasAnyChild(hasTestTag("AcceptButton")))
  }
}
