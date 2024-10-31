package com.github.se.cyrcle.ui.card

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.di.mocks.MockImageRepository
import com.github.se.cyrcle.di.mocks.MockParkingRepository
import com.github.se.cyrcle.model.parking.ImageRepository
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.parking.TestInstancesParking
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class CardScreenTest {
  private lateinit var parkingRepository: ParkingRepository
  private lateinit var imageRepository: ImageRepository
  private lateinit var parkingViewModel: ParkingViewModel
  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    parkingRepository = MockParkingRepository()
    navigationActions = mock(NavigationActions::class.java)
    imageRepository = MockImageRepository()
    parkingViewModel = ParkingViewModel(imageRepository, parkingRepository)

    `when`(navigationActions.currentRoute()).thenReturn(Screen.CARD)
  }

  @Test
  fun displayAllComponents() {
    parkingViewModel.selectParking(TestInstancesParking.parking1)
    composeTestRule.setContent { CardScreen(navigationActions, parkingViewModel) }

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
    composeTestRule.setContent { CardScreen(navigationActions, parkingViewModel) }

    composeTestRule.onNodeWithTag("TopAppBarTitle").assertTextContains("Description of Parking")
    composeTestRule.onNodeWithTag("ParkingImagesRow").onChildren().assertCountEquals(1)
    composeTestRule.onNodeWithTag("CapacityColumn").onChildAt(1).assertTextContains("51-100 spots")
    composeTestRule.onNodeWithTag("RackTypeColumn").onChildAt(1).assertTextContains("Two-tier rack")
    composeTestRule.onNodeWithTag("ProtectionColumn").onChildAt(1).assertTextContains("Covered")
    composeTestRule.onNodeWithTag("PriceColumn").onChildAt(1).assertTextContains("Free")
    composeTestRule.onNodeWithTag("SecurityColumn").onChildAt(1).assertTextContains("Yes")
  }

  @Test
  fun displayTitleAndMultipleImages() {
    parkingViewModel.selectParking(TestInstancesParking.parking2)
    composeTestRule.setContent { CardScreen(navigationActions, parkingViewModel) }

    composeTestRule
        .onNodeWithTag("TopAppBarTitle")
        .assertTextContains("Description of Avenue de la Gare")
    composeTestRule.onNodeWithTag("ParkingImagesRow").onChildren().assertCountEquals(2)
  }
}
