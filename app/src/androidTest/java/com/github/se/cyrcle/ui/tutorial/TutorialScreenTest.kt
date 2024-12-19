package com.github.se.cyrcle.ui.tutorial

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class TutorialScreenTest {

  private lateinit var navigationActions: NavigationActions

  @get:Rule
  val composeTestRule = createComposeRule()

  @Before
  fun setup() {
    navigationActions = mock(NavigationActions::class.java)
  }

  @Test
  fun tutorialScreenBaseDisplayed() {
    composeTestRule.setContent { TutorialScreen(navigationActions) }

    composeTestRule.onNodeWithTag("TutorialScreen", true).assertIsDisplayed()

    composeTestRule.onNodeWithTag("TutorialScreenTopPart", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("TutorialScreenBottomPart", true).assertIsDisplayed()

    composeTestRule.onNodeWithTag("TutorialScreenNextButton", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("TutorialScreenNextButtonText", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("TutorialScreenSkipButton", true).assertIsDisplayed()
  }

  @Test
  fun tutorialScreenTestBehavior() {
    composeTestRule.setContent { TutorialScreen(navigationActions) }

    fun nextPage() {
      composeTestRule
          .onNodeWithTag("TutorialScreenNextButton", true)
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
    }

    // Welcome Screen
    composeTestRule.onNodeWithTag("TutorialScreenWelcomeTitle", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("TutorialScreenWelcomeSubtitle", true).assertIsDisplayed()
    nextPage()

    // Images
    composeTestRule.onNodeWithTag("TutorialScreenImageMap", true).assertIsDisplayed()
    nextPage()
    composeTestRule.onNodeWithTag("TutorialScreenImageList", true).assertIsDisplayed()
    nextPage()

    // Thank you Screen
    composeTestRule.onNodeWithTag("TutorialScreenThankYouTitle", true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("TutorialScreenThankYouSubtitle", true).assertIsDisplayed()
    nextPage()

    verify(navigationActions).navigateTo(Route.MAP)
  }
}
