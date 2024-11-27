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
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockAuthenticationRepository
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockReportedObjectRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.authentication.AuthenticationRepository
import com.github.se.cyrcle.model.image.ImageRepository
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserDetails
import com.github.se.cyrcle.model.user.UserPublic
import com.github.se.cyrcle.model.user.UserRepository
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class ListScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockUserRepository: UserRepository
  private lateinit var mockParkingRepository: ParkingRepository
  private lateinit var mockImageRepository: ImageRepository
  private lateinit var authenticator: AuthenticationRepository
  private lateinit var mockReportedObjectRepository: ReportedObjectRepository
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
    authenticator = MockAuthenticationRepository()
    mockReportedObjectRepository = MockReportedObjectRepository()

    parkingViewModel =
        ParkingViewModel(mockImageRepository, mockParkingRepository, mockReportedObjectRepository)
    mapViewModel = MapViewModel()
    userViewModel =
        UserViewModel(mockUserRepository, mockParkingRepository, mockImageRepository, authenticator)

    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.LIST)

    // Set up test user
    val user =
        User(
            UserPublic("1", "janesmith", "http://example.com/jane.jpg"),
            UserDetails("Jane", "Smith", "jane.smith@example.com"))

    // Set up test data
    // parking1 already in
    parkingViewModel.addParking(TestInstancesParking.parking2)
    parkingViewModel.addParking(TestInstancesParking.parking3)
    userViewModel.setCurrentUser(user)
    userViewModel.setCurrentUserById(user.public.userId)
    userViewModel.addFavoriteParkingToSelectedUser(TestInstancesParking.parking1)
    userViewModel.addFavoriteParkingToSelectedUser(TestInstancesParking.parking2)
  }

  @Test
  fun testPinActionCardDisplayed() {
    composeTestRule.setContent {
      SpotCard(
          mockNavigationActions,
          parkingViewModel,
          userViewModel,
          TestInstancesParking.parking1,
          0.0)
    }

    // Verify that the PinActionCard is displayed
    composeTestRule.onNodeWithTag("PinActionCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("UnpinActionCard").assertIsNotDisplayed()
  }

  @Test
  fun testUnpinActionCardDisplayed() {
    composeTestRule.setContent {
      SpotCard(
          mockNavigationActions,
          parkingViewModel,
          userViewModel,
          TestInstancesParking.parking1,
          0.0)
    }

    // Unpin the parking
    parkingViewModel.togglePinStatus(TestInstancesParking.parking1)

    composeTestRule.onNodeWithTag("PinActionCard").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("UnpinActionCard").assertIsDisplayed()
  }

  @Test
  fun testAddToFavoritesActionCardDisplayed() {
    composeTestRule.setContent {
      SpotCard(
          mockNavigationActions,
          parkingViewModel,
          userViewModel,
          TestInstancesParking.parking3, // not in our user's favorites
          0.0)
    }

    // Verify that the AddToFavoriteActionCard is displayed
    composeTestRule.onNodeWithTag("AddToFavoriteActionCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AlreadyFavoriteActionCard").assertIsNotDisplayed()
  }

  @Test
  fun testAlreadyFavoriteActionCardDisplayed() {
    composeTestRule.setContent {
      SpotCard(
          mockNavigationActions,
          parkingViewModel,
          userViewModel,
          TestInstancesParking.parking1, // in our user's favorites
          0.0)
    }

    // Verify that the AlreadyFavoriteActionCard is displayed
    composeTestRule.onNodeWithTag("AddToFavoriteActionCard").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("AlreadyFavoriteActionCard").assertIsDisplayed()
  }

  @Test
  fun testQuickFavoriteAddsToUserFavorites() {
    composeTestRule.setContent {
      SpotCard(
          mockNavigationActions,
          parkingViewModel,
          userViewModel,
          TestInstancesParking.parking3,
          0.0)
    }

    val isFavorite =
        userViewModel.currentUser.value
            ?.details
            ?.favoriteParkings
            ?.contains(TestInstancesParking.parking3.uid) ?: false
    assert(!isFavorite)

    // Perform swipe left to add to favorites using general swipe
    composeTestRule.onNodeWithTag("SpotListItem").performTouchInput {
      swipe(start = centerRight, end = centerLeft, durationMillis = 300)
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
      SpotCard(
          mockNavigationActions,
          parkingViewModel,
          userViewModel,
          TestInstancesParking.parking1, // already in favorites
          0.0)
    }

    val isFavorite =
        userViewModel.currentUser.value
            ?.details
            ?.favoriteParkings
            ?.contains(TestInstancesParking.parking1.uid) ?: false
    assert(isFavorite)

    // Perform swipe left to add to favorites using general swipe
    composeTestRule.onNodeWithTag("SpotListItem").performTouchInput {
      swipe(start = centerRight, end = centerLeft, durationMillis = 300)
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
  fun testQuickFavoriteDoesNothingForUnsignedUsers() {
    userViewModel.setCurrentUser(null)
    composeTestRule.setContent {
      SpotCard(
          mockNavigationActions,
          parkingViewModel,
          userViewModel,
          TestInstancesParking.parking3,
          0.0)
    }

    composeTestRule.onNodeWithTag("AddToFavoriteActionCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AlreadyFavoriteActionCard").assertIsNotDisplayed()

    // Perform swipe left to add to favorites using general swipe
    composeTestRule.onNodeWithTag("SpotListItem").performTouchInput {
      swipe(start = centerRight, end = centerLeft, durationMillis = 300)
    }

    composeTestRule.onNodeWithTag("AddToFavoriteActionCard").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AlreadyFavoriteActionCard").assertIsNotDisplayed()
  }

  @Test
  fun testCCTVCheckboxIntegration() {
    composeTestRule.setContent {
      SpotListScreen(
          navigationActions = mockNavigationActions,
          parkingViewModel = parkingViewModel,
          mapViewModel = mapViewModel,
          userViewModel = userViewModel)
    }

    // Show filters
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()

    // Initial state should be unchecked
    composeTestRule.onNodeWithTag("CCTVCheckbox").assertIsDisplayed().performClick()

    // Verify the ViewModel was updated
    assert(parkingViewModel.onlyWithCCTV.value)

    // Click again to uncheck
    composeTestRule.onNodeWithTag("CCTVCheckbox").performClick()

    // Verify the ViewModel was updated back to false
    assert(!parkingViewModel.onlyWithCCTV.value)
  }

  @Test
  fun testCCTVCheckboxInteraction() {
    composeTestRule.setContent { FilterHeader(parkingViewModel) }

    // Show filters first
    composeTestRule.onNodeWithTag("ShowFiltersButton").performClick()

    // Check both checkbox and label are displayed
    composeTestRule.onNodeWithTag("CCTVCheckbox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("CCTVCheckboxLabel").assertIsDisplayed()
  }

  @Test
  fun testSpotListScreenStructure() {
    composeTestRule.setContent {
      SpotListScreen(
          navigationActions = mockNavigationActions,
          parkingViewModel = parkingViewModel,
          mapViewModel = mapViewModel,
          userViewModel = userViewModel)
    }

    // Verify main screen components
    composeTestRule.onNodeWithTag("SpotListScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SpotListColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AllSpotsTitle").assertIsDisplayed()
  }

  @Test
  fun testPinnedSpotsTitle() {
    composeTestRule.setContent {
      SpotListScreen(
          navigationActions = mockNavigationActions,
          parkingViewModel = parkingViewModel,
          mapViewModel = mapViewModel,
          userViewModel = userViewModel)
    }

    // Initially, no parking is pinned
    assert(parkingViewModel.pinnedParkings.value.isEmpty())
    composeTestRule.onNodeWithTag("PinnedSpotsTitle").assertIsNotDisplayed()

    // Pin a parking
    parkingViewModel.togglePinStatus(TestInstancesParking.parking1)
    assert(parkingViewModel.pinnedParkings.value.isNotEmpty())
    composeTestRule.onNodeWithTag("PinnedSpotsTitle").assertIsDisplayed()
  }

  @Test
  fun testSpotCardIsDisplayed() {
    composeTestRule.setContent {
      SpotCard(
          navigationActions = mockNavigationActions,
          parkingViewModel = parkingViewModel,
          userViewModel = userViewModel,
          parking = TestInstancesParking.parking1,
          distance = 0.0)
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
      SpotCard(
          navigationActions = mockNavigationActions,
          parkingViewModel = parkingViewModel,
          userViewModel = userViewModel,
          parking = TestInstancesParking.parking2,
          distance = 0.0)
    }

    composeTestRule
        .onNodeWithTag("SpotListItem", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()

    composeTestRule.onNodeWithTag("ParkingNoReviews", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("ParkingRating", useUnmergedTree = true).assertIsNotDisplayed()

    verify(mockNavigationActions).navigateTo(Screen.PARKING_DETAILS)
    assertEquals(TestInstancesParking.parking2, parkingViewModel.selectedParking.value)
  }

  @Test
  fun testShowFiltersButtonInitiallyDisplaysShowFilters() {
    composeTestRule.setContent { FilterHeader(parkingViewModel) }

    // Act & Assert
    composeTestRule.onNodeWithTag("ShowFiltersButton").assertIsDisplayed().assertHasClickAction()
  }

  @Test
  @OptIn(ExperimentalTestApi::class)
  fun testShowFiltersButtonTogglesFilterSection() {
    composeTestRule.setContent { FilterHeader(parkingViewModel) }

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

    composeTestRule.setContent { FilterHeader(parkingViewModel) }

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
    assert(
        parkingViewModel.selectedProtection.value.containsAll(
            ParkingProtection.entries.toSet() - ParkingProtection.entries[0]))
  }

  @Test
  @OptIn(ExperimentalTestApi::class)
  fun testRackTypeFilters() {
    composeTestRule.setContent { FilterHeader(parkingViewModel) }

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

    // Assert that the selected rack type is updated in the ParkingViewModel
    assert(
        parkingViewModel.selectedRackTypes.value.containsAll(
            ParkingRackType.entries.toSet() - ParkingRackType.entries[0]))
  }

  @Test
  @OptIn(ExperimentalTestApi::class)
  fun testCapacityFilters() {
    composeTestRule.setContent { FilterHeader(parkingViewModel) }

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
    assert(
        parkingViewModel.selectedCapacities.value.containsAll(
            ParkingCapacity.entries.toSet() - ParkingCapacity.entries[0]))
  }

  @Test
  fun testDistanceFormattingM() {
    composeTestRule.setContent {
      SpotCard(
          navigationActions = mockNavigationActions,
          parkingViewModel = parkingViewModel,
          userViewModel = userViewModel,
          parking = TestInstancesParking.parking1,
          distance = 0.5 // 500m
          )
    }

    // Test meters display
    composeTestRule
        .onNodeWithTag("ParkingDistance", useUnmergedTree = true)
        .assertTextEquals("500 m")
  }

  @Test
  fun testDistanceFormattingKM() {
    composeTestRule.setContent {
      SpotCard(
          navigationActions = mockNavigationActions,
          parkingViewModel = parkingViewModel,
          userViewModel = userViewModel,
          parking = TestInstancesParking.parking1,
          distance = 2.5 // 2.5km
          )
    }

    // Test kilometers display
    composeTestRule
        .onNodeWithTag("ParkingDistance", useUnmergedTree = true)
        .assertTextEquals("2.50 km")
  }

  @Test
  fun testParkingNameTruncation() {
    val longNameParking =
        TestInstancesParking.parking1.copy(
            optName = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
    parkingViewModel.addParking(longNameParking)

    composeTestRule.setContent {
      SpotCard(
          navigationActions = mockNavigationActions,
          parkingViewModel = parkingViewModel,
          userViewModel = userViewModel,
          parking = longNameParking,
          distance = 0.0)
    }

    composeTestRule
        .onNodeWithTag("ParkingName", useUnmergedTree = true)
        .assertTextEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa...")
  }
}
