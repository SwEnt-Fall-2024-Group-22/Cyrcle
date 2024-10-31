package com.github.se.cyrcle.ui.review

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.di.mocks.MockReviewRepository
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.review.TestInstancesReview
import com.github.se.cyrcle.ui.navigation.NavigationActions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class AllReviewsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val navigationActions = mock(NavigationActions::class.java)
  private val parkingViewModel = ParkingViewModel(MockImageRepository(), MockParkingRepository())
  private val reviewViewModel = ReviewViewModel(MockReviewRepository())

  @Before
  fun setUp() {
    parkingViewModel.selectParking(TestInstancesParking.parking2)
    reviewViewModel.addReview(TestInstancesReview.review4)
    reviewViewModel.addReview(TestInstancesReview.review3)
    reviewViewModel.addReview(TestInstancesReview.review2)
    reviewViewModel.addReview(TestInstancesReview.review1)
  }

  @Test
  fun allReviewsScreen_displaysTopBarAndList() {

    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel)
    }

    composeTestRule.onNodeWithTag("AllReviewsScreenBox").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ReviewCard0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ReviewCard1").assertIsDisplayed()
  }

  @Test
  fun clickingReviewCard_expandsAndCollapsesCard() {
    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel)
    }

    composeTestRule.onNodeWithTag("ReviewCard0").performClick()
    composeTestRule.onNodeWithTag("ExpandedReviewBox0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ExpandedReviewBox0").assertTextContains("Rating: 4.5")
    composeTestRule.onNodeWithTag("ExpandedReviewBox0").assertTextContains("Text: New Review.")
    composeTestRule.onNodeWithTag("ExpandedReviewBox0").performClick()
    composeTestRule.onNodeWithTag("ReviewCard0").assertIsDisplayed()
  }

  @Test
  fun clickingAnotherReviewCard_expandsCorrectCard() {

    composeTestRule.setContent {
      AllReviewsScreen(navigationActions, parkingViewModel, reviewViewModel)
    }

    composeTestRule.onNodeWithTag("ReviewCard1").performClick()
    composeTestRule.onNodeWithTag("ExpandedReviewBox1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ExpandedReviewBox1").assertTextContains("Rating: 1.0")
    composeTestRule.onNodeWithTag("ExpandedReviewBox1").assertTextContains("Text: Bad Parking.")
  }
}
