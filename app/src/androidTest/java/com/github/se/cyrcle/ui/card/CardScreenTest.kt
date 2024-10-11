package com.github.se.cyrcle.ui.card

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class CardScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navHostController: NavHostController
  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    navHostController = mock(NavHostController::class.java)
    navigationActions = NavigationActions(navHostController)
  }

  @Test
  fun topAppBarDisplaysCorrectly() {
    composeTestRule.setContent { CardScreen(navigationActions, curParking = parking1) }

    // Verify the top app bar title
    composeTestRule.onNodeWithTag("TopAppBarTitle").assertTextContains("Description of Test_spot_1")
  }

  @Test
  fun imagesDisplayedCorrectly() {
    composeTestRule.setContent { CardScreen(navigationActions, curParking = parking1) }

    // Verify the images in the LazyRow
    composeTestRule.onNodeWithTag("ParkingImagesRow").assertIsDisplayed()

    composeTestRule
        .onAllNodesWithTag("ParkingImage0") // First image
        .assertCountEquals(1)
  }

  @Test
  fun buttonsAreDisplayed() {
    composeTestRule.setContent { CardScreen(navigationActions, curParking = parking1) }

    // Verify the buttons
    composeTestRule.onNodeWithTag("ShowInMapButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("AddReviewButton").assertIsDisplayed()

    composeTestRule.onNodeWithTag("ReportButton").assertIsDisplayed()
  }

  @Test
  fun displaysCorrectCapacityAndRackType() {
    composeTestRule.setContent { CardScreen(navigationActions, curParking = parking1) }

    // Check the specific text within the second child of the CapacityColumn (which contains "51-100
    // spots")
    composeTestRule
        .onNodeWithTag("CapacityColumn")
        .onChildAt(1) // Select the second child (index 1) which contains the actual capacity value
        .assertTextContains("51-100 spots")
  }
}
