package com.github.se.cyrcle.ui.profile

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockAuthenticationRepository
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockOfflineParkingRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockReportedObjectRepository
import com.github.se.cyrcle.di.mocks.MockReviewRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.parking.Location
import com.github.se.cyrcle.model.parking.Parking
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.report.ReportedObjectRepository
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.review.TestInstancesReview
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserDetails
import com.github.se.cyrcle.model.user.UserPublic
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import com.github.se.cyrcle.ui.navigation.TopLevelDestinations
import com.mapbox.geojson.Point
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class ViewProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockNavigationActions: NavigationActions
  private lateinit var mockUserRepository: MockUserRepository
  private lateinit var mockParkingRepository: MockParkingRepository
  private lateinit var mockOfflineParkingRepository: MockOfflineParkingRepository
  private lateinit var mockImageRepository: MockImageRepository
  private lateinit var mockAuthenticator: MockAuthenticationRepository
  private lateinit var mockReportedObjectRepository: ReportedObjectRepository
  private lateinit var mockReviewRepository: MockReviewRepository

  private lateinit var userViewModel: UserViewModel
  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var reviewViewModel: ReviewViewModel

  @Before
  fun setUp() {
    mockNavigationActions = mock(NavigationActions::class.java)
    mockUserRepository = MockUserRepository()
    mockParkingRepository = MockParkingRepository()
    mockOfflineParkingRepository = MockOfflineParkingRepository()
    mockImageRepository = MockImageRepository()
    mockAuthenticator = MockAuthenticationRepository()
    mockReportedObjectRepository = MockReportedObjectRepository()
    mockReviewRepository = MockReviewRepository()

    val user =
        User(
            UserPublic("1", "janesmith", "http://example.com/jane.jpg"),
            UserDetails("Jane", "Smith", "jane.smith@example.com"))

    userViewModel =
        UserViewModel(
            mockUserRepository, mockParkingRepository, mockImageRepository, mockAuthenticator)
    parkingViewModel =
        ParkingViewModel(
            mockImageRepository,
            userViewModel,
            mockParkingRepository,
            mockOfflineParkingRepository,
            mockReportedObjectRepository)
    reviewViewModel = ReviewViewModel(mockReviewRepository, mockReportedObjectRepository)

    userViewModel.addUser(user, {}, {})
    mockAuthenticator.testUser = user
    userViewModel.signIn({}, {})

    // parking1 already added by whoever created parkingviewmodelmock on instanciation
    parkingViewModel.addParking(TestInstancesParking.parking2)
    parkingViewModel.addParking(TestInstancesParking.parking3)

    // Fetch the user
    userViewModel.setCurrentUserById("1")

    // Add favorite parkings
    userViewModel.addFavoriteParkingToSelectedUser(TestInstancesParking.parking1)
    userViewModel.addFavoriteParkingToSelectedUser(TestInstancesParking.parking2)
    userViewModel.addFavoriteParkingToSelectedUser(TestInstancesParking.parking3)

    composeTestRule.setContent {
      ViewProfileScreen(
          navigationActions = mockNavigationActions,
          userViewModel = userViewModel,
          parkingViewModel = parkingViewModel,
          reviewViewModel = reviewViewModel)
    }
  }

  @Test
  fun testSignOutApproval() {
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("SignOutButton").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("SignOutDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SignOutDialogConfirmButton").assertIsDisplayed().performClick()
    verify(mockNavigationActions).navigateTo(TopLevelDestinations.AUTH)
  }

  @Test
  fun testSignOutRefuse() {
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("SignOutButton").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("SignOutDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SignOutDialogCancelButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("SignOutButton").assertIsDisplayed()
  }

  @Test
  fun testGamblingNavigation() {
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("GamblingButton").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    verify(mockNavigationActions).navigateTo(Screen.GAMBLING)
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

    // We display the parking tabs
    composeTestRule
        .onNodeWithTag("TabFavoriteParkings")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()

    composeTestRule.onNodeWithTag("FavoriteParkingList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ParkingItem0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ParkingNote0", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("ParkingName0", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextContains("Rue de la paix")

    composeTestRule.onNodeWithTag("FavoriteParkingList").performScrollToIndex(1)
    composeTestRule.onNodeWithTag("ParkingNote1", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("ParkingItem1").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("ParkingName1", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextContains("Rude épais")

    composeTestRule.onNodeWithTag("FavoriteParkingList").performScrollToIndex(2)
    composeTestRule.onNodeWithTag("ParkingItem2").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ParkingNote2", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("ParkingName2", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextContains("Rue du pet")
  }

  @Test
  fun testStarIconClickDisplaysConfirmationDialog() {
    // We display the parking tabs
    composeTestRule
        .onNodeWithTag("TabFavoriteParkings")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("FavoriteToggle0", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Remove favorite").assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Are you sure you want to remove Rue de la paix from your favorites?")
        .assertIsDisplayed()
  }

  @Test
  fun testConfirmRemoveFavoriteParking() {
    composeTestRule.waitForIdle()
    // We display the parking tabs
    composeTestRule
        .onNodeWithTag("TabFavoriteParkings")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()

    // Click the star icon to show the confirmation dialog
    composeTestRule.onNodeWithTag("FavoriteToggle0", useUnmergedTree = true).performClick()
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
    // We display the parking tabs
    composeTestRule
        .onNodeWithTag("TabFavoriteParkings")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()

    // Click the star icon to show the confirmation dialog
    composeTestRule.onNodeWithTag("FavoriteToggle0", useUnmergedTree = true).performClick()
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
    // We display the parking tabs
    composeTestRule
        .onNodeWithTag("TabFavoriteParkings")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()

    // Remove the first parking
    composeTestRule.onNodeWithTag("FavoriteToggle0", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Remove").performClick()
    composeTestRule.waitForIdle()
    // Remove the second parking
    composeTestRule.onNodeWithTag("FavoriteToggle0", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Remove").performClick()
    composeTestRule.waitForIdle()

    // Remove the third parking
    composeTestRule.onNodeWithTag("FavoriteToggle0", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Remove").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("NoFavoritesMessage").assertIsDisplayed()
  }

  @Test
  fun testRemoveFavoriteParkingsAndCheckIndexes() {
    composeTestRule.waitForIdle()
    // We display the parking tabs
    composeTestRule
        .onNodeWithTag("TabFavoriteParkings")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()

    // Remove the middle parking
    composeTestRule.onNodeWithTag("FavoriteParkingList").performScrollToIndex(1)
    composeTestRule.onNodeWithTag("ParkingItem1", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("FavoriteToggle1").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Remove").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Rue de la paix").assertIsDisplayed()
    composeTestRule.onNodeWithText("Rue du pet").assertIsDisplayed()

    // Remove the third parking (which is now second)
    composeTestRule.onNodeWithTag("FavoriteToggle1").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Remove").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Rue de la paix").assertIsDisplayed()
  }

  @Test
  fun testFavoriteToggleNotClickableWhileEditing() {
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("EditButton").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("FavoriteToggle0").assertIsNotDisplayed()
  }

  @Test
  fun testAddParkingAndScrollFavorites() {
    composeTestRule.waitForIdle()

    // We display the parking tabs
    composeTestRule
        .onNodeWithTag("TabFavoriteParkings")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()

    val parking4 =
        Parking(
            "Test_spot_4",
            "Roulade P",
            "Wazzup beijing",
            Location(Point.fromLngLat(7.19, 47.19)),
            listOf(
                "https://upload.wikimedia.org/wikipedia/commons/7/78/%22G%C3%A4nsemarkt%22_in_Amance_-_panoramio.jpg"),
            emptyList(),
            0,
            emptyList(),
            ParkingCapacity.LARGE,
            ParkingRackType.TWO_TIER,
            ParkingProtection.COVERED,
            0.0,
            true,
            owner = TestInstancesUser.user1.public.userId,
            emptyList())

    parkingViewModel.addParking(parking4)
    userViewModel.addFavoriteParkingToSelectedUser(parking4)

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("FavoriteParkingList").performScrollToIndex(3)
    composeTestRule.onNodeWithTag("FavoriteToggle3").assertIsDisplayed()
  }

  @Test
  fun testUsernamePersistenceAfterSaveAndCancel() {
    composeTestRule.waitForIdle()

    // Enter edit mode
    composeTestRule.onNodeWithTag("EditButton").performClick()
    composeTestRule.waitForIdle()

    // Change username and save
    composeTestRule.onNodeWithTag("UsernameField").performTextReplacement("alicejohnson")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("SaveButton").performClick()
    composeTestRule.waitForIdle()

    // Verify that the display reflects the new username
    composeTestRule.onNodeWithTag("DisplayUsername").assertTextEquals("@alicejohnson")

    // Enter edit mode again and cancel
    composeTestRule.onNodeWithTag("EditButton").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("CancelButton").performClick()
    composeTestRule.waitForIdle()

    // Verify that the display still reflects the new username
    composeTestRule.onNodeWithTag("DisplayUsername").assertTextEquals("@alicejohnson")
  }

  @Test
  fun testNavigateToParkingDetailsOnClick() {
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("ParkingItem0", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()

    verify(mockNavigationActions).navigateTo(Screen.PARKING_DETAILS)
  }

  @Test
  fun whenNoReviews_showsEmptyMessage() {
    composeTestRule.waitForIdle()

    // We display the parking tabs
    composeTestRule
        .onNodeWithTag("TabMyReviews")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()

    composeTestRule
        .onNodeWithTag("NoReviewsMessage")
        .assertIsDisplayed()
        .assertTextEquals("You haven't written any reviews yet")
  }

  @Test
  fun whenUserHasReviews_showsReviewsList() {
    myReviewsTestHelper()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("UserReviewsList").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("ReviewCard0", useUnmergedTree = true)
        .performScrollTo()
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag("ReviewTitle0", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextEquals(TestInstancesParking.parking2.optName!!)

    // Ensure the card is clickable and it takes us the all reviews screen
    composeTestRule.onNodeWithTag("ReviewCard0").assertHasClickAction().performClick()
    composeTestRule.waitForIdle()

    verify(mockNavigationActions).navigateTo(Screen.PARKING_DETAILS)
    assert(parkingViewModel.selectedParking.value == TestInstancesParking.parking2)
  }

  /**
   * Helper function to set up the test environment for the "My Reviews" tab.
   * - Select the "My Reviews" tab
   * - Add test parkings to the mock repository
   * - Add user1's reviews from TestInstancesReview
   * - Trigger review fetch for user1
   */
  private fun myReviewsTestHelper() {
    composeTestRule
        .onNodeWithTag("TabMyReviews")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()

    // Add test parkings to mock repository
    mockParkingRepository.addParking(TestInstancesParking.parking1, {}, {}) // For review5
    mockParkingRepository.addParking(TestInstancesParking.parking2, {}, {}) // For review1
    mockReviewRepository.addReview(TestInstancesReview.review1, {}, {})
    mockReviewRepository.addReview(TestInstancesReview.review5, {}, {})
    reviewViewModel.getReviewsByOwnerId("user1")
  }

  @Test
  fun testTabLayoutSwipeChangesTab() {
    composeTestRule.onNodeWithTag("TabFavoriteParkings").assertIsSelected()
    composeTestRule.onNodeWithTag("FavoriteParkingList").assertIsDisplayed()
    composeTestRule.onNodeWithTag("FavoriteParkingList").performTouchInput {
      swipeLeft(startX = this.width * 0.9f, endX = this.width * 0.1f)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("TabMyReviews").assertIsSelected()
  }

  @Test
  fun testTabLayoutSwipeDoesNotExceedTabCount() {
    composeTestRule.onNodeWithTag("FavoriteParkingList").performTouchInput {
      swipeRight(startX = this.width * 0.1f, endX = this.width * 0.9f)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("TabFavoriteParkings").assertIsSelected()
    composeTestRule.onNodeWithTag("FavoriteParkingList").assertIsDisplayed()
  }

  @Test
  fun testDisplaysImagesListWhenNotEmpty() {
    val mockImagesUrls = listOf("https://example.com/image1.jpg", "https://example.com/image2.jpg")
    val mockImagePaths = listOf("path1", "path2")
    userViewModel._selectedUserImageUrls.value = mockImagesUrls
    userViewModel._selectedUserAssociatedImages.value = mockImagePaths
    composeTestRule.onNodeWithTag("TabMyImages").performClick()
    composeTestRule.onNodeWithTag("UserImagesList").assertIsDisplayed()
  }

  @Test
  fun testImageClickDisplaysDialog() {
    val mockImageUrl = "https://example.com/image1.jpg"
    val mockImagePath = "parking/mockParkingId/1"
    userViewModel._selectedUserImageUrls.value = listOf(mockImageUrl)
    userViewModel._selectedUserAssociatedImages.value = listOf(mockImagePath)
    composeTestRule.onNodeWithTag("TabMyImages").performClick()
    composeTestRule.onNodeWithTag("ImageCard").performClick()
    composeTestRule.onNodeWithTag("ParkingFromImageButton").assertIsDisplayed()
  }

  @Test
  fun testDeleteImageFromDialogCallsRemoveImageFromUserImages() {
    val mockImageUrl = "https://example.com/image1.jpg"
    val mockImagePath = "parking/mockParkingId/1"
    userViewModel._selectedUserImageUrls.value = listOf(mockImageUrl)
    userViewModel._selectedUserAssociatedImages.value = listOf(mockImagePath)
    composeTestRule.onNodeWithTag("TabMyImages").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("ImageCard").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("DeleteImageButton").performClick()
    composeTestRule.waitForIdle()
    val associatedImages = userViewModel._selectedUserAssociatedImages.value
    assert(!associatedImages.contains(mockImagePath)) {
      "Expected $mockImagePath to be removed from associated images, but it still exists."
    }
  }

  @Test
  fun testDisplaysNoImagesWhenListIsEmpty() {
    userViewModel._selectedUserImageUrls.value = emptyList()
    userViewModel._selectedUserAssociatedImages.value = emptyList()
    composeTestRule.onNodeWithTag("TabMyImages").performClick()
    composeTestRule.onNodeWithTag("NoImagesMessage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("UserImagesList").assertIsNotDisplayed()
  }
}
