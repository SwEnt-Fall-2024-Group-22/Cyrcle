package com.github.se.cyrcle.ui.list

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.model.parking.ImageRepository
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.parking.Tile
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfMeasurement
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class ListScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Mock private lateinit var mockParkingRepository: ParkingRepository
  @Mock private lateinit var mockImageRepository: ImageRepository
  @Mock private lateinit var mockNavigationActions: NavigationActions
  private lateinit var parkingViewModel: ParkingViewModel

  @Before
  fun setUp() {
    // Set up the test environment for the Compose UI test
    MockitoAnnotations.openMocks(this)
    parkingViewModel = ParkingViewModel(mockImageRepository, mockParkingRepository)

    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.LIST)
  }

  @Test
  fun testSpotCardIsDisplayed() {
    composeTestRule.setContent {
      SpotCard(mockNavigationActions, parkingViewModel, TestInstancesParking.parking1, 0.0)
    }

    composeTestRule.onNodeWithTag("SpotListItem", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("ParkingName", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals(TestInstancesParking.parking1.optName ?: "Unnamed Parking")
    composeTestRule
        .onNodeWithTag("ParkingDistance", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals(String.format("%.0f m", 0.0))
    composeTestRule.onNodeWithTag("ParkingNoReviews", useUnmergedTree = true).assertIsNotDisplayed()
  }

  @Test
  fun testSpotCardIsClickable() {
    composeTestRule.setContent {
      SpotCard(mockNavigationActions, parkingViewModel, TestInstancesParking.parking2, 0.0)
    }

    composeTestRule
        .onNodeWithTag("SpotListItem", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()

    composeTestRule.onNodeWithTag("ParkingNoReviews", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("ParkingNbReviews", useUnmergedTree = true).assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("ParkingRating", useUnmergedTree = true).assertIsNotDisplayed()

    verify(mockNavigationActions).navigateTo(Screen.CARD)
    assertEquals(TestInstancesParking.parking2, parkingViewModel.selectedParking.value)
  }

  @Test
  fun testShowFiltersButtonInitiallyDisplaysShowFilters() {
    // Arrange
    composeTestRule.setContent {
      FilterHeader(
          selectedProtection = emptySet(),
          selectedRackTypes = emptySet(),
          selectedCapacities = emptySet(),
          onAttributeSelected = {},
          onlyWithCCTV = false,
          onCCTVCheckedChange = {})
    }

    // Act & Assert
    composeTestRule.onNodeWithTag("ShowFiltersButton").assertIsDisplayed().assertHasClickAction()
  }

  @Test
  @OptIn(ExperimentalTestApi::class)
  fun testShowFiltersButtonTogglesFilterSection() {
    // Arrange
    composeTestRule.setContent {
      FilterHeader(
          selectedProtection = emptySet(),
          selectedRackTypes = emptySet(),
          selectedCapacities = emptySet(),
          onAttributeSelected = {},
          onlyWithCCTV = false,
          onCCTVCheckedChange = {})
    }

    // Act: Click to show filters
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("ShowFiltersButton"))

    // Act: Click to hide filters
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("ShowFiltersButton"))
  }

  @Test
  @OptIn(ExperimentalTestApi::class)
  fun testProtectionFilters() {

    val selectedProtection = mutableSetOf<ParkingProtection>()
    composeTestRule.setContent {
      FilterHeader(
          selectedProtection = selectedProtection,
          selectedRackTypes = emptySet(),
          selectedCapacities = emptySet(),
          onAttributeSelected = {
            when (it) {
              is ParkingProtection -> selectedProtection.add(it)
            }
          },
          onlyWithCCTV = false,
          onCCTVCheckedChange = {})
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
    composeTestRule.onAllNodesWithTag("ProtectionFilterItem").onFirst().performClick()
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
          selectedRackTypes = selectedRackType,
          selectedCapacities = emptySet(),
          onAttributeSelected = {
            when (it) {
              is ParkingRackType -> selectedRackType.add(it)
            }
          },
          onlyWithCCTV = false,
          onCCTVCheckedChange = {})
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
    composeTestRule.onAllNodesWithTag("RackTypeFilterItem").onFirst().performClick()
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
          selectedRackTypes = emptySet(),
          selectedCapacities = selectedCapacities,
          onAttributeSelected = {
            when (it) {
              is ParkingCapacity -> selectedCapacities.add(it)
            }
          },
          onlyWithCCTV = false,
          onCCTVCheckedChange = {})
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
    composeTestRule.onAllNodesWithTag("CapacityFilterItem").onFirst().performClick()
    assert(selectedCapacities.contains(ParkingCapacity.entries[0]))
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun testParkingDetailsScreenListsParkings() {
    val testParking = TestInstancesParking.parking1

    // Prepare to populate the parking list
    `when`(mockParkingRepository.getParkingsBetween(any(), any(), any(), any())).then {
      println(it.getArgument<Point>(0).toString())
      it.getArgument<(List<Parking>) -> Unit>(2)(emptyList())
    }
    `when`(
            mockParkingRepository.getParkingsBetween(
                eq(Tile.getTileFromPoint(TestInstancesParking.referencePoint).bottomLeft),
                any(),
                any(),
                any()))
        .then { it.getArgument<(List<Parking>) -> Unit>(2)(listOf(testParking)) }

    // Force list update
    parkingViewModel.getParkingsInRadius(TestInstancesParking.referencePoint, 2.0)

    composeTestRule.setContent { SpotListScreen(mockNavigationActions, parkingViewModel) }

    // Check that the list is displayed
    composeTestRule.onNodeWithTag("SpotListColumn").assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag("SpotListItem", useUnmergedTree = true)
        .assertCountEquals(1)
        .assertAll(hasClickAction())

    // Check that the parking name is displayed
    composeTestRule
        .onNodeWithTag("ParkingName", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals(testParking.optName ?: "Unnamed Parking")

    // Check that the parking distance is displayed
    composeTestRule
        .onNodeWithTag("ParkingDistance", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals(
            String.format(
                "%.2f km",
                TurfMeasurement.distance(
                    TestInstancesParking.EPFLCenter, testParking.location.center)))

    // Select all 3 criteria
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()

    // Select Protection
    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("Protection"))
    composeTestRule.onNodeWithTag("Protection", useUnmergedTree = true).performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("ProtectionFilter"))

    composeTestRule
        .onNodeWithTag("ProtectionFilter")
        .performScrollToIndex(testParking.protection.ordinal)
    composeTestRule.onNodeWithText(testParking.protection.description).performClick()

    // Select Rack Type
    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("Rack Type"))
    composeTestRule.onNodeWithTag("Rack Type", useUnmergedTree = true).performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("RackTypeFilter"))

    composeTestRule
        .onNodeWithTag("RackTypeFilter")
        .performScrollToIndex(testParking.rackType.ordinal)
    composeTestRule
        .onNodeWithText(testParking.rackType.description)
        .assertHasClickAction()
        .performClick()

    composeTestRule.onNodeWithTag("Rack Type", useUnmergedTree = true).performClick()

    // Select Capacity
    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("Capacity"))
    composeTestRule.onNodeWithTag("Capacity", useUnmergedTree = true).performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("CapacityFilter"))

    composeTestRule
        .onNodeWithTag("CapacityFilter")
        .performScrollToIndex(testParking.capacity.ordinal)
    composeTestRule
        .onNodeWithText(testParking.capacity.description)
        .assertHasClickAction()
        .performClick()

    // Store filters away
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()
    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SpotListItem"))
    composeTestRule.onAllNodesWithTag("SpotListItem").assertCountEquals(1)
  }
}
