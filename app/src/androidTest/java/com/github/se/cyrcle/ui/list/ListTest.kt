package com.github.se.cyrcle.ui.list

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.cyrcle.model.parking.*
import org.junit.Rule
import org.junit.Test

class FilterHeaderTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testShowFiltersButtonInitiallyDisplaysShowFilters() {
    // Arrange
    composeTestRule.setContent {
      FilterHeader(
          selectedProtection = emptySet(),
          onProtectionSelected = {},
          selectedRackTypes = emptySet(),
          onRackTypeSelected = {},
          selectedCapacities = emptySet(),
          onCapacitySelected = {})
    }

    // Act & Assert
    composeTestRule
        .onNodeWithTag("ShowFiltersButton")
        .assertExists()
        .assertTextEquals("Show Filters")
  }

  @Test
  fun testShowFiltersButtonTogglesFilterSection() {
    // Arrange
    composeTestRule.setContent {
      FilterHeader(
          selectedProtection = emptySet(),
          onProtectionSelected = {},
          selectedRackTypes = emptySet(),
          onRackTypeSelected = {},
          selectedCapacities = emptySet(),
          onCapacitySelected = {})
    }

    // Act: Click to show filters
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()

    Thread.sleep(1000)

    // Assert: Check that the button text changed
    composeTestRule.onNodeWithTag("ShowFiltersButton").assertTextEquals("Hide Filters")

    Thread.sleep(1000)

    // Act: Click to hide filters
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()

    Thread.sleep(1000)

    // Assert: Check that the button text reverted
    composeTestRule.onNodeWithTag("ShowFiltersButton").assertTextEquals("Show Filters")
  }

  @Test
  fun testCapacityFilterButtonActuallyClicks() {
    // Arrange
    val selectedCapacities = mutableSetOf<ParkingCapacity>(ParkingCapacity.XSMALL)
    composeTestRule.setContent {
      FilterHeader(
          selectedProtection = emptySet(),
          onProtectionSelected = {},
          selectedRackTypes = emptySet(),
          onRackTypeSelected = {},
          selectedCapacities = selectedCapacities,
          onCapacitySelected = { selectedCapacities.add(it) })
    }

    // Act: Click to show filters
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
  }
}
