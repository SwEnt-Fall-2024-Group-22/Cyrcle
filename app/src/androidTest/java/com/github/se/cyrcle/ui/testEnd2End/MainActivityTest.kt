package com.github.se.cyrcle.ui.testEnd2End

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeRight
import com.github.se.cyrcle.MainActivity
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.parking.online.ParkingRepository
import com.github.se.cyrcle.model.review.TestInstancesReview
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.github.se.cyrcle.permission.PermissionHandler
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
  private lateinit var userProfileRobot: UserProfileRobot
  private lateinit var cardRobot: ParkingDetailsScreenRobot
  private lateinit var addParkingRobot: AddParkingRobot
  private lateinit var allReviewsRobot: AllReviewRobot
  private lateinit var addReviewRobot: ReviewRobot

  private lateinit var permissionHandler: PermissionHandler

  @Before
  fun setUp() {
    hiltRule.inject()

    permissionHandler = composeTestRule.activity.permissionsHandler

    authRobot = AuthScreenRobot(composeTestRule)
    mapRobot = MapScreenRobot(composeTestRule, permissionHandler)
    listRobot = ListScreenRobot(composeTestRule)
    userProfileRobot = UserProfileRobot(composeTestRule)
    cardRobot = ParkingDetailsScreenRobot(composeTestRule)
    addParkingRobot = AddParkingRobot(composeTestRule)
    allReviewsRobot = AllReviewRobot(composeTestRule)
    addReviewRobot = ReviewRobot(composeTestRule)
  }

  @Test
  fun testAddParking() {
    composeTestRule.activity.userRepository.addUser(TestInstancesUser.user1, {}, {})

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

    composeTestRule.activity.userRepository.addUser(TestInstancesUser.user1, {}, {})

    // Authenticated user

    authRobot.assertAuthScreen()
    authRobot.performSignIn()

    mapRobot.assertMapScreen()
    mapRobot.toList()

    listRobot.assertListScreen(true)
    listRobot.toCard(0)

    cardRobot.assertParkingDetailsScreen(true)
    cardRobot.goBack()

    listRobot.toUserProfile()

    userProfileRobot.assertUserProfileScreen(true)
    userProfileRobot.signOut()

    // Anonymous user

    authRobot.assertAuthScreen()
    authRobot.performAnonymousSignIn()

    mapRobot.assertMapScreen()
    mapRobot.toList()

    listRobot.assertListScreen(false)
  }

  @Test
  fun testProfile() {
    composeTestRule.activity.userRepository.addUser(TestInstancesUser.user1, {}, {})

    // Authenticated user
    authRobot.assertAuthScreen()
    authRobot.performSignIn()
    mapRobot.assertMapScreen()
    mapRobot.toUserProfile()
    userProfileRobot.assertUserProfileScreen(true)
    userProfileRobot.signOut()

    // Anonymous user
    authRobot.assertAuthScreen()
    authRobot.performAnonymousSignIn()
    mapRobot.assertMapScreen()
    mapRobot.toAnonymousUserProfile()
    userProfileRobot.assertUserProfileScreen(false)
    userProfileRobot.toAuth()

    // Offline user
    authRobot.assertAuthScreen()
    authRobot.performOfflineSignIn()
    mapRobot.assertMapScreen()
    mapRobot.assertIsOfflineMode()
    mapRobot.toAnonymousUserProfile()
    userProfileRobot.assertUserProfileScreen(false)
  }

  @Test
  fun testReview() {

    composeTestRule.activity.userRepository.addUser(TestInstancesUser.user1, {}, {})
    composeTestRule.activity.reviewRepository.addReview(TestInstancesReview.review6, {}, {})

    // Authenticated user
    authRobot.assertAuthScreen()
    authRobot.performSignIn()

    mapRobot.assertMapScreen()
    mapRobot.toList()

    listRobot.assertListScreen(true)
    listRobot.toCard(0)

    cardRobot.assertParkingDetailsScreen(true)
    cardRobot.goAllReviews()

    allReviewsRobot.assertAllReviewScreen(true)
    allReviewsRobot.goToAddReview()

    addReviewRobot.assertAddReviewScreen()
    addReviewRobot.addReview()

    allReviewsRobot.assertReviewIsAdded()
    allReviewsRobot.deleteReviewAndAssertDeleted()
    allReviewsRobot.toUserProfile()

    userProfileRobot.assertUserProfileScreen(true)
    userProfileRobot.signOut()

    // Anonymous user
    authRobot.assertAuthScreen()
    authRobot.performAnonymousSignIn()

    mapRobot.assertMapScreen()
    mapRobot.toList()

    listRobot.assertListScreen(false)
    listRobot.toCard(0)

    cardRobot.assertParkingDetailsScreen(false)
    cardRobot.goAllReviews()

    allReviewsRobot.assertAllReviewScreen(false)
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
    fun assertParkingDetailsScreen(signedIn: Boolean) {
      composeTestRule.onNodeWithTag("TopAppBar").assertIsDisplayed()
      composeTestRule.onNodeWithTag("RowCapacityRack").assertIsDisplayed()
      composeTestRule.onNodeWithTag("RowProtectionPrice").assertIsDisplayed()
      composeTestRule.onNodeWithTag("RowSecurity").assertIsDisplayed()
      composeTestRule.onNodeWithTag("ButtonsColumn").performScrollTo().assertIsDisplayed()
      composeTestRule.onNodeWithTag("ShowInMapButton").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("ParkingDetailsScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("ParkingDetailsColumn").assertIsDisplayed()

      composeTestRule.onNodeWithTag("ParkingDetailsColumn").performTouchInput {
        swipe(end = Offset(centerX, centerX * 0.9f), start = Offset(centerX, centerX * 0.1f))
      }

      composeTestRule.onNodeWithTag("TopInteractionRow").assertIsDisplayed()
      composeTestRule.onNodeWithTag("IconsRow").assertIsDisplayed()
      composeTestRule.onNodeWithTag("PinIcon").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("AverageRatingRow").assertIsDisplayed()
      composeTestRule.onNodeWithTag("ImagesRow").assertIsDisplayed()
      composeTestRule.onNodeWithTag("addPhotoButton").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("InfoColumn").assertIsDisplayed()

      if (!signedIn) {
        composeTestRule
            .onNodeWithTag("BlackOutlinedFavoriteIcon")
            .assertIsDisplayed()
            .assertHasClickAction()
      }
    }

    @OptIn(ExperimentalTestApi::class)
    fun goBack() {
      composeTestRule
          .onNodeWithTag("TopAppBarGoBackButton")
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
      composeTestRule.waitUntilAtLeastOneExists(
          hasTestTag("SpotListScreen").or(hasTestTag("MapScreen")))
    }

    @OptIn(ExperimentalTestApi::class)
    fun goAllReviews() {

      composeTestRule.waitUntilExactlyOneExists(hasTestTag("SeeAllReviewsText"))
      composeTestRule.onNodeWithTag("ParkingDetailsColumn").performTouchInput {
        swipe(end = Offset(centerX, centerX * 0.9f), start = Offset(centerX, centerX * 0.1f))
      }
      composeTestRule.onNodeWithTag("SeeAllReviewsText").assertIsDisplayed().performClick()
      composeTestRule.waitUntilExactlyOneExists(hasTestTag("ReviewCard0"))
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
          .onAllNodesWithTag("SpotListItem")[index]
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()

      composeTestRule.waitUntilExactlyOneExists(hasTestTag("ParkingDetailsScreen"))
    }

    fun assertListScreen(signedIn: Boolean) {
      composeTestRule.onNodeWithTag("NavigationBar").assertIsDisplayed()
      composeTestRule.onNodeWithTag("SpotListColumn").assertIsDisplayed()
      composeTestRule.onNodeWithTag("ShowFiltersButton").assertIsDisplayed().assertHasClickAction()

      composeTestRule.onNodeWithTag("SpotListItem").assertIsDisplayed().assertHasClickAction()
      composeTestRule.onNodeWithTag("SpotCardContent", useUnmergedTree = true).assertExists()
      composeTestRule.onNodeWithTag("ParkingName", useUnmergedTree = true).assertExists()
      composeTestRule.onNodeWithTag("ParkingDistance", useUnmergedTree = true).assertExists()
      composeTestRule.onNodeWithTag("PinActionCard", useUnmergedTree = true).assertExists()

      if (!signedIn) {
        composeTestRule
            .onNodeWithTag("AddToFavoriteActionCard", useUnmergedTree = true)
            .assertExists()
      }
    }

    @OptIn(ExperimentalTestApi::class)
    fun toUserProfile() {
      composeTestRule
          .onNodeWithTag(TopLevelDestinations.PROFILE.textId)
          .assertHasClickAction()
          .performClick()

      composeTestRule.waitUntilExactlyOneExists(hasTestTag("ViewProfileScreen"))
    }
  }

  // ============================================================================
  // ============================= MAPSCREEN ROBOT ==============================
  // ============================================================================
  private class MapScreenRobot(
      val composeTestRule: ComposeTestRule,
      val permissionHandler: PermissionHandler
  ) {

    fun assertMapScreen() {
      composeTestRule.onNodeWithTag("MapScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("NavigationBar").assertIsDisplayed()

      composeTestRule.onNodeWithTag("SearchBar").assertIsDisplayed().assertHasClickAction()
      if (permissionHandler.getLocalisationPerm().value)
          composeTestRule.onNodeWithTag("recenterButton").assertIsDisplayed().assertHasClickAction()
    }

    fun assertIsOfflineMode() {
      composeTestRule.onNodeWithTag("MapScreen").assertIsDisplayed()
      composeTestRule.onNodeWithTag("addButton").assertIsNotDisplayed()
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
    fun toUserProfile() {
      composeTestRule
          .onNodeWithTag(TopLevelDestinations.PROFILE.textId)
          .assertHasClickAction()
          .performClick()

      composeTestRule.waitUntilExactlyOneExists(hasTestTag("ViewProfileScreen"))
    }

    @OptIn(ExperimentalTestApi::class)
    fun toAnonymousUserProfile() {
      composeTestRule
          .onNodeWithTag(TopLevelDestinations.PROFILE.textId)
          .assertHasClickAction()
          .performClick()

      composeTestRule.waitUntilExactlyOneExists(hasTestTag("CreateProfileScreen"))
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
      composeTestRule.onNodeWithTag("LoginTitle").assertIsDisplayed().assertTextEquals("Welcome to")
      composeTestRule.onNodeWithTag("AnonymousLoginButton").assertIsDisplayed()
      composeTestRule
          .onNodeWithTag("AuthenticateButton")
          .assertIsDisplayed()
          .assertHasClickAction()
          .assertTextContains("Sign in with Google")
      composeTestRule.onNodeWithTag("AppLogo").assertIsDisplayed()
      composeTestRule.onNodeWithTag("OfflineModeButton").assertIsDisplayed()
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

    @OptIn(ExperimentalTestApi::class)
    fun performOfflineSignIn() {
      composeTestRule.onNodeWithTag("OfflineModeButton").performClick()
      composeTestRule.waitUntilExactlyOneExists(hasTestTag("MapScreen"))
    }
  }

  // ============================================================================
  // ============================ USER PROFILE ROBOT ============================
  // ============================================================================
  private class UserProfileRobot(val composeTestRule: ComposeTestRule) {
    fun assertUserProfileScreen(isUserSignedIn: Boolean) {
      if (isUserSignedIn) {
        composeTestRule.onNodeWithTag("ViewProfileScreen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("SignOutButton").assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithTag("EditButton").assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithTag("GamblingButton").assertIsDisplayed().assertHasClickAction()
      } else {
        composeTestRule.onNodeWithTag("CreateProfileScreen").assertIsDisplayed()
      }
    }

    @OptIn(ExperimentalTestApi::class)
    fun signOut() {
      composeTestRule.onNodeWithTag("SignOutButton").performClick()
      composeTestRule.waitUntilAtLeastOneExists(hasTestTag("SignOutDialogConfirmButton"))
      composeTestRule
          .onNodeWithTag("SignOutDialogConfirmButton")
          .assertHasClickAction()
          .performClick()
      composeTestRule.waitUntilExactlyOneExists(hasTestTag("LoginScreen"))
    }

    @OptIn(ExperimentalTestApi::class)
    fun toAuth() {
      composeTestRule
          .onNodeWithTag("GoBackToSignInButton")
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
      composeTestRule.waitUntilExactlyOneExists(hasTestTag("LoginScreen"))
    }
  }
  // ============================================================================
  // ============================= ALL REVIEWS SCREEN ROBOT =============================
  // ============================================================================
  private class AllReviewRobot(val composeTestRule: ComposeTestRule) {

    fun assertAllReviewScreen(signedIn: Boolean) {
      composeTestRule.onNodeWithTag("ReviewCard0").assertIsDisplayed()

      if (signedIn) {
        composeTestRule.onNodeWithTag("AddReviewButton").assertIsDisplayed().assertHasClickAction()
      } else {
        composeTestRule.onNodeWithTag("AddReviewButton").assertIsNotDisplayed()
      }
    }

    fun goToAddReview() {
      composeTestRule
          .onNodeWithTag("AddReviewButton")
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
    }

    fun assertReviewIsAdded() {
      composeTestRule.onNodeWithTag("ReviewCard-1").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    fun toUserProfile() {

      composeTestRule
          .onNodeWithTag("TopAppBarGoBackButton")
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
      composeTestRule
          .onNodeWithTag(TopLevelDestinations.PROFILE.textId)
          .assertHasClickAction()
          .performClick()

      composeTestRule.waitUntilExactlyOneExists(hasTestTag("ViewProfileScreen"))
    }

    @OptIn(ExperimentalTestApi::class)
    fun deleteReviewAndAssertDeleted() {
      composeTestRule
          .onNodeWithTag("MoreOptions-1Button")
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
      composeTestRule.waitUntilExactlyOneExists(hasTestTag("MoreOptions-1DeleteReviewItem"))
      composeTestRule
          .onNodeWithTag("MoreOptions-1DeleteReviewItem")
          .assertIsDisplayed()
          .assertHasClickAction()
          .performClick()
      composeTestRule.onNodeWithTag("ReviewCard-1").assertIsNotDisplayed().assertDoesNotExist()
    }
  }

  // ============================================================================
  // ============================= ADD REVIEW ROBOT ==============================
  // ============================================================================
  private class ReviewRobot(val composeTestRule: ComposeTestRule) {

    fun assertAddReviewScreen() {
      composeTestRule.onNodeWithTag("ExperienceText").assertIsDisplayed()
      composeTestRule.onNodeWithTag("Slider").assertIsDisplayed()
      composeTestRule.onNodeWithTag("ReviewInput").assertIsDisplayed()
      composeTestRule.onNodeWithTag("AddReviewButton")
    }

    fun addReview() {
      composeTestRule.onNodeWithTag("Slider").performTouchInput { swipeRight() }

      composeTestRule
          .onNodeWithTag("ReviewInput")
          .performTextInput("Trop bien mais juste pour test")

      composeTestRule.onNodeWithTag("AddReviewButton").assertHasClickAction().performClick()
    }
  }

  // ============================================================================
  // ============================= HELPER FUNCTIONS =============================
  // ============================================================================

  private fun assertParkingInRepo(repository: ParkingRepository, parking: Parking) {
    val mockRepository = repository as MockParkingRepository
    repository.getParkingsByListOfIds(
        (0 until mockRepository.uid).map { it.toString() },
        {
          val found = it.find { p -> p.optName == parking.optName }
          if (found == null) fail("Parking not found")
          else {
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
