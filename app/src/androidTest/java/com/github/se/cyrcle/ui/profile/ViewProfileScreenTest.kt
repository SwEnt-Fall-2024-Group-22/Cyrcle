package com.github.se.cyrcle.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.parking.Location
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.mapbox.geojson.Point
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class ViewProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockNavigationActions: NavigationActions
  private lateinit var mockUserRepository: MockUserRepository
  private lateinit var mockParkingRepository: MockParkingRepository
  private lateinit var mockImageRepository: MockImageRepository

  private lateinit var userViewModel: UserViewModel
  private lateinit var parkingViewModel: ParkingViewModel

  @Before
  fun setUp() {
    mockNavigationActions = mock(NavigationActions::class.java)
    mockUserRepository = MockUserRepository()
    mockParkingRepository = MockParkingRepository()
    mockImageRepository = MockImageRepository()

    val user =
        User(
            userId = "1",
            username = "janesmith",
            firstName = "Jane",
            lastName = "Smith",
            email = "jane.smith@example.com",
            profilePictureUrl = "http://example.com/jane.jpg")

    userViewModel = UserViewModel(mockUserRepository, mockParkingRepository)
    parkingViewModel = ParkingViewModel(mockImageRepository, mockParkingRepository)

    userViewModel.addUser(user)

    // parking1 already added by whoever created parkingviewmodelmock on instanciation
    parkingViewModel.addParking(TestInstancesParking.parking2)
    parkingViewModel.addParking(TestInstancesParking.parking3)

    // Fetch the user
    userViewModel.getUserById("1")

    // Add favorite parkings
    userViewModel.addFavoriteParkingToSelectedUser(TestInstancesParking.parking1.uid)
    userViewModel.addFavoriteParkingToSelectedUser(TestInstancesParking.parking2.uid)
    userViewModel.addFavoriteParkingToSelectedUser(TestInstancesParking.parking3.uid)
    // Fetch favorite parkings
    userViewModel.getSelectedUserFavoriteParking()

    composeTestRule.setContent {
      ViewProfileScreen(navigationActions = mockNavigationActions, userViewModel = userViewModel)
    }
  }

  @Test
  fun testInitialDisplayMode() {
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ProfileImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("EditButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DisplayFirstName").assertTextEquals("Jane")
    composeTestRule.onNodeWithTag("DisplayLastName").assertTextEquals("Smith")
    composeTestRule.onNodeWithTag("DisplayUsername").assertTextEquals("@janesmith")
  }

  @Test
  fun testEditModeTransition() {
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("EditButton").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ProfileImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("EditButton").assertIsNotDisplayed()
    composeTestRule.onNodeWithTag("FirstNameField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("LastNameField").assertIsDisplayed()
    composeTestRule.onNodeWithTag("UsernameField").assertIsDisplayed()
  }

  @Test
  fun testSaveChanges() {
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("EditButton").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("FirstNameField").performTextReplacement("Alice")
    composeTestRule.onNodeWithTag("LastNameField").performTextReplacement("Johnson")
    composeTestRule.onNodeWithTag("UsernameField").performTextReplacement("alicejohnson")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("SaveButton").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ProfileImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("EditButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DisplayFirstName").assertTextEquals("Alice")
    composeTestRule.onNodeWithTag("DisplayLastName").assertTextEquals("Johnson")
    composeTestRule.onNodeWithTag("DisplayUsername").assertTextEquals("@alicejohnson")
  }

  @Test
  fun testCancelEditMode() {
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("EditButton").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("FirstNameField").performTextReplacement("Alice")
    composeTestRule.onNodeWithTag("LastNameField").performTextReplacement("Johnson")
    composeTestRule.onNodeWithTag("UsernameField").performTextReplacement("@alicejohnson")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("CancelButton").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ProfileImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("EditButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("DisplayFirstName").assertTextEquals("Jane")
    composeTestRule.onNodeWithTag("DisplayLastName").assertTextEquals("Smith")
    composeTestRule.onNodeWithTag("DisplayUsername").assertTextEquals("@janesmith")
  }

  @Test
  fun testCantChangeProfilePicture() {
    // Dont enter edit mode
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ProfileImage").assertHasNoClickAction()
  }

  @Test
  fun testChangeProfilePicture() {
    // Enter edit mode
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("EditButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ProfileImage").assertHasClickAction()
  }

  @Test
  fun testFavoriteParkingsDisplay() {
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("FavoriteParkingsTitle").assertIsDisplayed()

    composeTestRule.onNodeWithTag("FavoriteParkingList").onChildren().assertCountEquals(3)
    composeTestRule.onNodeWithTag("ParkingItem_0").assertTextContains("Rue de la paix")
    composeTestRule.onNodeWithTag("ParkingItem_1").assertTextContains("Rude épais")
    composeTestRule.onNodeWithTag("ParkingItem_2").assertTextContains("Rue du pet")
  }

  @Test
  fun testStarIconClickDisplaysConfirmationDialog() {
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("FavoriteToggle_0").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Remove favorite").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Are you sure you want to remove Rue de la paix from your favorites?")
        .assertIsDisplayed()
  }

  @Test
  fun testConfirmRemoveFavoriteParking() {
    composeTestRule.waitForIdle()

    // Click the star icon to show the confirmation dialog
    composeTestRule.onNodeWithTag("FavoriteToggle_0").performClick()
    composeTestRule.waitForIdle()

    // Confirm removal
    composeTestRule.onNodeWithText("Remove").performClick()
    composeTestRule.waitForIdle()

    // Verify the specific parking is removed from favorites
    composeTestRule.onNodeWithText("Rue de la paix").assertDoesNotExist()
  }

  @Test
  fun testCancelRemoveFavoriteParking() {
    composeTestRule.waitForIdle()

    // Click the star icon to show the confirmation dialog
    composeTestRule.onNodeWithTag("FavoriteToggle_0").performClick()
    composeTestRule.waitForIdle()

    // Cancel removal
    composeTestRule.onNodeWithText("Cancel").performClick()
    composeTestRule.waitForIdle()

    // Verify the specific parking is still in favorites
    composeTestRule.onNodeWithText("Rue de la paix").assertIsDisplayed()
  }

  @Test
  fun testRemoveAllFavoriteParkings() {
    composeTestRule.waitForIdle()

    // Remove the first parking
    composeTestRule.onNodeWithTag("FavoriteToggle_0").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Remove").performClick()
    composeTestRule.waitForIdle()

    // Remove the second parking
    composeTestRule.onNodeWithTag("FavoriteToggle_0").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Remove").performClick()
    composeTestRule.waitForIdle()

    // Remove the third parking
    composeTestRule.onNodeWithTag("FavoriteToggle_0").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Remove").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("NoFavoritesMessage").assertIsDisplayed()
  }

  @Test
  fun testFavoriteToggleNotClickableWhileEditing() {
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("EditButton").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("FavoriteToggle_0").assertIsNotDisplayed()
  }

  @Test
  fun testAddParkingAndScrollFavorites() {
    composeTestRule.waitForIdle()

    val parking4 =
        Parking(
            "Test_spot_4",
            "Roulade P",
            "Wazzup beijing",
            Location(Point.fromLngLat(7.19, 47.19)),
            listOf(
                "https://upload.wikimedia.org/wikipedia/commons/7/78/%22G%C3%A4nsemarkt%22_in_Amance_-_panoramio.jpg"),
            ParkingCapacity.LARGE,
            ParkingRackType.TWO_TIER,
            ParkingProtection.COVERED,
            0.0,
            true)

    parkingViewModel.addParking(parking4)
    userViewModel.addFavoriteParkingToSelectedUser(parking4.uid)
    userViewModel.getSelectedUserFavoriteParking()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("FavoriteToggle_3").assertDoesNotExist()

    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag("FavoriteParkingList")
        .performScrollToNode(hasTestTag("FavoriteToggle_3"))
    composeTestRule.onNodeWithTag("FavoriteToggle_3").assertIsDisplayed()
  }
}
