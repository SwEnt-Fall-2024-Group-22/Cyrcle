package com.github.se.cyrcle.ui.map

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class MapScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockNavigation: NavigationActions
  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var mapViewModel: MapViewModel

  @Before
  fun setUp() {
    mockNavigation = mock(NavigationActions::class.java)
    val imageRepository = MockImageRepository()
    val parkingRepository = MockParkingRepository()
    parkingViewModel = ParkingViewModel(imageRepository, parkingRepository)
    mapViewModel = MapViewModel()
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
    composeTestRule.setContent { MapScreen(mockNavigation, parkingViewModel, mapViewModel) }

    composeTestRule.onNodeWithTag("MapScreen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("BottomNavigationBar").assertIsDisplayed()

    // Assert that the zoom controls are displayed
    composeTestRule.onNodeWithTag("ZoomControlsIn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ZoomControlsOut").assertIsDisplayed()

    // Assert that the add button is displayed
    composeTestRule.onNodeWithTag("addButton").assertIsDisplayed()
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
      MapScreen(navigationActions = mockNavigation, parkingViewModel, mapViewModel, state)
    }

    for (i in 0..(defaultZoom - minZoom).toInt()) {
      composeTestRule.onNodeWithTag("ZoomControlsOut").performClick()
    }

    // Assert the state value
    assert(state.value == minZoom)
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
      MapScreen(navigationActions = mockNavigation, parkingViewModel, mapViewModel, state)
    }

    for (i in 0..(maxZoom - defaultZoom).toInt()) {
      composeTestRule.onNodeWithTag("ZoomControlsIn").performClick()
    }

    // Assert the state value
    assert(state.value == maxZoom)
  }
}
