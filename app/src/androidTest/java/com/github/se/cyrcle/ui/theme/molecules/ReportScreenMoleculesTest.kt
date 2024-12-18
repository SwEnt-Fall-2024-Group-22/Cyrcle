package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.model.parking.ParkingReportReason
import com.github.se.cyrcle.model.report.ReportedObjectType
import com.github.se.cyrcle.ui.navigation.NavigationActions
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class ReportScreenMoleculesTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun reportTopAppBar_displaysTitleCorrectly() {
    composeTestRule.setContent {
      TopAppBar(navigationActions = mock(NavigationActions::class.java), title = "Test Title")
    }

    composeTestRule.onNodeWithText("Test Title").assertExists().assertIsDisplayed()
  }

  @Test
  fun reportTextBlock_displaysTitleAndBulletPoints() {
    val title = "Test Title"
    val bulletPoints = listOf("Point 1", "Point 2", "Point 3")

    composeTestRule.setContent { ReportTextBlock(title = title, bulletPoints = bulletPoints) }

    // Log the semantics tree for debugging
    composeTestRule.onRoot().printToLog("ReportTextBlockTest")

    // Assert the title is displayed
    composeTestRule.onNodeWithText(title).assertExists().assertIsDisplayed()

    // Assert each bullet point's text is displayed
    bulletPoints.forEachIndexed { index, point ->
      // Find the specific bullet point node and then verify its text content
      composeTestRule
          .onNodeWithTag("BulletPoint$index") // Find the parent bullet point node
          .onChildAt(1) // Target the second child (the one with the actual bullet point text)
          .assertTextContains(point)
    }
  }

  @Test
  fun reportInputs_displaysCorrectFieldsForReportedObjectType() {
    val parkingReason = mutableStateOf(ParkingReportReason.INEXISTANT)
    val reportDescription = mutableStateOf("Test description")
    val padding = 16.dp

    composeTestRule.setContent {
      ReportInputs(
          selectedReasonIfParking = parkingReason,
          selectedReasonIfReview = null,
          selectedReasonIfImage = null,
          reportedObjectType = ReportedObjectType.PARKING,
          reportDescription = reportDescription,
          horizontalPadding = padding)
    }

    composeTestRule.onNodeWithTag("ReasonDropdown").assertExists().assertIsDisplayed()

    composeTestRule.onNodeWithTag("DetailsInput").assertExists().assertIsDisplayed()
  }

  @Test
  fun submitButtonWithDialog_showsDialogOnClickAndHandlesSubmit() {
    val showDialog = mutableStateOf(false)
    var wasSubmitted = false

    composeTestRule.setContent {
      SubmitButtonWithDialog(
          showDialog = showDialog, validInputs = true, onSubmit = { wasSubmitted = true })
    }

    // Log the semantics tree to verify node structure
    composeTestRule.onRoot().printToLog("SubmitButtonTest")

    // Use the exact text with brackets for the query
    composeTestRule.onNodeWithText("Submit").assertExists().assertIsDisplayed()

    // Perform click and wait for the UI to update
    composeTestRule.onNodeWithText("Submit").performClick()

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithText("Yes") // Ensure dialog has a test tag
        .performClick()

    // Simulate dialog acceptance
    composeTestRule.runOnUiThread { showDialog.value = false }

    assert(wasSubmitted)
  }
}
