package com.github.se.cyrcle.ui.testEnd2End

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import com.github.se.cyrcle.MainActivity
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class MainActivityTest {

  @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1) val composeTestRule = createAndroidComposeRule<MainActivity>()

  private lateinit var authRobot: AuthScreenRobot
  private lateinit var mapRobot: MapScreenRobot
  private lateinit var listRobot: ListScreenRobot
  private lateinit var cardRobot: ParkingDetailsScreenRobot
  private lateinit var addParkingRobot: AddParkingRobot

  @Before
  fun setUp() {
    hiltRule.inject()

    authRobot = AuthScreenRobot(composeTestRule)
    mapRobot = MapScreenRobot(composeTestRule)
    listRobot = ListScreenRobot(composeTestRule)
    cardRobot = ParkingDetailsScreenRobot(composeTestRule)
    addParkingRobot = AddParkingRobot(composeTestRule)
  }

  @Test
  fun testAddParking() {
    authRobot.assertAuthScreen()
    authRobot.performSignIn()

    mapRobot.assertMapScreen()
    mapRobot.toAddParking()

    addParkingRobot.assertLocationPickerScreen()
    addParkingRobot.selectLocation()
    addParkingRobot.makeRectangleAndNext()
    addParkingRobot.assertAttributesPickerScreen()

    addParkingRobot.addParking(TestInstancesParking.parking2)
    assertParkingInRepo(composeTestRule.activity.parkingRepository, TestInstancesParking.parking2)
  }

  @Test
  fun testReviewCard() {
    authRobot.assertAuthScreen()
    authRobot.performAnonymousSignIn()

    mapRobot.assertMapScreen()
    mapRobot.toList()

    listRobot.assertListScreen()
    listRobot.toCard(0)

    cardRobot.assertParkingDetailsScreen()
  }

  // ============================================================================
  // ============================ ADDPARKINGS ROBOT =============================
  // ============================================================================
  private class AddParkingRobot(val composeTestRule: ComposeTestRule) {

    fun assertLocationPickerScreen() {
      composeTestRule.onNodeWithTag("LocationPickerScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("LocationPickerBottomBar").assertIsDisplayed()
      composeTestRule.onNodeWithTag("LocationPickerTopBar").assertIsDisplayed()
      composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("nextButton").assertIsDisplayed().assertHasClickAction()
    }

    @OptIn(ExperimentalTestApi::class)
    fun selectLocation() {
      composeTestRule.onNodeWithTag("nextButton").assertHasClickAction().performClick()

      composeTestRule.waitUntilAtLeastOneExists(hasTestTag("toggleRectangleButton"))
    }

    @OptIn(ExperimentalTestApi::class)
    fun makeRectangleAndNext() = runTest {
      composeTestRule.onNodeWithTag("LocationPickerScreen").performTouchInput {
        swipe(start = Offset(0.5f, 0.5f), end = Offset(0.7f, 0.7f), durationMillis = 10)
      }
      composeTestRule.awaitIdle()
      composeTestRule.onNodeWithTag("nextButton").performClick()

      composeTestRule.waitUntilAtLeastOneExists(hasTestTag("AttributesPickerScreen"))
    }

    fun assertAttributesPickerScreen() {
      composeTestRule.onNodeWithTag("AttributesPickerScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("AttributesPickerTopBar").assertIsDisplayed()
      composeTestRule.onNodeWithTag("AttributesPickerBottomBar").assertIsDisplayed()
      composeTestRule.onNodeWithTag("cancelButton").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("submitButton").assertIsDisplayed().assertHasClickAction()
    }

    @OptIn(ExperimentalTestApi::class)
    fun addParking(parking: Parking) {
      fun selectEnumValue(enumIdx: Int, enumValueIdx: Int) {
        composeTestRule.onAllNodesWithTag("EnumDropDown")[enumIdx].performClick()
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("EnumDropDown0Item"))
        composeTestRule
            .onNodeWithTag("EnumDropDown${enumValueIdx}Item")
            .performScrollTo()
            .performClick()
      }

      composeTestRule.onNodeWithTag("AttributesPickerTitle").performTextClearance()
      composeTestRule.onNodeWithTag("AttributesPickerTitle").performTextInput(parking.optName ?: "")

      selectEnumValue(0, parking.protection.ordinal)
      selectEnumValue(1, parking.capacity.ordinal)
      selectEnumValue(2, parking.rackType.ordinal)

      if (parking.hasSecurity) {
        composeTestRule.onNodeWithTag("BooleanRadioButtonYesRadioButton").performClick()
      } else {
        composeTestRule.onNodeWithTag("BooleanRadioButtonNoRadioButton").performClick()
      }

      composeTestRule
          .onNodeWithTag("AttributesPickerColumn")
          .performScrollToNode(hasTestTag("AttributesPickerDescription"))
      composeTestRule.onNodeWithTag("AttributesPickerDescription").performTextClearance()
      composeTestRule
          .onNodeWithTag("AttributesPickerDescription")
          .performTextInput(parking.optDescription ?: "")

      composeTestRule.onNodeWithTag("submitButton").performClick()
      composeTestRule.waitUntilAtLeastOneExists(hasTestTag("MapScreen"))
    }
  }

  // ============================================================================
  // =========================== PARKINGDETAILS ROBOT ===========================
  // ============================================================================
  private class ParkingDetailsScreenRobot(val composeTestRule: ComposeTestRule) {
    fun assertParkingDetailsScreen() {
      composeTestRule.onNodeWithTag("TopAppBar").assertIsDisplayed()
      composeTestRule.onNodeWithTag("RowCapacityRack").assertIsDisplayed()
      composeTestRule.onNodeWithTag("RowProtectionPrice").assertIsDisplayed()
      composeTestRule.onNodeWithTag("RowSecurity").assertIsDisplayed()
      composeTestRule.onNodeWithTag("ButtonsColumn").assertIsDisplayed()
      composeTestRule.onNodeWithTag("ShowInMapButton").assertIsDisplayed().assertHasClickAction()
      composeTestRule
          .onNodeWithTag("ReportButton")
          .assertExists()
          .performScrollTo()
          .assertIsDisplayed()
          .assertHasClickAction()
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

  // ============================================================================
  // ============================= LISTSCREEN ROBOT =============================
  // ============================================================================
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

  // ============================================================================
  // ============================= MAPSCREEN ROBOT ==============================
  // ============================================================================
  private class MapScreenRobot(val composeTestRule: ComposeTestRule) {

    fun assertMapScreen() {
      composeTestRule.onNodeWithTag("MapScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("ZoomControlsIn").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("ZoomControlsOut").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("NavigationBar").assertIsDisplayed()
      composeTestRule.onNodeWithTag("recenterButton").assertIsDisplayed().assertHasClickAction()
    }

    @OptIn(ExperimentalTestApi::class)
    fun toList() {
      composeTestRule
          .onNodeWithTag(TopLevelDestinations.LIST.textId)
          .assertHasClickAction()
          .performClick()

      composeTestRule.waitUntilExactlyOneExists(hasTestTag("SpotListColumn"))
    }

    @OptIn(ExperimentalTestApi::class)
    fun toAddParking() {
      composeTestRule.onNodeWithTag("addButton").assertHasClickAction().performClick()

      composeTestRule.waitUntilExactlyOneExists(hasTestTag("LocationPickerScreen"))
    }
  }

  // ============================================================================
  // ============================= AUTHSCREEN ROBOT =============================
  // ============================================================================
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
          .onNodeWithTag("AuthenticateButton")
          .assertIsDisplayed()
          .assertHasClickAction()
          .assertTextContains("Sign in with Google")
    }

    @OptIn(ExperimentalTestApi::class)
    fun performAnonymousSignIn() {
      composeTestRule.onNodeWithTag("AnonymousLoginButton").performClick()

      composeTestRule.waitUntilExactlyOneExists(hasTestTag("MapScreen"))
    }

    @OptIn(ExperimentalTestApi::class)
    fun performSignIn() {
      composeTestRule.onNodeWithTag("AuthenticateButton").performClick()

      composeTestRule.waitUntilExactlyOneExists(hasTestTag("MapScreen"))
    }
  }

  // ============================================================================
  // ============================= HELPER FUNCTIONS =============================
  // ============================================================================

  private fun assertParkingInRepo(repository: ParkingRepository, parking: Parking) {
    repository.getAllParkings(
        {
          val found = it.find { p -> p.optName == parking.optName }
          if (found == null) {
            fail("Parking not found in repository")
          } else {
            assertEquals(parking.optDescription ?: "", found.optDescription)
            assertEquals(parking.protection, found.protection)
            assertEquals(parking.capacity, found.capacity)
            assertEquals(parking.rackType, found.rackType)
            assertEquals(parking.hasSecurity, found.hasSecurity)
          }
        },
        { fail("Error getting parkings") })
  }
}
