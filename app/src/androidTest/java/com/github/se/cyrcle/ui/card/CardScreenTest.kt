package com.github.se.cyrcle.ui.card

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.ui.card.CardScreen
import com.github.se.cyrcle.ui.card.parking1
import com.github.se.cyrcle.ui.navigation.NavigationActions
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    val mockNavController = TestNavHostController(ApplicationProvider.getApplicationContext())
    val navigationActions = NavigationActions(navController = mockNavController)

    @Test
    fun topAppBarDisplaysCorrectly() {
        composeTestRule.setContent { CardScreen(curParking = parking1, navigationActions) }

        // Verify the top app bar title
        composeTestRule.onNodeWithTag("TopAppBarTitle").assertTextContains("Description of Test_spot_1")
    }

    @Test
    fun imagesDisplayedCorrectly() {
        composeTestRule.setContent { CardScreen(curParking = parking1, navigationActions) }

        // Verify the images in the LazyRow
        composeTestRule.onNodeWithTag("ParkingImagesRow").assertExists()

        composeTestRule
            .onAllNodesWithTag("ParkingImage0") // First image
            .assertCountEquals(1)
    }

    @Test
    fun buttonsAreDisplayed() {
        composeTestRule.setContent { CardScreen(curParking = parking1, navigationActions) }

        // Verify the buttons
        composeTestRule.onNodeWithTag("ShowInMapButton").assertExists()

        composeTestRule.onNodeWithTag("AddReviewButton").assertExists()

        composeTestRule.onNodeWithTag("ReportButton").assertExists()
    }

    @Test
    fun displaysCorrectCapacityAndRackType() {
        composeTestRule.setContent { CardScreen(curParking = parking1, navigationActions) }

        // Check the specific text within the second child of the CapacityColumn (which contains "51-100
        // spots")
        composeTestRule
            .onNodeWithTag("CapacityColumn")
            .onChildAt(1) // Select the second child (index 1) which contains the actual capacity value
            .assertTextContains("51-100 spots")
    }
}