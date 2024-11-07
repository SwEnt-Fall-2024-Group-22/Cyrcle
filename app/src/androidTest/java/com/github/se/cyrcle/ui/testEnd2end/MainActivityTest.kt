package com.github.se.cyrcle.ui.testEnd2end

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.github.se.cyrcle.MainActivity
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class MainActivityTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Inject lateinit var parkingRepository: ParkingRepository

  private lateinit var authRobot: AuthScreenRobot
  private lateinit var mapRobot: MapScreenRobot
  private lateinit var listRobot: ListScreenRobot
  private lateinit var cardRobot: ParkingDetailsScreenRobot

  @Before
  fun setUp() {

    hiltRule.inject()

    authRobot = AuthScreenRobot(composeTestRule)
    mapRobot = MapScreenRobot(composeTestRule)
    listRobot = ListScreenRobot(composeTestRule)
    cardRobot = ParkingDetailsScreenRobot(composeTestRule)
  }

  @Test
  fun reviewCardDisplaysWell() {
    authRobot.assertAuthScreen()
    authRobot.performSignIn()

    mapRobot.assertMapScreen()
    mapRobot.toList()

    listRobot.assertListScreen()
    listRobot.toCard(0)

    cardRobot.assertParkingDetailsScreen()
  }

  private class ParkingDetailsScreenRobot(val composeTestRule: ComposeTestRule) {
    fun assertParkingDetailsScreen() {
      composeTestRule.onNodeWithTag("TopAppBar").assertIsDisplayed()
      composeTestRule.onNodeWithTag("RowCapacityRack").assertIsDisplayed()
      composeTestRule.onNodeWithTag("RowProtectionPrice").assertIsDisplayed()
      composeTestRule.onNodeWithTag("RowSecurity").assertIsDisplayed()
      composeTestRule.onNodeWithTag("ButtonsColumn").assertIsDisplayed()
      composeTestRule.onNodeWithTag("ShowInMapButton").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("AddReviewButton").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("ReportButton").assertIsDisplayed().assertHasClickAction()
    }

    @OptIn(ExperimentalTestApi::class)
    fun goBack() {
      composeTestRule
          .onNodeWithTag("GoBackButton")
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
      composeTestRule.waitUntilAtLeastOneExists(
          hasTestTag("SpotListScreen").or(hasTestTag("MapScreen")))
    }
  }

  private class ListScreenRobot(val composeTestRule: ComposeTestRule) {

    @OptIn(ExperimentalTestApi::class)
    fun toCard(index: Int) {
      composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SpotListItem"))
      composeTestRule.onNodeWithTag("SpotListColumn").performScrollToIndex(index)
      composeTestRule
          .onNodeWithTag("SpotListColumn")
          .onChildAt(index)
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()

      composeTestRule.waitUntilExactlyOneExists(hasTestTag("ParkingDetailsScreen"))
    }

    fun assertListScreen() {
      composeTestRule.onNodeWithTag("NavigationBar").assertIsDisplayed()
      composeTestRule.onNodeWithTag("SpotListColumn").assertIsDisplayed()
      composeTestRule.onNodeWithTag("ShowFiltersButton").assertIsDisplayed().assertHasClickAction()

      // TODO check that the cards are displayed
    }

    @OptIn(ExperimentalTestApi::class)
    fun toMap() {
      composeTestRule
          .onNodeWithTag(TopLevelDestinations.MAP.textId)
          .assertHasClickAction()
          .performClick()

      composeTestRule.waitUntilExactlyOneExists(hasTestTag("MapScreen"))
    }
  }

  private class MapScreenRobot(val composeTestRule: ComposeTestRule) {

    fun assertMapScreen() {
      composeTestRule.onNodeWithTag("MapScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("addButton").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("ZoomControlsIn").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("ZoomControlsOut").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("NavigationBar").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    fun toList() {
      composeTestRule
          .onNodeWithTag(TopLevelDestinations.LIST.textId)
          .assertHasClickAction()
          .performClick()

      composeTestRule.waitUntilExactlyOneExists(hasTestTag("SpotListColumn"))
    }
  }

  private class AuthScreenRobot(val composeTestRule: ComposeTestRule) {

    fun assertAuthScreen() {
      composeTestRule.onNodeWithTag("LoginScreen").assertIsDisplayed()
      composeTestRule
          .onNodeWithTag("LoginTitle")
          .assertIsDisplayed()
          .assertTextEquals("Welcome to Cyrcle")
      composeTestRule
          .onNodeWithTag("AnonymousLoginButton")
          .assertIsDisplayed()
          .assertHasClickAction()
      composeTestRule
          .onNodeWithTag("GoogleLoginButton")
          .assertIsDisplayed()
          .assertHasClickAction()
          .assertTextContains("Sign in with Google")
    }

    @OptIn(ExperimentalTestApi::class)
    fun performSignIn() {
      composeTestRule.onNodeWithTag("AnonymousLoginButton").performClick()

      composeTestRule.waitUntilExactlyOneExists(hasTestTag("MapScreen"))
    }
  }
}
