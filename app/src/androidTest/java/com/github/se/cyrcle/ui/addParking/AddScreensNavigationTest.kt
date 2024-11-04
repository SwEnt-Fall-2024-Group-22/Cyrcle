package com.github.se.cyrcle.ui.addParking

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.cyrcle.CyrcleNavHost
import com.github.se.cyrcle.model.address.AddressViewModel
import com.github.se.cyrcle.model.map.MapViewModel
import com.github.se.cyrcle.model.parking.ImageRepository
import com.github.se.cyrcle.model.parking.Location
import com.github.se.cyrcle.model.parking.ParkingRepository
import com.github.se.cyrcle.model.parking.ParkingViewModel
import com.github.se.cyrcle.model.review.ReviewViewModel
import com.github.se.cyrcle.model.user.User
import com.github.se.cyrcle.model.user.UserViewModel
import com.github.se.cyrcle.ui.addParking.attributes.AttributesPicker
import com.github.se.cyrcle.ui.addParking.location.LocationPicker
import com.github.se.cyrcle.ui.map.MapScreen
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.mapbox.geojson.Point
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class AddScreensNavigationTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Mock lateinit var mockedParkingRepository: ParkingRepository
  @Mock lateinit var mockedImageRepository: ImageRepository

  @Composable
  fun setUp(): List<Any> {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)
    val parkingViewModel: ParkingViewModel = viewModel(factory = ParkingViewModel.Factory)
    val reviewViewModel: ReviewViewModel = viewModel(factory = ReviewViewModel.Factory)
    val mapViewModel: MapViewModel = viewModel(factory = MapViewModel.Factory)
    val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
    val addressViewModel: AddressViewModel = viewModel(factory = AddressViewModel.Factory)
    CyrcleNavHost(
        navigationActions,
        navController,
        parkingViewModel,
        reviewViewModel,
        userViewModel,
        mapViewModel,
        addressViewModel)
    return listOf<Any>(
        navigationActions,
        parkingViewModel,
        reviewViewModel,
        userViewModel,
        mapViewModel,
        addressViewModel)
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun testAddButtonNavigatesToLocationPicker() {

    composeTestRule.setContent {
      val list = setUp()
      val navigationActions = list[0] as NavigationActions
      val parkingViewModel = list[1] as ParkingViewModel
      val userViewModel = list[3] as UserViewModel
      val mapViewModel = list[4] as MapViewModel

      userViewModel.setCurrentUser(User(userId = "default", username = "sayMyName", email = ""))
      MapScreen(navigationActions, parkingViewModel, userViewModel, mapViewModel)
    }
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("addButton"))
    // Perform click on the add button
    composeTestRule.onNodeWithTag("addButton").performClick()
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("LocationPickerScreen"))
    // Wait until the Location Picker screen is displayed
    composeTestRule.onNodeWithText("Where is the Parking ?").assertExists().isDisplayed()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun testNavigationToAttribute() {

    composeTestRule.setContent {
      val list = setUp()
      val navigationActions = list[0] as NavigationActions
      val mapViewModel = list[4] as MapViewModel
      LocationPicker(navigationActions, mapViewModel)
    }
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("nextButton"))
    // Perform click on the add button
    composeTestRule.onNodeWithTag("nextButton").performClick()
    // check that we're still on the same screen
    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("LocationPickerScreen"))
    composeTestRule.onNodeWithTag("LocationPickerScreen").assertExists().assertIsDisplayed()

    // Perform Click on the next button
    composeTestRule.onNodeWithTag("nextButton").performClick()
    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("AttributesPickerScreen"))
    composeTestRule.onNodeWithTag("AttributesPickerScreen").assertExists().isDisplayed()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun testSubmit() {
    MockitoAnnotations.openMocks(this)
    `when`(mockedParkingRepository.getNewUid()).thenReturn("newUid")
    composeTestRule.setContent {
      val list = setUp()
      val navigationActions = list[0] as NavigationActions
      val parkingViewModel = ParkingViewModel(mockedImageRepository, mockedParkingRepository)
      val mapViewModel = list[4] as MapViewModel
      val addressViewModel = list[5] as AddressViewModel
      mapViewModel.updateLocation(Location(Point.fromLngLat(0.0, 0.0)))
      AttributesPicker(navigationActions, parkingViewModel, mapViewModel, addressViewModel)
    }
    composeTestRule.waitUntilExactlyOneExists(hasTestTag("submitButton"))
    // Perform click on the add button
    composeTestRule.onNodeWithTag("submitButton").performClick()

    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("MapScreen"))
    composeTestRule.onNodeWithTag("MapScreen").assertExists().assertIsDisplayed()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun testCancel() {
    composeTestRule.setContent {
      val list = setUp()
      val navigationActions = list[0] as NavigationActions
      val parkingViewModel = list[1] as ParkingViewModel
      val mapViewModel = list[4] as MapViewModel
      val addressViewModel = list[5] as AddressViewModel
      mapViewModel.updateLocation(Location(Point.fromLngLat(0.0, 0.0)))
      AttributesPicker(navigationActions, parkingViewModel, mapViewModel, addressViewModel)
    }

    composeTestRule.waitUntilExactlyOneExists(hasTestTag("cancelButton"))
    // Perform click on the add button
    composeTestRule.onNodeWithTag("cancelButton").performClick()

    composeTestRule.waitUntilAtLeastOneExists(hasTestTag("MapScreen"))
    composeTestRule.onNodeWithTag("MapScreen").assertExists().assertIsDisplayed()
  }

  @OptIn(ExperimentalTestApi::class)
  @Test
  fun testCancel2() {
    composeTestRule.setContent {
      val list = setUp()
      val navigationActions = list[0] as NavigationActions
      val mapViewModel = list[4] as MapViewModel
      LocationPicker(navigationActions, mapViewModel)
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("cancelButton").performClick()
    composeTestRule.waitUntilExactlyOneExists((hasTestTag("MapScreen")))
    composeTestRule.onNodeWithTag("MapScreen").assertExists().assertIsDisplayed()
  }
}
