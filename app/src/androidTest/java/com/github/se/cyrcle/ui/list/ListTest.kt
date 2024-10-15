package com.github.se.cyrcle.ui.list

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.model.parking.*
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.mapbox.turf.TurfMeasurement
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

    composeTestRule.onNodeWithTag("SpotListItem", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("ParkingName", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals(TestInstancesParking.parking1.optName ?: "Unnamed Parking")
    composeTestRule
        .onNodeWithTag("ParkingDistance", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals(String.format("%.2f km", 0.0))
    composeTestRule
        .onNodeWithTag("ParkingPrice", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals("${TestInstancesParking.parking1.price} $")
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

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun testCardScreenListsParkings() {
    composeTestRule.setContent {
      SpotListScreen(navigationActions, listOf(TestInstancesParking.parking1))
    }

    // Check that the list is displayed
    composeTestRule.onNodeWithTag("SpotListColumn").assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag("SpotListItem")
        .assertCountEquals(1)
        .assertAll(hasClickAction())

    // Check that the parking name is displayed
    composeTestRule
        .onNodeWithTag("ParkingName", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals(TestInstancesParking.parking1.optName ?: "Unnamed Parking")

    // Check that the parking distance is displayed
    composeTestRule
        .onNodeWithTag("ParkingDistance", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals(
            String.format(
                "%.2f km",
                TurfMeasurement.distance(
                    referencePoint1, TestInstancesParking.parking1.location.center)))

    // Check that the parking price is displayed
    composeTestRule
        .onNodeWithTag("ParkingPrice", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals("${TestInstancesParking.parking1.price} $")

    // Select all 3 criteria
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()

    // Select Protection
    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("Protection"))
    composeTestRule.onNodeWithTag("Protection", useUnmergedTree = true).performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("ProtectionFilter"))
    composeTestRule
        .onAllNodesWithTag("ProtectionFilterItem")[TestInstancesParking.parking1.protection.ordinal]
        .performClick()
    composeTestRule.onNodeWithTag("Protection").performClick()

    // Select Rack Type
    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("Rack Type"))
    composeTestRule.onNodeWithTag("Rack Type", useUnmergedTree = true).performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("RackTypeFilter"))
    composeTestRule
        .onAllNodesWithTag("RackTypeFilterItem")[TestInstancesParking.parking1.rackType.ordinal]
        .performClick()
    composeTestRule.onNodeWithTag("Rack Type").performClick()

    // Select Capacity
    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("Capacity"))
    composeTestRule.onNodeWithTag("Capacity", useUnmergedTree = true).performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("CapacityFilter"))
    composeTestRule
        .onAllNodesWithTag("CapacityFilterItem")[TestInstancesParking.parking1.capacity.ordinal]
        .performClick()

    // Store filters away
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SpotListItem"))
    composeTestRule.onAllNodesWithTag("SpotListItem").assertCountEquals(1)

    // Check that the parking protection match is displayed
    composeTestRule
        .onAllNodesWithTag("MatchedCriterionItem", useUnmergedTree = true)
        .assertCountEquals(3)
        .assertAny(hasText("Protection: ${TestInstancesParking.parking1.protection}"))
        .assertAny(hasText("Rack Type: ${TestInstancesParking.parking1.rackType}"))
        .assertAny(hasText("Capacity: ${TestInstancesParking.parking1.capacity}"))
  }
}
