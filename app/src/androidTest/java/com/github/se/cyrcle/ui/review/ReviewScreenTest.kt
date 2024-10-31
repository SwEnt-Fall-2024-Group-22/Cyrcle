package com.github.se.cyrcle.ui.review

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockReviewRepository
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.review.TestInstancesReview
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.doNothing

@RunWith(AndroidJUnit4::class)
class ReviewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // Mock dependencies
  private val mockNavigationActions = mock(NavigationActions::class.java)

  private val mockParkingRepository = MockParkingRepository()
  private val mockImageRepository = MockImageRepository()
  private val mockReviewRepository = MockReviewRepository()
  private val parkingViewModel = ParkingViewModel(mockImageRepository, mockParkingRepository)
  private val reviewViewModel = ReviewViewModel(mockReviewRepository)

  @Before
  fun setUp() {
    parkingViewModel.selectParking(TestInstancesParking.parking1)
    reviewViewModel.addReview(TestInstancesReview.review1)
  }

  @Test
  fun reviewScreen_hasTopAppBar() {
    composeTestRule.setContent {
      ReviewScreen(mockNavigationActions, parkingViewModel, reviewViewModel)
    }

    composeTestRule
        .onNodeWithTag("TopAppBarTitle")
        .assertIsDisplayed()
        .assertTextContains("Add Your Review")
  }

  @Test
  fun reviewScreen_sliderChanges() {
    composeTestRule.setContent {
      ReviewScreen(mockNavigationActions, parkingViewModel, reviewViewModel)
    }

    // Assert initial value of the slider
    composeTestRule.onNodeWithText("Rating: 0.0").assertExists()

    // Perform actions on the slider
    composeTestRule.onNodeWithTag("Slider").performTouchInput { swipeRight() }

    // Assert value after slider change
    composeTestRule.onNodeWithText("Rating: 5.0").assertExists()
  }

  @Test
  fun reviewScreen_starsReflectSliderValue() {
    composeTestRule.setContent {
      ReviewScreen(mockNavigationActions, parkingViewModel, reviewViewModel)
    }

    // Test initial stars (all empty)
    composeTestRule.onNodeWithTag("Star1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Star2").assertIsDisplayed()

    // Move slider to 2.5
    composeTestRule.onNodeWithTag("Slider").performTouchInput { swipeRight(50f) }

    // Assert stars are updated
    composeTestRule.onNodeWithTag("Star1").assertExists()
    composeTestRule.onNodeWithTag("Star3").assertExists()
  }

  @Test
  fun reviewScreen_addReviewButtonSaves() {
    doNothing().`when`(mockNavigationActions).navigateTo(Screen.CARD)

    composeTestRule.setContent {
      ReviewScreen(mockNavigationActions, parkingViewModel, reviewViewModel)
    }
    // Enter a review
    composeTestRule.onNodeWithTag("ReviewInput").performTextInput("Great parking!")

    // Click Add Review button
    composeTestRule.onNodeWithTag("AddReviewButton").performClick()
  }
}
