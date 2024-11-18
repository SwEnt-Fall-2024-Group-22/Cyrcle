package com.github.se.cyrcle.ui.profile

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class CreateProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockNavigationActions: NavigationActions

  @Before
  fun setUp() {
    mockNavigationActions = mock(NavigationActions::class.java)

    composeTestRule.setContent { CreateProfileScreen(mockNavigationActions) }
  }

  @Test
  @OptIn(ExperimentalTestApi::class)
  fun testInitialDisplay() {
    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("CreateProfileScreen"))

    // Verify initial display mode elements
    composeTestRule.onNodeWithTag("CreateProfileScreen").assertExists()
  }
}
