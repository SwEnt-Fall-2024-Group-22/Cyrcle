package com.github.se.cyrcle.ui.zone

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

class ZoneManagerScreenTest {
  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun checkAllUIElementsAreDisplayed() {
    composeTestRule.setContent { ZoneManagerScreen(navigationActions) }

    composeTestRule.waitUntilExactlyOneExists(hasTestTag("TopAppBar"))
    composeTestRule.onNodeWithTag("TopAppBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AddButton").assertIsDisplayed().performClick()
    verify(navigationActions).navigateTo(Screen.ZONE_SELECTION)
  }
}
