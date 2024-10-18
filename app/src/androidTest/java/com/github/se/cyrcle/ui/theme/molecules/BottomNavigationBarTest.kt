package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class NavigationActionsTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navigationDestination: NavDestination
  private lateinit var navHostController: NavHostController
  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    navigationDestination = mock(NavDestination::class.java)
    navHostController = mock(NavHostController::class.java)
    navigationActions = NavigationActions(navHostController)
  }

  @Test
  fun displayBottomNavigationBar() {
    val defaultTag = "BottomNavigationBar"
    composeTestRule.setContent {
      Scaffold(bottomBar = { BottomNavigationBar(navigationActions, selectedItem = Route.AUTH) }) {
          innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {}
      }
    }

    composeTestRule.onNodeWithTag(defaultTag).assertIsDisplayed()

    for (topLevelDestination in LIST_TOP_LEVEL_DESTINATION) {
      composeTestRule.onNodeWithTag(topLevelDestination.textId).assertIsDisplayed()
      composeTestRule.onNodeWithTag("${topLevelDestination.textId}Text", true).assertIsDisplayed()
      composeTestRule.onNodeWithTag("${topLevelDestination.textId}Icon", true).assertIsDisplayed()
    }
  }
}
