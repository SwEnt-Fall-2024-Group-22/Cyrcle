package com.github.se.cyrcle.ui.list

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.model.parking.*
import com.github.se.cyrcle.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class ListTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var navHostController: NavHostController
  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    // Set up the test environment for the Compose UI test
    navHostController = mock(NavHostController::class.java)
    navigationActions = NavigationActions(navHostController)
  }

  @Test
  fun testSpotCardIsDisplayed() {
    composeTestRule.setContent {
      SpotCard(navigationActions, TestInstancesParking.parking1, 0.0, emptyList())
    }

    composeTestRule.onNodeWithTag("SpotCard_Unnamed", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("ParkingName", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals("Unnamed Parking")
    composeTestRule
        .onNodeWithTag("ParkingDistance", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals("0.00 km")
    composeTestRule
        .onNodeWithTag("ParkingPrice", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals("0.0 $")
  }

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
        .assertIsDisplayed()
        .assertTextEquals("Show Filters")
  }

  @Test
  @OptIn(ExperimentalTestApi::class)
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
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("ShowFiltersButton"))

    // Assert: Check that the button text changed
    composeTestRule.onNodeWithTag("ShowFiltersButton").assertTextEquals("Hide Filters")

    // Act: Click to hide filters
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("ShowFiltersButton"))

    // Assert: Check that the button text reverted
    composeTestRule.onNodeWithTag("ShowFiltersButton").assertTextEquals("Show Filters")
  }

  @Test
  @OptIn(ExperimentalTestApi::class)
  fun testProtectionFilters() {

    val selectedProtection = mutableSetOf<ParkingProtection>()
    composeTestRule.setContent {
      FilterHeader(
          selectedProtection = selectedProtection,
          onProtectionSelected = { selectedProtection.add(it) },
          selectedRackTypes = emptySet(),
          onRackTypeSelected = {},
          selectedCapacities = emptySet(),
          onCapacitySelected = {})
    }

    // Act: Click to show filters
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("Protection"))

    composeTestRule
        .onNodeWithTag("Protection")
        .assertIsDisplayed()
        .assertTextEquals("Protection")
        .performClick()

    composeTestRule.waitUntilExactlyOneExists(hasTestTag("ProtectionFilter"))
    composeTestRule.onNodeWithTag("ProtectionFilter").assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag("ProtectionFilterItem")
        .assertCountEquals(ParkingProtection.entries.size)
        .assertAll(hasClickAction())
    composeTestRule
        .onAllNodesWithTag("ProtectionFilterItem")[0]
        .performClick()
    assert(selectedProtection.contains(ParkingProtection.entries[0]))
  }

  @Test
  @OptIn(ExperimentalTestApi::class)
  fun testRackTypeFilters() {
    // Arrange
    val selectedRackType = mutableSetOf<ParkingRackType>()
    composeTestRule.setContent {
      FilterHeader(
          selectedProtection = emptySet(),
          onProtectionSelected = {},
          selectedRackTypes = selectedRackType,
          onRackTypeSelected = { selectedRackType.add(it) },
          selectedCapacities = emptySet(),
          onCapacitySelected = {})
    }

    // Act: Click to show filters
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("Rack Type"))

    composeTestRule
        .onNodeWithTag("Rack Type")
        .assertIsDisplayed()
        .assertTextEquals("Rack Type")
        .performClick()

    composeTestRule.waitUntilExactlyOneExists(hasTestTag("RackTypeFilter"))
    composeTestRule.onNodeWithTag("RackTypeFilter").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("RackTypeFilterItem").assertAll(hasClickAction())
    composeTestRule
          .onAllNodesWithTag("RackTypeFilterItem")[0]
          .performClick()
    assert(selectedRackType.contains(ParkingRackType.entries[0]))
  }

  @Test
  @OptIn(ExperimentalTestApi::class)
  fun testCapacityFilters() {
    // Arrange
    val selectedCapacities = mutableSetOf(ParkingCapacity.XSMALL)
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
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("Capacity"))

    composeTestRule
        .onNodeWithTag("Capacity")
        .assertIsDisplayed()
        .assertTextEquals("Capacity")
        .performClick()

    composeTestRule.waitUntilExactlyOneExists(hasTestTag("CapacityFilter"))
    composeTestRule.onNodeWithTag("CapacityFilter").assertIsDisplayed()
    composeTestRule.onAllNodesWithTag("CapacityFilterItem").assertAll(hasClickAction())
    composeTestRule
          .onAllNodesWithTag("CapacityFilterItem")[0]
          .performClick()
    assert(selectedCapacities.contains(ParkingCapacity.entries[0]))
  }
}
