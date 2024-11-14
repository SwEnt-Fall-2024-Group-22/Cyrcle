package com.github.se.cyrcle.ui.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.ImageRepository
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.parking.Tile
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserDetails
import com.github.se.cyrcle.model.user.UserPublic
import com.github.se.cyrcle.model.user.UserRepository
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.mapbox.turf.TurfMeasurement
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class ListScreenTest {
    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var mockUserRepository: UserRepository
    private lateinit var mockParkingRepository: ParkingRepository
    private lateinit var mockImageRepository: ImageRepository
    private lateinit var mockNavigationActions: NavigationActions
    private lateinit var parkingViewModel: ParkingViewModel
    private lateinit var mapViewModel: MapViewModel
    private lateinit var userViewModel: UserViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockNavigationActions = mock(NavigationActions::class.java)
        mockUserRepository = MockUserRepository()
        mockParkingRepository = MockParkingRepository()
        mockImageRepository = MockImageRepository()

        parkingViewModel = ParkingViewModel(mockImageRepository, mockParkingRepository)
        mapViewModel = MapViewModel()
        userViewModel = UserViewModel(mockUserRepository, mockParkingRepository)

        `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.LIST)

        // Set up test user
        val user = User(
            UserPublic("1", "janesmith", "http://example.com/jane.jpg"),
            UserDetails("Jane", "Smith", "jane.smith@example.com")
        )

        // Set up test data
        // parking1 already in
        parkingViewModel.addParking(TestInstancesParking.parking2)
        parkingViewModel.addParking(TestInstancesParking.parking3)
        userViewModel.addUser(user)
        userViewModel.getUserById(user.public.userId)
        userViewModel.addFavoriteParkingToSelectedUser(TestInstancesParking.parking1.uid)
        userViewModel.addFavoriteParkingToSelectedUser(TestInstancesParking.parking2.uid)
        userViewModel.getSelectedUserFavoriteParking()
    }


    @Test
    fun testPinActionCardDisplayed() {
        composeTestRule.setContent {
            var pinnedParkings by rememberSaveable { mutableStateOf(setOf<String>()) }
            val handlePinStatusChanged = { parkingId: String, isPinned: Boolean ->
                pinnedParkings =
                    if (isPinned) {
                        pinnedParkings + parkingId
                    } else {
                        pinnedParkings - parkingId
                    }
            }

            SpotCard(
                mockNavigationActions,
                parkingViewModel,
                userViewModel,
                TestInstancesParking.parking1,
                0.0,
                initialIsPinned = false,
                onPinStatusChanged = handlePinStatusChanged)
        }

        // Verify that the PinActionCard is displayed
        composeTestRule.onNodeWithTag("PinActionCard").assertIsDisplayed()
        composeTestRule.onNodeWithTag("UnpinActionCard").assertIsNotDisplayed()
    }

    @Test
    fun testUnpinActionCardDisplayed() {
        composeTestRule.setContent {
            var pinnedParkings by rememberSaveable { mutableStateOf(setOf<String>()) }
            val handlePinStatusChanged = { parkingId: String, isPinned: Boolean ->
                pinnedParkings =
                    if (isPinned) {
                        pinnedParkings + parkingId
                    } else {
                        pinnedParkings - parkingId
                    }
            }
            SpotCard(
                mockNavigationActions,
                parkingViewModel,
                userViewModel,
                TestInstancesParking.parking1,
                0.0,
                initialIsPinned = true,
                onPinStatusChanged = handlePinStatusChanged)
        }
        composeTestRule.onNodeWithTag("PinActionCard").assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("UnpinActionCard").assertIsDisplayed()
    }

    @Test
    fun testAddToFavoritesActionCardDisplayed() {
        composeTestRule.setContent {
            var pinnedParkings by rememberSaveable { mutableStateOf(setOf<String>()) }
            val handlePinStatusChanged = { parkingId: String, isPinned: Boolean ->
                pinnedParkings =
                    if (isPinned) {
                        pinnedParkings + parkingId
                    } else {
                        pinnedParkings - parkingId
                    }
            }

            SpotCard(
                mockNavigationActions,
                parkingViewModel,
                userViewModel,
                TestInstancesParking.parking3, // not in our user's favorites
                0.0,
                initialIsPinned = false,
                onPinStatusChanged = handlePinStatusChanged)
        }

        // Verify that the AddToFavoriteActionCard is displayed
        composeTestRule.onNodeWithTag("AddToFavoriteActionCard").assertIsDisplayed()
        composeTestRule.onNodeWithTag("AlreadyFavoriteActionCard").assertIsNotDisplayed()
    }

    @Test
    fun testAlreadyFavoriteActionCardDisplayed() {
        composeTestRule.setContent {
            var pinnedParkings by rememberSaveable { mutableStateOf(setOf<String>()) }
            val handlePinStatusChanged = { parkingId: String, isPinned: Boolean ->
                pinnedParkings =
                    if (isPinned) {
                        pinnedParkings + parkingId
                    } else {
                        pinnedParkings - parkingId
                    }
            }

            SpotCard(
                mockNavigationActions,
                parkingViewModel,
                userViewModel,
                TestInstancesParking.parking1, // in our user's favorites
                0.0,
                initialIsPinned = false,
                onPinStatusChanged = handlePinStatusChanged)
        }

        // Verify that the AlreadyFavoriteActionCard is displayed
        composeTestRule.onNodeWithTag("AddToFavoriteActionCard").assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("AlreadyFavoriteActionCard").assertIsDisplayed()
    }

    @Test
    fun testQuickFavoriteAddsToUserFavorites() {
        composeTestRule.setContent {
            var pinnedParkings by rememberSaveable { mutableStateOf(setOf<String>()) }
            val handlePinStatusChanged = { parkingId: String, isPinned: Boolean ->
                pinnedParkings =
                    if (isPinned) {
                        pinnedParkings + parkingId
                    } else {
                        pinnedParkings - parkingId
                    }
            }

            SpotCard(
                mockNavigationActions,
                parkingViewModel,
                userViewModel,
                TestInstancesParking.parking3,
                0.0,
                initialIsPinned = false,
                onPinStatusChanged = handlePinStatusChanged)
        }

        val isFavorite =
            userViewModel.currentUser.value
                ?.details
                ?.favoriteParkings
                ?.contains(TestInstancesParking.parking3.uid) ?: false
        assert(!isFavorite)

        // Perform swipe left to add to favorites using general swipe
        composeTestRule.onNodeWithTag("SpotListItem").performTouchInput {
            swipe(
                start = centerRight,
                end = centerLeft,
                durationMillis = 300
            )
        }

        // Check if the parking was added to favorites
        val isFavoriteAdded =
            userViewModel.currentUser.value
                ?.details
                ?.favoriteParkings
                ?.contains(TestInstancesParking.parking3.uid) ?: true
        assert(isFavoriteAdded)
    }

    @Test
    fun testQuickFavoriteDoesNothingOnQuickAddAlreadyFavorite() {
        composeTestRule.setContent {
            var pinnedParkings by rememberSaveable { mutableStateOf(setOf<String>()) }
            val handlePinStatusChanged = { parkingId: String, isPinned: Boolean ->
                pinnedParkings =
                    if (isPinned) {
                        pinnedParkings + parkingId
                    } else {
                        pinnedParkings - parkingId
                    }
            }

            SpotCard(
                mockNavigationActions,
                parkingViewModel,
                userViewModel,
                TestInstancesParking.parking1, // already in favorites
                0.0,
                initialIsPinned = false,
                onPinStatusChanged = handlePinStatusChanged)
        }

        val isFavorite =
            userViewModel.currentUser.value
                ?.details
                ?.favoriteParkings
                ?.contains(TestInstancesParking.parking1.uid) ?: false
        assert(isFavorite)

        // Perform swipe left to add to favorites using general swipe
        composeTestRule.onNodeWithTag("SpotListItem").performTouchInput {
            swipe(
                start = centerRight,
                end = centerLeft,
                durationMillis = 300
            )
        }

        // Check if the parking was added to favorites
        val isFavoriteAdded =
            userViewModel.currentUser.value
                ?.details
                ?.favoriteParkings
                ?.contains(TestInstancesParking.parking1.uid) ?: true
        assert(isFavoriteAdded)
    }


    @Test
    fun testSpotCardIsDisplayed() {
        composeTestRule.setContent {
            var pinnedParkings by rememberSaveable { mutableStateOf(setOf<String>()) }
            val handlePinStatusChanged = { parkingId: String, isPinned: Boolean ->
                pinnedParkings = if (isPinned) {
                    pinnedParkings + parkingId
                } else {
                    pinnedParkings - parkingId
                }
            }

            SpotCard(
                navigationActions = mockNavigationActions,
                parkingViewModel = parkingViewModel,
                userViewModel = userViewModel,
                parking = TestInstancesParking.parking1,
                distance = 0.0,
                initialIsPinned = false,
                onPinStatusChanged = handlePinStatusChanged
            )
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
            var pinnedParkings by rememberSaveable { mutableStateOf(setOf<String>()) }
            val handlePinStatusChanged = { parkingId: String, isPinned: Boolean ->
                pinnedParkings = if (isPinned) {
                    pinnedParkings + parkingId
                } else {
                    pinnedParkings - parkingId
                }
            }

            SpotCard(
                navigationActions = mockNavigationActions,
                parkingViewModel = parkingViewModel,
                userViewModel = userViewModel,
                parking = TestInstancesParking.parking2,
                distance = 0.0,
                initialIsPinned = false,
                onPinStatusChanged = handlePinStatusChanged
            )
        }

        composeTestRule
            .onNodeWithTag("SpotListItem", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertHasClickAction()
            .performClick()

        composeTestRule.onNodeWithTag("ParkingNoReviews", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("ParkingNbReviews", useUnmergedTree = true).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("ParkingRating", useUnmergedTree = true).assertIsNotDisplayed()

        verify(mockNavigationActions).navigateTo(Screen.PARKING_DETAILS)
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
        val loc = testParking.location.center
        // Prepare to populate the parking list
        `when`(mockParkingRepository.getParkingsBetween(any(), any(), any(), any())).then {
            it.getArgument<(List<Parking>) -> Unit>(2)(emptyList())
        }
        `when`(
            mockParkingRepository.getParkingsBetween(
                eq(Tile.getTileFromPoint(loc).bottomLeft), any(), any(), any()))
            .then { it.getArgument<(List<Parking>) -> Unit>(2)(listOf(testParking)) }

        composeTestRule.setContent {
            SpotListScreen(mockNavigationActions, parkingViewModel, mapViewModel, userViewModel)
        }
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SpotListColumn"))

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
                    "%.0f m",
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