package com.github.se.cyrcle.ui.map

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class MapScreenTest : TestCase() {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockNavigation: NavigationActions

  @Before
  fun setUp() {
    mockNavigation = mock(NavigationActions::class.java)
    `when`(mockNavigation.currentRoute()).thenReturn(Screen.MAP)
  }

  @Test
  fun testMapIsDisplayed() {
    composeTestRule.setContent { MapScreen(mockNavigation) }

    composeTestRule.onNodeWithTag("MapScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("BottomNavigationBar").assertIsDisplayed()

    // Assert that the zoom controls are displayed
    composeTestRule.onNodeWithTag("ZoomControls").assertIsDisplayed()
    // Assert that the add button is displayed
    composeTestRule.onNodeWithTag("AddButton").assertIsDisplayed()
  }
}
