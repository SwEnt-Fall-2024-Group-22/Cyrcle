package com.github.se.cyrcle.ui.addParking

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.ui.addParking.attributes.RackTypeHelpScreen
import com.github.se.cyrcle.ui.addParking.attributes.RackTypeItem
import com.github.se.cyrcle.ui.navigation.NavigationActions
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class RackInfoTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testRackTypeHelpScreen() {
    val mockNavigationActions = mock(NavigationActions::class.java)

    composeTestRule.setContent { RackTypeHelpScreen(mockNavigationActions) }

    // Check if the top app bar is displayed
    composeTestRule.onNodeWithText("Rack Types").assertIsDisplayed()

    // Check if all rack types are displayed (excluding OTHER)
    ParkingRackType.entries.take(2).forEachIndexed { _, rackType ->
      composeTestRule
          .onNodeWithTag("rackTypeItem_${rackType.name}")
          .assertExists()
          .performScrollTo()
          .assertIsDisplayed()

      // Check if the rack type description is displayed
      composeTestRule.onNodeWithText(rackType.description).assertIsDisplayed()

      // Check if the image is displayed
      composeTestRule
          .onNodeWithContentDescription("${rackType.description} image")
          .assertIsDisplayed()

      // Check if the rack description text is displayed
      composeTestRule.onNodeWithTag("rackDescription_${rackType.name}").assertIsDisplayed()

      composeTestRule.onNodeWithTag("rackImage_${rackType.name}").assertIsDisplayed()
    }

    // Check if the list is scrollable
    composeTestRule
        .onNodeWithTag("rackTypeList")
        .performScrollToIndex(ParkingRackType.entries.size - 2)

    // Check if the last item (excluding OTHER) is displayed after scrolling
    composeTestRule.onNodeWithTag("rackTypeItem_${ParkingRackType.GRID.name}").assertIsDisplayed()
  }

  @Test
  fun testRackTypeItemLeftAligned() {
    composeTestRule.setContent { RackTypeItem(ParkingRackType.U_RACK, isImageOnRight = false) }

    composeTestRule.onNodeWithTag("rackTypeItem_U_RACK").assertIsDisplayed()
    composeTestRule.onNodeWithText(ParkingRackType.U_RACK.description).assertIsDisplayed()
    composeTestRule.onNodeWithTag("rackImage_U_RACK").assertIsDisplayed()
    composeTestRule.onNodeWithTag("rackDescription_U_RACK").assertIsDisplayed()
  }

  @Test
  fun testRackTypeItemRightAligned() {
    composeTestRule.setContent { RackTypeItem(ParkingRackType.VERTICAL, isImageOnRight = true) }

    composeTestRule.onNodeWithTag("rackTypeItem_VERTICAL").assertIsDisplayed()
    composeTestRule.onNodeWithText(ParkingRackType.VERTICAL.description).assertIsDisplayed()
    composeTestRule.onNodeWithTag("rackImage_VERTICAL").assertIsDisplayed()
    composeTestRule.onNodeWithTag("rackDescription_VERTICAL").assertIsDisplayed()
  }
}
