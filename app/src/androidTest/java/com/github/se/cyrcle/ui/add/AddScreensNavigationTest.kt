package com.github.se.cyrcle.ui.add

import CyrcleNavHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.map.MapScreen
import com.github.se.cyrcle.ui.navigation.NavigationActions
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddScreensNavigationTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Composable
  fun setUp(): Pair<NavigationActions, ParkingViewModel> {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)
    val parkingViewModel: ParkingViewModel = viewModel(factory = ParkingViewModel.Factory)
    CyrcleNavHost(navigationActions, navController, parkingViewModel)
    return Pair(navigationActions, parkingViewModel)
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun testAddButtonNavigatesToLocationPicker() {

    composeTestRule.setContent {
      val (navigationActions, parkingViewModel) = setUp()
      MapScreen(navigationActions, parkingViewModel)
    }
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("addButton"))
    // Perform click on the add button
    composeTestRule.onNodeWithTag("addButton").performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("LocationPickerScreen"))
    // Wait until the Location Picker screen is displayed
    composeTestRule.onNodeWithText("Where is the Parking ?").assertExists().isDisplayed()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun testNavigationToAttribute() {

    composeTestRule.setContent {
      val (navigationActions, parkingViewModel) = setUp()
      LocationPicker(navigationActions, parkingViewModel)
    }
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("nextButton"))
    // Perform click on the add button
    composeTestRule.onNodeWithTag("nextButton").performClick()

    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("AttributesPickerScreen"))
    composeTestRule.onNodeWithTag("AttributesPickerScreen").assertExists().assertIsDisplayed()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun testSubmit() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)
      val parkingViewModel: ParkingViewModel = viewModel(factory = ParkingViewModel.Factory)
      CyrcleNavHost(navigationActions, navController, parkingViewModel)
      AttributesPicker(navigationActions, parkingViewModel)
    }
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("submitButton"))
    // Perform click on the add button
    composeTestRule.onNodeWithTag("submitButton").performClick()

    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("MapScreen"))
    composeTestRule.onNodeWithTag("MapScreen").assertExists().assertIsDisplayed()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun testCancel() {
    composeTestRule.setContent {
      val (navigationActions, parkingViewModel) = setUp()
      AttributesPicker(navigationActions, parkingViewModel)
    }
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("cancelButton"))
    // Perform click on the add button
    composeTestRule.onNodeWithTag("cancelButton").performClick()

    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("MapScreen"))
    composeTestRule.onNodeWithTag("MapScreen").assertExists().assertIsDisplayed()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun testCancel2() {
    composeTestRule.setContent {
      val (navigationActions, parkingViewModel) = setUp()
      LocationPicker(navigationActions, parkingViewModel)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("cancelButton").performClick()
    composeTestRule.waitUntilExactlyOneExists((hasTestTag("MapScreen")))
    composeTestRule.onNodeWithTag("MapScreen").assertExists().assertIsDisplayed()
  }
}
