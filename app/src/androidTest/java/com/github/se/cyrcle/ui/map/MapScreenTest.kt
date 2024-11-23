package com.github.se.cyrcle.ui.map

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.AuthenticationRepositoryMock
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockPermissionHandler
import com.github.se.cyrcle.di.mocks.MockReportedObjectRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Route
import com.github.se.cyrcle.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

@RunWith(AndroidJUnit4::class)
class MapScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockNavigation: NavigationActions
  private lateinit var mockReportedObjectRepository: ReportedObjectRepository
  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var userViewModel: UserViewModel
  private lateinit var mapViewModel: MapViewModel
  private lateinit var permissionHandler: MockPermissionHandler

  @Before
  fun setUp() {
    mockNavigation = mock(NavigationActions::class.java)
    mockReportedObjectRepository = MockReportedObjectRepository()

    val imageRepository = MockImageRepository()
    val parkingRepository = MockParkingRepository()
    val userRepository = MockUserRepository()
    val authenticator = AuthenticationRepositoryMock()

    parkingViewModel =
        ParkingViewModel(imageRepository, parkingRepository, mockReportedObjectRepository)
    userViewModel = UserViewModel(userRepository, parkingRepository, imageRepository, authenticator)
    mapViewModel = MapViewModel()
    permissionHandler = MockPermissionHandler()

    `when`(mockNavigation.currentRoute()).thenReturn(Screen.MAP)
  }

  /**
   * Test to verify that the map screen and its components are displayed correctly.
   *
   * This test sets the content to the `MapScreen` and checks if the map screen, bottom navigation
   * bar, zoom controls, and add button are displayed.
   */
  @Test
  fun testMapIsDisplayed() {
    composeTestRule.setContent {
      MapScreen(mockNavigation, parkingViewModel, userViewModel, mapViewModel, permissionHandler)
    }

    composeTestRule.onNodeWithTag("MapScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("NavigationBar").assertIsDisplayed()

    // Assert that the zoom controls are displayed
    composeTestRule.onNodeWithTag("ZoomControlsIn").assertIsDisplayed().assertHasClickAction()
    composeTestRule.onNodeWithTag("ZoomControlsOut").assertIsDisplayed().assertHasClickAction()
  }

  /**
   * Test to verify that the internal state of the zoom level decreases correctly.
   *
   * This test sets the content to the `MapScreen` with an initial zoom level state, performs zoom
   * out actions, and asserts that the state value matches the minimum zoom level.
   */
  @Test
  fun testInternalStateOut() {

    val state = mutableStateOf(defaultZoom)

    composeTestRule.setContent {
      MapScreen(
          mockNavigation, parkingViewModel, userViewModel, mapViewModel, permissionHandler, state)
    }

    for (i in 0..(defaultZoom - minZoom).toInt()) {
      composeTestRule.onNodeWithTag("ZoomControlsOut").performClick()
    }

    // Assert the state value
    assert(state.value == minZoom)
  }

  @Test
  fun testAddParkingRules() {
    composeTestRule.setContent {
      MapScreen(mockNavigation, parkingViewModel, userViewModel, mapViewModel, permissionHandler)
    }

    // Check that the add button has no click action when there is no user
    composeTestRule.onNodeWithTag("addButton").assertDoesNotExist()
    verifyNoInteractions(mockNavigation)

    userViewModel.setCurrentUser(TestInstancesUser.user1)

    // Check that the add button has a click action when there is a user
    composeTestRule.onNodeWithTag("addButton").performClick()
    verify(mockNavigation).navigateTo(Route.ADD_SPOTS)
  }

  /**
   * Test to verify that the internal state of the zoom level increases correctly.
   *
   * This test sets the content to the `MapScreen` with an initial zoom level state, performs zoom
   * in actions, and asserts that the state value matches the maximum zoom level.
   */
  @Test
  fun testInternalStateIn() {

    val state = mutableStateOf(defaultZoom)

    composeTestRule.setContent {
      MapScreen(
          mockNavigation, parkingViewModel, userViewModel, mapViewModel, permissionHandler, state)
    }

    for (i in 0..(maxZoom - defaultZoom).toInt()) {
      composeTestRule.onNodeWithTag("ZoomControlsIn").performClick()
    }

    // Assert the state value
    assert(state.value == maxZoom)
  }

  /**
   * Test to verify that the recenter button has a click action.
   *
   * This test sets the content to the `MapScreen` and checks if the recenter button has a click
   * action. It then performs a click action on the recenter button and asserts that the focus mode
   * is toggled.
   */
  @Test
  fun testRecenterButton() {
    composeTestRule.setContent {
      MapScreen(mockNavigation, parkingViewModel, userViewModel, mapViewModel, permissionHandler)
    }

    // Start with enabled location
    permissionHandler.authorizeLoc.value = true

    // Assert that the recenter button is displayed
    composeTestRule.onNodeWithTag("recenterButton").assertIsDisplayed()

    // Assert that the recenter button has a click action
    composeTestRule.onNodeWithTag("recenterButton").assertHasClickAction()

    assert(mapViewModel.isTrackingModeEnable.value)

    // Remove location permission
    permissionHandler.authorizeLoc.value = false
    // Assert that the recenter button is displayed
    composeTestRule.onNodeWithTag("recenterButton").assertDoesNotExist()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun testAddButtonNavigatesToLocationPicker() {
    val navigationActions = mock(NavigationActions::class.java)
    userViewModel.setCurrentUser(TestInstancesUser.user1)

    composeTestRule.setContent {
      MapScreen(navigationActions, parkingViewModel, userViewModel, mapViewModel, permissionHandler)
    }
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("addButton"))
    // Perform click on the add button
    composeTestRule.onNodeWithTag("addButton").performClick()

    verify(navigationActions).navigateTo(Route.ADD_SPOTS)
  }
}
