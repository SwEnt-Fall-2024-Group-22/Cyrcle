package com.github.se.cyrcle.ui.parkingDetails

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockReviewRepository
import com.github.se.cyrcle.di.mocks.MockUserRepository
import com.github.se.cyrcle.model.parking.ImageRepository
import com.github.se.cyrcle.model.parking.ParkingCapacity
import com.github.se.cyrcle.model.parking.ParkingProtection
import com.github.se.cyrcle.model.parking.ParkingRackType
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.review.ReviewRepository
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.TestInstancesUser
import com.github.se.cyrcle.model.user.UserRepository
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class ParkingDetailsScreenTest {
  private lateinit var parkingRepository: ParkingRepository
  private lateinit var imageRepository: ImageRepository
  private lateinit var userRepository: UserRepository
  private lateinit var reviewRepository: ReviewRepository

  private lateinit var userViewModel: UserViewModel
  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var reviewViewModel: ReviewViewModel

  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    navigationActions = mock(NavigationActions::class.java)

    parkingRepository = MockParkingRepository()
    imageRepository = MockImageRepository()
    userRepository = MockUserRepository()
    reviewRepository = MockReviewRepository()

    parkingViewModel = ParkingViewModel(imageRepository, parkingRepository)
    userViewModel = UserViewModel(userRepository, parkingRepository)
    reviewViewModel = ReviewViewModel(reviewRepository)

    parkingViewModel.addParking(TestInstancesParking.parking2)
    parkingViewModel.addParking(TestInstancesParking.parking3)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.PARKING_DETAILS)
  }

  @Test
  fun favoriteIconVisibleWhenNotSignedInAndDoesNothing() {
    parkingViewModel.selectParking(TestInstancesParking.parking1)
    userViewModel.setCurrentUser(null) // Ensure user is signed out

    composeTestRule.setContent {
      ParkingDetailsScreen(navigationActions, parkingViewModel, userViewModel)
    }

    composeTestRule.onNodeWithTag("FavoriteIconContainer").assertIsDisplayed()
    composeTestRule.onNodeWithTag("BlackOutlinedFavoriteIcon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("BlackOutlinedFavoriteIcon").performClick()
    composeTestRule.onNodeWithTag("BlackOutlinedFavoriteIcon").assertIsDisplayed()
  }

  @Test
  fun addToFavoritesWhenSignedIn() {

    parkingViewModel.selectParking(TestInstancesParking.parking3)
    userViewModel.addUser(TestInstancesUser.user1)
    userViewModel.getUserById(TestInstancesUser.user1.public.userId)
    userViewModel.getSelectedUserFavoriteParking()

    composeTestRule.setContent {
      ParkingDetailsScreen(navigationActions, parkingViewModel, userViewModel)
    }
    // Initially should show outline icon
    composeTestRule.onNodeWithTag("BlackOutlinedFavoriteIcon").assertIsDisplayed()

    // Click to add to favorites
    composeTestRule.onNodeWithTag("BlackOutlinedFavoriteIcon").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("RedFilledFavoriteIcon").assertIsDisplayed()
  }

  @Test
  fun removeFromFavoritesWhenSignedIn() {
    parkingViewModel.selectParking(TestInstancesParking.parking1)
    userViewModel.addUser(TestInstancesUser.user1)
    userViewModel.getUserById(TestInstancesUser.user1.public.userId)
    userViewModel.getSelectedUserFavoriteParking()

    composeTestRule.setContent {
      ParkingDetailsScreen(navigationActions, parkingViewModel, userViewModel)
    }

    // Initially should show filled icon
    composeTestRule.onNodeWithTag("RedFilledFavoriteIcon").assertIsDisplayed()

    // Click to remove from favorites
    composeTestRule.onNodeWithTag("RedFilledFavoriteIcon").performClick()

    // Should now show outline icon
    composeTestRule.onNodeWithTag("BlackOutlinedFavoriteIcon").assertIsDisplayed()
  }

  @Test
  fun displayAllComponents() {
    parkingViewModel.selectParking(TestInstancesParking.parking1)
    composeTestRule.setContent {
      ParkingDetailsScreen(navigationActions, parkingViewModel, userViewModel)
    }

    // Verify the top app bar
    composeTestRule.onNodeWithTag("TopAppBar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("TopAppBarTitle").assertIsDisplayed()

    // Verify the reviews
    composeTestRule.onNodeWithTag("AverageRatingRow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SeeAllReviewsText").assertIsDisplayed()

    // Verify the images
    composeTestRule.onNodeWithTag("ParkingImagesRow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ParkingImage0").assertIsDisplayed()

    // Scroll to the information section
    composeTestRule.onNodeWithTag("CapacityColumn").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("RackTypeColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ProtectionColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PriceColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SecurityColumn").assertIsDisplayed()

    // Scroll to the buttons section
    composeTestRule.onNodeWithTag("ButtonsColumn").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("ShowInMapButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ReportButton").assertIsDisplayed()
  }

  @Test
  fun componentsDisplayCorrectValues() {
    parkingViewModel.selectParking(TestInstancesParking.parking1)
    composeTestRule.setContent {
      ParkingDetailsScreen(navigationActions, parkingViewModel, userViewModel)
    }

    composeTestRule
        .onNodeWithTag("TopAppBarTitle")
        .assertTextContains("Description of Rue de la paix")
    composeTestRule.onNodeWithTag("ParkingImagesRow").onChildren().assertCountEquals(1)
    composeTestRule
        .onNodeWithTag("CapacityColumn")
        .onChildAt(1)
        .assertTextContains(ParkingCapacity.LARGE.description)
    composeTestRule
        .onNodeWithTag("RackTypeColumn")
        .onChildAt(1)
        .assertTextContains(ParkingRackType.TWO_TIER.description)
    composeTestRule
        .onNodeWithTag("ProtectionColumn")
        .onChildAt(1)
        .assertTextContains(ParkingProtection.COVERED.description)
    composeTestRule.onNodeWithTag("PriceColumn").onChildAt(1).assertTextContains("Free")
    composeTestRule.onNodeWithTag("SecurityColumn").onChildAt(1).assertTextContains("Yes")
  }

  @Test
  fun displayTitleAndMultipleImages() {
    parkingViewModel.selectParking(TestInstancesParking.parking2)
    composeTestRule.setContent {
      ParkingDetailsScreen(navigationActions, parkingViewModel, userViewModel)
    }

    composeTestRule.onNodeWithTag("TopAppBarTitle").assertTextContains("Description of Rude épais")
    composeTestRule.onNodeWithTag("ParkingImagesRow").onChildren().assertCountEquals(2)
  }

  @Test
  fun seeAllReviewsBehavesCorrectly() {
    parkingViewModel.selectParking(TestInstancesParking.parking1)
    composeTestRule.setContent {
      ParkingDetailsScreen(navigationActions, parkingViewModel, userViewModel)
    }

    composeTestRule.onNodeWithTag("SeeAllReviewsText").performClick()

    verify(navigationActions).navigateTo(Screen.ALL_REVIEWS)
  }
}
