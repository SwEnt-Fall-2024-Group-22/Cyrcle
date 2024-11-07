package com.github.se.cyrcle.ui.parkingDetails

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

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

    `when`(navigationActions.currentRoute()).thenReturn(Screen.CARD)
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

    // Verify the images
    composeTestRule.onNodeWithTag("ParkingImagesRow").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ParkingImage0").assertIsDisplayed()

    // Verify the buttons
    composeTestRule.onNodeWithTag("ButtonsColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ShowInMapButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("AddReviewButton").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ReportButton").assertIsDisplayed()

    // Verify the rest of the content
    composeTestRule.onNodeWithTag("CapacityColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("RackTypeColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ProtectionColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("PriceColumn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("SecurityColumn").assertIsDisplayed()
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
  fun addReviewButtonBehavesCorrectly() {
    parkingViewModel.selectParking(TestInstancesParking.parking1)
    composeTestRule.setContent {
      ParkingDetailsScreen(navigationActions, parkingViewModel, userViewModel)
    }

    composeTestRule.onNodeWithTag("AddReviewButton").assertIsDisplayed().performClick()

    verifyNoInteractions(navigationActions)

    userViewModel.setCurrentUser(TestInstancesUser.user1)
    composeTestRule.onNodeWithTag("AddReviewButton").assertIsDisplayed().performClick()

    verify(navigationActions).navigateTo(ArgumentMatchers.matches(Screen.REVIEW))
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
}
