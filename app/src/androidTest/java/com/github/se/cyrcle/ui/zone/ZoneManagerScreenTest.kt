package com.github.se.cyrcle.ui.zone

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.zone.Zone
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.mapbox.geojson.BoundingBox
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

class ZoneManagerScreenTest {
  @get:Rule val composeTestRule = createComposeRule()
  private lateinit var navigationActions: NavigationActions
  val mapViewModel = MapViewModel()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun checkAllUIElementsAreDisplayedWithNoZones() {
    composeTestRule.setContent { ZoneManagerScreen(mapViewModel, navigationActions) }

    composeTestRule.waitUntilExactlyOneExists(hasTestTag("TopAppBar"))
    composeTestRule.onNodeWithTag("TopAppBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AddButton").assertIsDisplayed().performClick()
    verify(navigationActions).navigateTo(Screen.ZONE_SELECTION)

    composeTestRule.onNodeWithTag("ZoneManagerHeader").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ZoneCard").assertDoesNotExist()
  }

  @Test
  fun verifyDisplayOfZoneCard() {
    val zone =
        Zone(
            BoundingBox.fromLngLats(0.0, 0.0, 1.0, 1.0),
            "Test Zone",
        )
    val zonesState = mutableStateOf(listOf(zone))
    composeTestRule.setContent { ZoneCard(zone, zonesState) }
    composeTestRule
        .onNodeWithContentDescription("Refresh")
        .assertIsDisplayed()
        .assertHasClickAction()
    composeTestRule
        .onNodeWithContentDescription("Delete")
        .assertIsDisplayed()
        .assertHasClickAction()
  }

  @Test
  fun verifyHeader() {
    composeTestRule.setContent { ZoneHeader() }
    composeTestRule.onNodeWithTag("ZoneManagerHeaderArea").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ZoneManagerHeaderLastRefreshed").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Refresh").assertIsDisplayed()
  }
}
